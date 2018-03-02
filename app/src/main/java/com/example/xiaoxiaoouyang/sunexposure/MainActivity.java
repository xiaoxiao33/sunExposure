package com.example.xiaoxiaoouyang.sunexposure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
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
