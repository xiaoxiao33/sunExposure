package com.example.xiaoxiaoouyang.sunexposure;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
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
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;


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

    private CSVManager csvManager = new CSVManager();
    ArrayList<CSVRow> data = new ArrayList<CSVRow>();




    private Handler m_handler;
    private Runnable m_handlerTask;

    private class MyLocationListener implements LocationListener {

        public MyLocationListener(Context c) {
            checkLocationPermission(c);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }

        @Override
        public void onLocationChanged(Location location) {
//            sendBroadcastMessage(location);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            loc = location;

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
                Toast.makeText(this, wifiInfo.getSSID()+"", Toast.LENGTH_SHORT).show();
                int rssi = wifiInfo.getRssi();
                int level = WifiManager.calculateSignalLevel(rssi, 10);
                int percentage = (int) ((level/10.0)*100);
                Log.v("wifi", "perc:" + String.valueOf(percentage));
            }else{
                Toast.makeText(this, "please connect to a wifi network! ", Toast.LENGTH_SHORT).show();
            }
        }else {
            wifi.setWifiEnabled(true);
        }
    }
    private void getCellSignal() {
        checkPhonePermission(context);
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
        if (allCellInfo != null && allCellInfo.size() != 0) {
            CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
            CellSignalStrength cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
            int dbm = cellSignalStrengthGsm.getDbm();
            int asuLevel = cellSignalStrengthGsm.getAsuLevel();
            int level = cellSignalStrengthGsm.getLevel();
            Log.v("Signal", "dbm:" + String.valueOf(dbm));
            Log.v("Signal", "asu:" + String.valueOf(asuLevel));
            Log.v("Signal", "level:" + String.valueOf(level));
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
            if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS){
                try{
                    checkLocationPermission(context);
                    GpsStatus gpsStatus = lm.getGpsStatus(null);
                    if(gpsStatus != null) {
                        Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
                        Iterator<GpsSatellite>sat = satellites.iterator();
                        int i = 0;
                        while (sat.hasNext()) {
                            GpsSatellite satellite = sat.next();
                            String lSatellites;
                            lSatellites = "Satellite" + (i++) + ": "
                                    + satellite.getPrn() + ","
                                    + satellite.usedInFix() + ","
                                    + satellite.getSnr() + ","
                                    + satellite.getAzimuth() + ","
                                    + satellite.getElevation()+ "\n\n";

                            Log.d("SATELLITE",lSatellites);
                        }
                        numOfSatellites = i;
                    }


                }
                catch(Exception ex){}
            }
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
                System.out.println("The longitude: ");
                System.out.println(longitude);
                System.out.println("The Latitude");
                System.out.println(latitude);
                System.out.println("Satellites number: ");
                System.out.println(numOfSatellites);
                System.out.println("Cell signal:");
                getCellSignal();
                getWifiInfo();


                sendBroadcastMessage(numOfSatellites);
                sendBroadcastMessage(loc);


                m_handler.postDelayed(m_handlerTask, 1000);

                CSVRow r = new CSVRow();
                r.timestamp = 0; // CHANGE
                r.longitude = longitude;
                r.latitude = latitude;
                r.uvi = 12;
                r.numGPSSat = 5;
                data.add(r);

                m_handler.postDelayed(m_handlerTask, 3000);


            }
        };
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();
        checkLocationPermission(context);

        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
            CSVRow r = new CSVRow();
            r.timestamp = 0; // CHANGE
            r.longitude = location.getLongitude();
            r.latitude = location.getLatitude();
            r.uvi = 12;
            r.numGPSSat = 5;
            data.add(r);
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
}
