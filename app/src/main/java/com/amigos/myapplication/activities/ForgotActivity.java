package com.amigos.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.amigos.myapplication.R;

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