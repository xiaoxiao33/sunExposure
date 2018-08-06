package com.example.xiaoxiaoouyang.sunexposure;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import java.util.Timer;
import java.util.TimerTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;


public class GPSService extends Service implements Serializable{
    /** indicates how to behave if the service is killed */
    int mStartMode;

    /** interface for clients that bind */
    IBinder mBinder;

    private PowerManager.WakeLock wl;

    public int counter = 0;

    public double uvmeasureint = 0;
    public boolean IOthresh;

    public GPSService(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public GPSService() {
    }

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    private Context context;
    private double longitude = 0;
    private double latitude = 0;
    private double templong = 0;
    private double templat = 0;
    private int tempnumOfSatellites = 0;
    private double tempuviMeasure = 0.0;
    private int numOfSatellites = 0;
    private Location loc = null;
    private double uviMeasure = 0.0;
    private boolean checker = false;
    private long timestamp;

    private static final int NOTIF_ID = 0;

    public static final String
            ACTION_LOCATION_BROADCAST = GPSService.class.getName() + "LocationBroadcast",
            EXTRA_LATITUDE = "extra_latitude",
            EXTRA_LONGITUDE = "extra_longitude",
            ACTION_UVI_BROADCAST = GPSService.class.getName() + "UviBroadcast",
            EXTRA_UVI = "extra_uvi",
            ACTION_SATELLITES_BROADCAST = GPSService.class.getName() + "SatellitesBroadcast",
            EXTRA_COUNT = "extra_count";

    private MyGpsListener myGpsListener;
    private MyLocationListener myLocationListener;
    private TelephonyManager telephonyManager;
    private LocationManager lm;

    boolean isGPSEnabled = false;

    private CSVManager csvManager = new CSVManager();
    public ArrayList<CSVRow> data = new ArrayList<CSVRow>();
    private Calendar cal;
    private int cellAsu;
    private int cellDbm;
    private int cellLevel;
    private int wifiPerc;
    private String mtime;

    private Handler m_handler;
    private Runnable m_handlerTask;

    private class MyLocationListener implements LocationListener {

        public MyLocationListener(Context c) {
            checkLocationPermission(c);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private void getWifiInfo(){
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if(wifi.isWifiEnabled()){
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            if(String.valueOf(wifiInfo.getSupplicantState()).equals("COMPLETED")){
                wifiPerc = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 100);
            }else{
                System.out.println("please connect to a wifi network!");

            }
        }else {
            System.out.println("please connect to a wifi network!");
        }
    }
    private void getCellSignal() {
        checkPhonePermission(context);
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
        if (allCellInfo != null && allCellInfo.size() != 0) {
            for (int i = 0 ; i < allCellInfo.size(); i++) {
                if (allCellInfo.get(i).isRegistered()) {
                    if (allCellInfo.get(i) instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        cellDbm = cellSignalStrengthLte.getDbm();
                        cellAsu = cellSignalStrengthLte.getAsuLevel();
                        cellLevel = cellSignalStrengthLte.getLevel();
                        break;
                    }
                }
            }
        }

    }

    private class MyGpsListener implements GpsStatus.Listener {

        public MyGpsListener(Context c) {
            checkLocationPermission(c);
            lm.addGpsStatusListener(myGpsListener);
            System.out.println("GPSstatus Listener attached");
        }

        @Override
        public void onGpsStatusChanged(int event){

        }
    }
//
//    public sendRequest() {
//
//    }

    public Boolean checkTime() {
        boolean value;
        Date d=new Date();
        int currentDateTimeInt;
        SimpleDateFormat sdf=new SimpleDateFormat("kk:mm");
        String currentDateTimeString = sdf.format(d);
        currentDateTimeString = currentDateTimeString.replace(":", "");
        int DATE_PICKER = 600;
        int DATE_PICKER2 = 2400;

        System.out.println("new int is: " + currentDateTimeString);



        //Toast.makeText(context, currentDateTimeString, Toast.LENGTH_LONG).show();
        if (currentDateTimeString.charAt(0) == '0') {
            String currentDateTimeString2 = currentDateTimeString.substring(0, 0) + currentDateTimeString.substring(1);
            System.out.println("new int is: " + currentDateTimeString2);
            currentDateTimeInt = Integer.parseInt(currentDateTimeString2);
        }
        else {
            currentDateTimeInt = Integer.parseInt(currentDateTimeString);
        }

        if (currentDateTimeInt >= DATE_PICKER && currentDateTimeInt <= DATE_PICKER2) {
            System.out.println("Data is being transmitted");
            value = true;
        }
        else {
            System.out.println("Data is not being transmitted");
            value = false;
        }
        return value;}


    @Override
    public void onCreate() {

        super.onCreate();
        checker = true;

//        String CHANNEL_ID = "my_channel_01";
//        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
//                "Channel human readable title",
//                NotificationManager.IMPORTANCE_DEFAULT);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "This is my wakelock");

//        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
//
//        Notification notification = new Notification.Builder(this, CHANNEL_ID)
//                .setContentTitle("")
//                .setContentText("").build();
//
//        startForeground(1, notification);
        m_handler = new Handler();
        m_handlerTask = new Runnable()
        {
            @Override
            public void run() {

                if (checkTime()) {
                    Log.i("sending", "sending!");
                    checkLocationPermission(context);
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        numOfSatellites = location.getExtras().getInt("satellites");
                        templong = longitude;
                        templat = latitude;
                        tempnumOfSatellites = numOfSatellites;
                    } else {
                        longitude = templong;
                        latitude = templat;
                        numOfSatellites = tempnumOfSatellites;
                    }

                    loc = location;

                    getWifiInfo();
                    getCellSignal();
                    CSVRow r = new CSVRow();

                    if (numOfSatellites < 4 && wifiPerc < 50) {
                        IOthresh = false;
                    } else {
                        IOthresh = true;
                    }

                    timestamp = Calendar.getInstance().getTimeInMillis();
                    mtime = getDate(timestamp);
                    r.mtime = mtime;
                    r.timestamp = timestamp;
                    templong = longitude;
                    templat = latitude;
                    tempnumOfSatellites = numOfSatellites;
                    double u = UVIMeasurement(longitude, latitude);
                    if (uviMeasure == 100) {
                        uviMeasure = tempuviMeasure;
                    }
                    else {
                        tempuviMeasure = uviMeasure;

                    }
                    r.longitude = longitude;
                    r.latitude = latitude;
                    r.uvi = uviMeasure;
                    r.numGPSSat = numOfSatellites;
                    r.wifiPerc = wifiPerc;
                    r.cellDbm = cellDbm;
                    r.cellAsu = cellAsu;
                    r.cellLevel = cellLevel;
                    r.IOthresh = IOthresh;

                    data.add(r);
                    sendBroadcastMessage(loc);
                    sendBroadcastMessage(uviMeasure);
                    sendBroadcastMessage(numOfSatellites);
                }
            else {
                    System.out.println("Not sending Request");
                }
                m_handler.postDelayed(m_handlerTask, 120000/2);
            }

        };
    };

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification.Builder builder = new Notification.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("SUN EXPOSURE IS CURRENTLY RETRIEVING DATA")
                    .setOngoing(true)
                    .setTicker("TICKER")
                    .setWhen(System.currentTimeMillis());

            builder.setChannelId(CHANNEL_ID);

            Notification notification = builder.build();
            startForeground(1, notification);
        }
        else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("SUN EXPOSURE IS CURRENTLY RETRIEVING DATA")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }
////        String CHANNEL_ID = "my_channel_01";
////        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
////                "Channel human readable title",
////                NotificationManager.IMPORTANCE_DEFAULT);
////        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
////
////        Notification notification = new Notification.Builder(this, CHANNEL_ID)
////                .setContentTitle("")
////                .setContentText("").build();
//
//        startForeground(1, notification);
        startTimer();
        context = getApplicationContext();
        checkLocationPermission(context);
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled && checker) {
            myGpsListener = new MyGpsListener(context);
            myLocationListener = new MyLocationListener(context);
            checker = false;
            m_handlerTask.run();
            wl.acquire();
        }
        else {
            stoptimertask();
        }
        return START_NOT_STICKY;
    }


    /** A client is binding to the service with bindService() */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {

    }


    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        stoptimertask();
        super.onDestroy();
        m_handler.removeCallbacks(m_handlerTask);

        lm.removeUpdates(myLocationListener);
        lm.removeGpsStatusListener(myGpsListener);
        myLocationListener = null;
        checker = true;
        myGpsListener = null;
        Toast.makeText(this, "Service halted", Toast.LENGTH_SHORT).show();

        Log.i("Service Stopped", "Service stopped");
        csvManager.saveData(data);
        wl.release();
        stopForeground(true);
        stopSelf();
//        Intent broadcastIntent = new Intent(getApplicationContext(), SensorRestarterBroadcastReceiver.class);
//        PendingIntent.getBroadcast(context, 0,
//                new Intent(getApplicationContext(), GPSService.class), 0);
//        sendBroadcast(broadcastIntent);
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public ArrayList<CSVRow> getData() {
        return data;
    }

    private void sendBroadcastMessage(Location location) {
        if (location != null) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, location.getLatitude());
            intent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void sendBroadcastMessage(double index) {

        Intent intent = new Intent(ACTION_UVI_BROADCAST);
        intent.putExtra(EXTRA_UVI, index);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void sendBroadcastMessage(int number) {

        Intent intent = new Intent(ACTION_SATELLITES_BROADCAST);
        intent.putExtra(EXTRA_COUNT, number);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void checkLocationPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
         Log.e("first","error");
        }
        try {
            lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        sdf.setTimeZone(tz);//set time zone.
        String localTime = sdf.format(time);
        return localTime;
    }

    private void checkPhonePermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED) {
            Log.e("first","check phone permission denied");
        }
        try {
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private double UVIMeasurement(double longitude, double latitude) {
        longitude = (double)Math.round(longitude * 100d) / 100d;
        latitude = (double)Math.round(latitude * 100d) / 100d;


        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String dateString = df.format(new Date());

        String address = "https://api.openuv.io/api/v1/uv?lat=" + String.valueOf(latitude) + "&lng=" + String.valueOf(longitude) + "&dt=" + dateString;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(address)
                .get()
                .addHeader("x-access-token", "35beb0a6436cd5c301105d75cb7da98a")
                .build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject responseJson = new JSONObject(response.body().string());

            uviMeasure = responseJson.getJSONObject("result").getDouble("uv");
            System.out.println("UVI NUM:" + Double.toString(uviMeasure));

            return 0;
        }
        catch (Exception e){
            e.printStackTrace();
            uviMeasure = 100;
            return -1;
        }
    }
}
