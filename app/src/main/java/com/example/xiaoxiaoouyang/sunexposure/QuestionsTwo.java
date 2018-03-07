package com.example.xiaoxiaoouyang.sunexposure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

public class QuestionsTwo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_two);
    }

    public void goToQuestionThree(View view) {
        Intent intent = new Intent(this, Questions3.class);
        startActivity(intent);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId()) {
            case R.id.time7_1:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.time8_1:
                if (checked)
                    // Ninjas rule
                    break;
        }
    }
}
