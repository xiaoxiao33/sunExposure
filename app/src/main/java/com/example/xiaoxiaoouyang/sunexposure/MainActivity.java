package com.example.xiaoxiaoouyang.sunexposure;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    Context ctx;

    Intent mSensorIntent;
    private GPSService mGPSService;

    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

//        Intent AR397 = new Intent(ctx, AlarmReceiverHealth.class);
//        PendingIntent recurringAR397 = PendingIntent.getBroadcast(this, 0, AR397, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarms.setRepeating(AlarmManager.RTC_WAKEUP, 0, 60000, recurringAR397);
        setContentView(R.layout.activity_main);

        mGPSService = new GPSService();
        mSensorIntent = new Intent(this, mGPSService.getClass());

        final TextView textView = (TextView) findViewById(R.id.textView8);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double latitude = intent.getDoubleExtra(GPSService.EXTRA_LATITUDE, 0);
                        double longitude = intent.getDoubleExtra(GPSService.EXTRA_LONGITUDE, 0);
                        textView.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                    }
                }, new IntentFilter(GPSService.ACTION_LOCATION_BROADCAST)
        );


        final TextView textView2 = (TextView) findViewById(R.id.textView9);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double index = intent.getDoubleExtra(GPSService.EXTRA_UVI, 0);
                        textView2.setText(index + "");
                    }
                }, new IntentFilter(GPSService.ACTION_UVI_BROADCAST)
        );

        final TextView textView3 = (TextView) findViewById(R.id.textView11);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int index = intent.getIntExtra(GPSService.EXTRA_COUNT, 0);
                        textView3.setText("Number of Satellites: " + index);
                    }
                }, new IntentFilter(GPSService.ACTION_SATELLITES_BROADCAST)
        );

        if (!isMyServiceRunning(mGPSService.getClass())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(ctx, GPSService.class));
            } else {
                startService(new Intent(ctx, GPSService.class));
            }
        }}


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    public void goToMenu(View view) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void startCollecting(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(ctx, GPSService.class));
        } else {
            startService(new Intent(ctx, GPSService.class));
        }
    }



//    private void checkTime(boolean value) {
//
//        Date d=new Date();
//        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm");
//        String currentDateTimeString = sdf.format(d);
//        currentDateTimeString = currentDateTimeString.replace(":", "");
//        int DATE_PICKER = 0600;
//        int DATE_PICKER2 = 2100;
//
//        int currentDateTimeInt = Integer.parseInt(currentDateTimeString);
//        System.out.println("current Date Time is: " + currentDateTimeString);
//
//        if (currentDateTimeInt >= DATE_PICKER && currentDateTimeInt <= DATE_PICKER2 && value == false) {
//            startService(new Intent(getBaseContext(), GPSService.class));
//            System.out.println("we got here once");
//            value = true;
//        }
//        else {
//            stopService(new Intent(getBaseContext(), GPSService.class));
//            System.out.println("we got here for some fking reason");
//            value = false;
//        }}

    public void stopCollecting(View view) {
        stopService(new Intent(getBaseContext(), GPSService.class));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(getBaseContext(), GPSService.class));
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    public void loadActivity() {
        startService(new Intent(getBaseContext(), GPSService.class));
    }}

