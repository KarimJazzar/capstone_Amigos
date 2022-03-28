package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.TripAdapter;
import com.amigos.myapplication.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {
    RecyclerView trips;
    ArrayList<Trip> tripsList;
    TripAdapter tripAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String fromTrip = intent.getStringExtra("from");
        String toTrip = intent.getStringExtra("to");
        String inputDate = intent.getStringExtra("date");

        trips = findViewById(R.id.tripsResult);
        tripsList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Trips").whereEqualTo("from",fromTrip).whereEqualTo("to",toTrip).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap<String, String> map = (HashMap<String, String>) document.getData().get("driver");
                        Timestamp timestamp = document.getTimestamp("date");
                        LocalDateTime ldt = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault());
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd - MMM - yyyy");
                        System.out.println(dateTimeFormatter.format(ldt));
                        if(inputDate.equalsIgnoreCase(dateTimeFormatter.format(ldt))){
                            tripsList.add(new Trip(fromTrip,toTrip,map.get("name")));
                            tripAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
        setAdapter();
    }

    private void setAdapter(){
        tripAdapter = new TripAdapter(tripsList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        trips.setLayoutManager(layoutManager);
        trips.setItemAnimator(new DefaultItemAnimator());
        trips.setAdapter(tripAdapter);
    }

}