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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;


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

    public static final String
            ACTION_LOCATION_BROADCAST = GPSService.class.getName() + "LocationBroadcast",
            EXTRA_LATITUDE = "extra_latitude",
            EXTRA_LONGITUDE = "extra_longitude",
            ACTION_SATELLITES_BROADCAST = GPSService.class.getName() + "SatellitesBroadcast",
            EXTRA_COUNT = "extra_count";




    private MyGpsListener myGpsListener;

    private MyLocationListener myLocationListener;
//    private TelephonyManager telephonyManager;

    private LocationManager lm;
    private Location loc;


    private Handler m_handler;
    private Runnable m_handlerTask;

    private class MyLocationListener implements LocationListener {

        public MyLocationListener(Context c) {
            checkPermission(c);
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


    private class MyGpsListener implements GpsStatus.Listener {

        public MyGpsListener(Context c) {
            checkPermission(c);
            lm.addGpsStatusListener(myGpsListener);
            System.out.println("GPSstatus Listener attached");
        }

        @Override
        public void onGpsStatusChanged(int event){
            if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS){
                try{
                    checkPermission(context);
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
//                        sendBroadcastMessage(i);
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
                sendBroadcastMessage(numOfSatellites);
                sendBroadcastMessage(loc);


                m_handler.postDelayed(m_handlerTask, 1000);

            }
        };
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();
        checkPermission(context);

        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            myGpsListener = new MyGpsListener(context);
            myLocationListener = new MyLocationListener(context);
            m_handlerTask.run();

//            int signal = getCellSignal();
//            System.out.print("The Cell signal is" + String.valueOf(signal));

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


    private void checkPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
         Log.e("first","error");
        }
        try {
            lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            //telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
