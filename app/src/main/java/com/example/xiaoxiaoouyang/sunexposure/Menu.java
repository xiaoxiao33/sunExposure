package com.example.xiaoxiaoouyang.sunexposure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void goToDiagram(View view) {
        Intent intent = new Intent(this, ExposureDiagram.class);
        startActivity(intent);
    }

    public void goToQuestion(View view) {
        Intent intent = new Intent(this, Questions.class);
        startActivity(intent);
    }

}
