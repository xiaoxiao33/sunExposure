package com.example.xiaoxiaoouyang.sunexposure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Questions3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions3);
    }

    public void goToQuestionFour(View view) {
        Intent intent = new Intent(this, Questions4.class);
        startActivity(intent);
    }
}
