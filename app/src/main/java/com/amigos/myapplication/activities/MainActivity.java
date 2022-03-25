package com.amigos.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.amigos.myapplication.R;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseHelper.instance.isUserAuth()){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }else{
            FirebaseHelper.instance.getUserDataFromAuth();
            BottomNavigationView navigation = findViewById(R.id.bottomNavigationView);
            NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
            NavigationUI.setupWithNavController(navigation, navController);
        }
    }
}