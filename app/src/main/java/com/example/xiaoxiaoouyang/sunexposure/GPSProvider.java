package com.example.xiaoxiaoouyang.sunexposure;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class GPSProvider implements LocationListener {

    Context context;
    GpsStatus.Listener mGPSListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(final int event) {
        }

    };

    public GPSProvider(Context c) {
        context = c;
    }

    public Location getLocation(){
        if (ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            Log.e("fist","error");
            return null;
        }
        try {
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled){
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000,10,this);
                Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return loc;
            }else{
                Log.e("sec","errpr");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public int getGPSStatus() {
        context.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("first");
            return -1;
        }
        try {
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled){
                lm.addGpsStatusListener(mGPSListener);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000,10,this);
                GpsStatus gpsStatus = lm.getGpsStatus(null);
                Iterable<GpsSatellite> arr = gpsStatus.getSatellites();
                List<GpsSatellite> l = new ArrayList<>();
                for (GpsSatellite g : arr) {
                    l.add(g);
                }
                return l.size();

            }else{
                System.out.println("sedcond");
                Log.e("sec","errpr");
            }
        }catch (Exception e){
            System.out.println("Exception");
            e.printStackTrace();
        }
        return -1;

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

