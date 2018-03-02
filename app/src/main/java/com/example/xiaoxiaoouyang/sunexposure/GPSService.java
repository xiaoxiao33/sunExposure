package com.example.xiaoxiaoouyang.sunexposure;


import android.app.Service;
import android.content.Intent;

import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;


public class GPSService extends Service {
    /** indicates how to behave if the service is killed */
    int mStartMode;

    /** interface for clients that bind */
    IBinder mBinder;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    GPSProvider mGPSProvider;

    Handler m_handler;
    Runnable m_handlerTask;

    DataManager dataManager;

    @Override
    public void onCreate() {
        mGPSProvider = new GPSProvider(getBaseContext());
        dataManager = new DataManager(getBaseContext());

        m_handler = new Handler();
        m_handlerTask = new Runnable()
        {
            @Override
            public void run() {
                int size = mGPSProvider.getGPSStatus();
                System.out.println(size);
                Location loc = mGPSProvider.getLocation();
                dataManager.addData(DateFormat.getDateTimeInstance().format(new Date()), loc.getLongitude(), loc.getAltitude());


                m_handler.postDelayed(m_handlerTask, 3000);

            }
        };
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m_handlerTask.run();

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

        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

}
