package com.amigos.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class ForgotActivity extends AppCompatActivity {

    private Button backBtutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        backBtutton = findViewById(R.id.resetBack);

        backBtutton.setOnClickListener(view ->{
            this.finish();
        });
    }
}