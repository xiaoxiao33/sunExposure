package com.example.xiaoxiaoouyang.sunexposure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Questions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
    }

    public void goToQuestionTwo(View view) {
        Intent intent = new Intent(this, QuestionsTwo.class);
        startActivity(intent);
    }
}
