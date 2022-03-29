package com.amigos.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.ConditionRVAdapter;
import com.amigos.myapplication.helpers.DateHelper;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Trip;

public class TripRideDetailActivity extends AppCompatActivity {

    private RecyclerView conditionRV, passengerRV;
    private ConditionRVAdapter conditionAdapter  = new ConditionRVAdapter();
    private TextView titleTV, driveTV, dateTV, timeTV, seatsTV, priceTV;
    private Button deleteBtn, backBtn;
    private ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_ride_detail);

        titleTV = findViewById(R.id.tripRideTitle);
        avatar = findViewById(R.id.tripRideDriverImg);
        driveTV = findViewById(R.id.triRideDriverName);
        dateTV = findViewById(R.id.tripRideDate);
        timeTV = findViewById(R.id.tripRideTime);
        seatsTV = findViewById(R.id.tripRideSeats);
        priceTV = findViewById(R.id.tripRidePrice);
        deleteBtn = findViewById(R.id.tripRideDelete);
        backBtn = findViewById(R.id.tripRideBack);
        conditionRV = findViewById(R.id.tripRideConditionRV);
        passengerRV = findViewById(R.id.tripRidePassengerRV);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            titleTV.setText(extras.getString("header"));

            Trip trip = (Trip) extras.getSerializable("trip_obj");

            FirebaseHelper.instance.setProfileImage(trip.getDriver().getProfilePicture(), avatar);
            driveTV.setText(trip.getDriver().getFirstName() + " " + trip.getDriver().getLastName());
            dateTV.setText(DateHelper.dateToString(trip.getDate()));
            timeTV.setText("Departure at " + trip.getTime());
            seatsTV.setText("Seats left " + trip.getSeats());
            priceTV.setText("CAD$" + trip.getPrice());

            conditionAdapter.submitList(trip.getConditions());
            conditionRV.setLayoutManager(new LinearLayoutManager(TripRideDetailActivity.this));
            conditionRV.setHasFixedSize(true);
            conditionRV.setAdapter(conditionAdapter);
        }

        backBtn.setOnClickListener(view ->{
            this.finish();
        });
    }
}