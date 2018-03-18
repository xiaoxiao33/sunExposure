package com.example.xiaoxiaoouyang.sunexposure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

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

    }

    public void goToMenu(View view) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void startCollecting(View view) {

        startService(new Intent(getBaseContext(), GPSService.class));

    }

    public void stopCollecting(View view) {
        stopService(new Intent(getBaseContext(), GPSService.class));
    }
}
