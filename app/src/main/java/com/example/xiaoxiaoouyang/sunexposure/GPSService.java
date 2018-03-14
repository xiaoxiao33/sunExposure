package com.example.xiaoxiaoouyang.sunexposure;


import android.Manifest;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


public class GPSService extends Service {
    /** indicates how to behave if the service is killed */
    int mStartMode;

    /** interface for clients that bind */
    IBinder mBinder;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    private Context context;
    private double longitude = 0;
    private double latitude = 0;
    private int numOfSatellites = 0;
    private Location loc = null;
    private double uviMeasure = 0.0;

    public static final String
            ACTION_LOCATION_BROADCAST = GPSService.class.getName() + "LocationBroadcast",
            EXTRA_LATITUDE = "extra_latitude",
            EXTRA_LONGITUDE = "extra_longitude",
            ACTION_SATELLITES_BROADCAST = GPSService.class.getName() + "SatellitesBroadcast",
            EXTRA_COUNT = "extra_count";




    private MyGpsListener myGpsListener;
    private MyLocationListener myLocationListener;
    private TelephonyManager telephonyManager;
    private LocationManager lm;

    boolean isGPSEnabled = false;

    private CSVManager csvManager = new CSVManager();
    ArrayList<CSVRow> data = new ArrayList<CSVRow>();
    private Calendar cal;
    private int cellAsu;
    private int cellDbm;
    private int cellLevel;
    private int wifiPerc;





    private Handler m_handler;
    private Runnable m_handlerTask;

    private class MyLocationListener implements LocationListener {

        public MyLocationListener(Context c) {
            checkLocationPermission(c);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }

        @Override
        public void onLocationChanged(Location location) {
//            longitude = location.getLongitude();
//            latitude = location.getLatitude();
//            loc = location;


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
                //Toast.makeText(this, "please connect to a wifi network! ", Toast.LENGTH_SHORT).show();
            }
        }else {
            wifi.setWifiEnabled(true);
        }
    }
    private void getCellSignal() {
        checkPhonePermission(context);
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
        if (allCellInfo != null && allCellInfo.size() != 0) {
//            CellInfo cellInfo = telephonyManager.getAllCellInfo().get(0);
//            CellInfoGsm cellinfogsm = (CellInfoGsm) cellInfo;
//            CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
//            CellSignalStrength cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
//            cellDbm = cellSignalStrengthGsm.getDbm();
//            cellAsu = cellSignalStrengthGsm.getAsuLevel();
//            cellLevel = cellSignalStrengthGsm.getLevel();
            CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
            cellDbm = cellSignalStrengthLte.getDbm();
            cellAsu = cellSignalStrengthLte.getAsuLevel();
            cellLevel = cellSignalStrengthLte.getLevel();
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
//            if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS){
//                try{
//                    checkLocationPermission(context);
//                    GpsStatus gpsStatus = lm.getGpsStatus(null);
//                    if(gpsStatus != null) {
//                        Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
//                        Iterator<GpsSatellite>sat = satellites.iterator();
//                        int i = 0;
//                        while (sat.hasNext()) {
//                            GpsSatellite satellite = sat.next();
//                            String lSatellites;
//                            lSatellites = "Satellite" + (i++) + ": "
//                                    + satellite.getPrn() + ","
//                                    + satellite.usedInFix() + ","
//                                    + satellite.getSnr() + ","
//                                    + satellite.getAzimuth() + ","
//                                    + satellite.getElevation()+ "\n\n";
//
//                            Log.d("SATELLITE",lSatellites);
//                        }
//                        numOfSatellites = i;
//                    }
//
//
//                }
//                catch(Exception ex){}
//            }
        }
    }


    @Override
    public void onCreate() {

        super.onCreate();
        m_handler = new Handler();
        m_handlerTask = new Runnable()
        {
            @Override
            public void run() {

                checkLocationPermission(context);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    numOfSatellites = location.getExtras().getInt("satellites") ;

                }

                loc = location;

                getWifiInfo();
                getCellSignal();
                CSVRow r = new CSVRow();
                r.timestamp = Calendar.getInstance().getTimeInMillis();
                System.out.println(UVIMeasurement(longitude, latitude));
                r.longitude = longitude;
                r.latitude = latitude;
                r.uvi = 12;
                r.numGPSSat = numOfSatellites;
                r.wifiPerc = wifiPerc;
                r.cellDbm = cellDbm;
                r.cellAsu = cellAsu;
                r.cellLevel = cellLevel;

                data.add(r);

                sendBroadcastMessage(loc);
                sendBroadcastMessage(numOfSatellites);
                
                m_handler.postDelayed(m_handlerTask, 3000);


            }
        };
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();
        checkLocationPermission(context);

        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            myGpsListener = new MyGpsListener(context);
            myLocationListener = new MyLocationListener(context);
            m_handlerTask.run();

        }
        return START_STICKY;
    }


    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
        super.onDestroy();
        m_handler.removeCallbacks(m_handlerTask);

        lm.removeUpdates(myLocationListener);
        lm.removeGpsStatusListener(myGpsListener);
        myLocationListener = null;
        myGpsListener = null;
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();

        csvManager.saveData(data);
    }


    private void sendBroadcastMessage(Location location) {
        if (location != null) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, location.getLatitude());
            intent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void sendBroadcastMessage(int count) {

        Intent intent = new Intent(ACTION_SATELLITES_BROADCAST);
        intent.putExtra(EXTRA_COUNT, count);
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

    private void checkPhonePermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED) {
            Log.e("first","error");
        }
        try {
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private String UVIMeasurement(double longitude, double latitude) {
        longitude = (double)Math.round(longitude * 100d) / 100d;
        latitude = (double)Math.round(latitude * 100d) / 100d;

        String address = "https://api.openuv.io/api/v1/uv?lat=" + String.valueOf(latitude) + "&lng=" + String.valueOf(longitude) + "&dt=2018-01-24T10%3A50%3A52.283Z";

        System.out.println(longitude);
        System.out.println(latitude);
        System.out.println(address);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(address)
                .get()
                .addHeader("x-access-token", "f3be6e8351172dcf678a2a8ef76ba7f0")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }



    }
}
