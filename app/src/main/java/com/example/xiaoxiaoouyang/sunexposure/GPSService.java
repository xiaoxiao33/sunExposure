package com.example.xiaoxiaoouyang.sunexposure;


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
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.lang.*;
import java.util.Map;
//import android.telephony.CellInfo;
import org.json.JSONException;
import org.json.JSONObject;

//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;



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
//    private TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

    private LocationManager lm;
    private CSVManager csvManager = new CSVManager();
    ArrayList<CSVRow> data = new ArrayList<CSVRow>();
    private Calendar cal;
    private int cellAsu;
    private int cellDbm;
    private int cellLevel;
    private int wifiPerc;


//    public int getCellSignal() {
//        checkPermission(context);
//        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
//        //This will give info of all sims present inside your mobile
//        int strength = -1;
//        if (cellInfos != null){
//            for (int i = 0 ; i<cellInfos.size(); i++){
//                if (cellInfos.get(i).isRegistered()){
//                    if(cellInfos.get(i) instanceof CellInfoWcdma){
//                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
//                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
//                        strength = cellSignalStrengthWcdma.getDbm();
//                    } else if(cellInfos.get(i) instanceof CellInfoGsm){
//                        CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
//                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
//                        strength = cellSignalStrengthGsm.getDbm();
//                    } else if(cellInfos.get(i) instanceof CellInfoLte){
//                        CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
//                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
//                        strength = cellSignalStrengthLte.getDbm();
//                    }
//                }
//            }
//            return strength;
//        }
//        return strength;
//    }




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

    private void getwifiinfo(){
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if(wifi.isWifiEnabled()){
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            if(String.valueOf(wifiInfo.getSupplicantState()).equals("COMPLETED")){
                Toast.makeText(this, wifiInfo.getSSID()+"", Toast.LENGTH_SHORT).show();
                int rssi = wifiInfo.getRssi();
                int level = WifiManager.calculateSignalLevel(rssi, 10);
                wifiPerc = (int) ((level/10.0)*100);
//                Log.v("wifi", "perc:" + String.valueOf(percentage));
            }else{
                Toast.makeText(this, "please connect to a wifi network! ", Toast.LENGTH_SHORT).show();
            }
        }else {
            wifi.setWifiEnabled(true);
        }
    }
    private void cellsignal(){
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
        CellSignalStrength cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
        cellDbm = cellSignalStrengthGsm.getDbm();
        cellAsu = cellSignalStrengthGsm.getAsuLevel();
        cellLevel = cellSignalStrengthGsm.getLevel();
//        Log.v("Signal", "dbm:" + String.valueOf(dbm));
//        Log.v("Signal", "asu:" + String.valueOf(asuLevel));
//        Log.v("Signal", "level:" + String.valueOf(level));
    }


    @Override
    public void onCreate() {

        super.onCreate();
        m_handler = new Handler();
        m_handlerTask = new Runnable()
        {
            @Override
            public void run() {
//                System.out.println("The longitude: ");
//                System.out.println(longitude);
//                System.out.println("The Latitude");
//                System.out.println(latitude);
//                System.out.println("Satellites number: ");
//                System.out.println(numOfSatellites);

                getwifiinfo();
                cellsignal();
                CSVRow r = new CSVRow();
                System.out.println(UVIMeasurement(longitude, latitude));
                r.timestamp = Calendar.getInstance().getTimeInMillis();
                r.longitude = longitude;
                r.latitude = latitude;
                r.uvi = 12;
                r.numGPSSat = numOfSatellites;
                r.wifiPerc = wifiPerc;
                r.cellDbm = cellDbm;
                r.cellAsu = cellAsu;
                r.cellLevel = cellLevel;

                data.add(r);

                m_handler.postDelayed(m_handlerTask, 3000);

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


    private void checkPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            Log.e("first","error");
        }
        try {
            lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    private String UVIMeasurement(double longitude, double latitude) {
//        longitude = location.getLongitude();
//        latitude = location.getLatitude();

//        NumberFormat formatter = new DecimalFormat("#0.000");
//
//        longitude = Integer.parseInt(longitude);
//
//
//        longitude = formatter.format(longitude);
//        latitude = Integer.toString(formatter.format(latitude));

        longitude = (double)Math.round(longitude * 100d) / 100d;
        latitude = (double)Math.round(latitude * 100d) / 100d;

        String address = "https://api.openuv.io/api/v1/uv?lat=" + String.valueOf(latitude) + "&lng=" + String.valueOf(longitude) + "&dt=2018-01-24T10%3A50%3A52.283Z";

        System.out.println(longitude);
        System.out.println(latitude);
        System.out.println(address);
//        Context mContext = getApplicationContext();
//        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, address, null, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        System.out.println("Response: " + response.toString());
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO: Handle error
//                        System.out.println("EEEERORRRRR");
//                        error.printStackTrace();
//
//                    }
//                });
//
//        requestQueue.add(jsonObjectRequest);

//        return "aaa";

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



}}
