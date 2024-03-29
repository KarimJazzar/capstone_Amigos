package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.ConditionRVAdapter;
import com.amigos.myapplication.adapters.PassengerRVAdapter;
import com.amigos.myapplication.helpers.DateHelper;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.helpers.Status;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Passenger;
import com.amigos.myapplication.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TripRideDetailActivity extends AppCompatActivity {

    private RecyclerView conditionRV, passengerRV;
    private ConditionRVAdapter conditionAdapter  = new ConditionRVAdapter();
    private PassengerRVAdapter passengerRVAdapter = new PassengerRVAdapter();
    private TextView titleTV, driveTV, dateTV, timeTV, seatsTV, priceTV, fromTV, toTV;
    private Button deleteBtn, backBtn;
    private ImageView avatar;
    private boolean isDriver;
    private String tripID;
    private Trip trip;

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
        fromTV = findViewById(R.id.tripRideFrom);
        toTV = findViewById(R.id.tripRideTo);
        conditionRV = findViewById(R.id.tripRideConditionRV);
        passengerRV = findViewById(R.id.tripRidePassengerRV);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            titleTV.setText(extras.getString("header"));
            tripID = extras.getString("trip_id");
            trip = (Trip) extras.getSerializable("trip_obj");
            updateView();
        }

        DocumentReference tripDoc = FirebaseHelper.instance.getDB().collection("Trips").document(tripID);

        tripDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null && value.exists()) {
                    trip = (Trip) value.toObject(Trip.class);
                    updateView();
                } else {
                    finish();
                }
            }
        });

        deleteBtn.setOnClickListener(view ->{
            if(isDriver) {
                CollectionReference chatRef = FirebaseHelper.instance.getDB().collection("Chat");
                Query yourChat =  chatRef.whereEqualTo("tripID", tripID);
                yourChat.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot documents = task.getResult();
                        for(DocumentSnapshot document : documents) {
                            FirebaseHelper.instance.getDB().collection("Chat").document(document.getId()).delete();
                        }
                    }
                });

                if(DateHelper.didDatePassed(DateHelper.dateToString(trip.getDate()))) {
                    trip.setStatus(Status.completed);
                } else {
                    trip.setStatus(Status.canceled);
                }

                trip.getUsers().set(0,"");
                FirebaseHelper.instance.getDB().collection("Trips").document(tripID).set(trip);
            } else {
                CollectionReference chatRef = FirebaseHelper.instance.getDB().collection("Chat");
                Query yourChat =  chatRef.whereArrayContains("users", FirebaseHelper.instance.getUserId()).whereEqualTo("tripID", tripID);
                yourChat.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot documents = task.getResult();
                        for(DocumentSnapshot document : documents) {
                            FirebaseHelper.instance.getDB().collection("Chat").document(document.getId()).delete();
                        }
                    }
                });

                Integer userIndex = trip.getUsers().indexOf(FirebaseHelper.instance.getUserId());

                List<String> newUserList = new ArrayList<>();
                List<Passenger> newPassengerList = new ArrayList<>();

                for (Integer i = 0; i < trip.getUsers().size(); i++) {
                    if(!trip.getUsers().get(i).equals(FirebaseHelper.instance.getUserId())) {
                        newUserList.add(trip.getUsers().get(i));
                    }
                }

                if(trip.getStatus().equals(Status.inprogress) && !DateHelper.didDatePassed(DateHelper.dateToString(trip.getDate()))) {
                    for(Integer i = 0; i < trip.getPassengers().size(); i++) {
                        if(!trip.getPassengers().get(i).getUserID().equals(FirebaseHelper.instance.getUserId())) {
                            newPassengerList.add(trip.getPassengers().get(i));
                        } else {
                            trip.setSeats(trip.getSeats() + trip.getPassengers().get(i).getSeats());
                        }
                    }

                    trip.setPassengers(newPassengerList);
                }

                trip.setUsers(newUserList);
                FirebaseHelper.instance.getDB().collection("Trips").document(tripID).set(trip);
            }

            this.finish();
        });

        backBtn.setOnClickListener(view ->{
            this.finish();
        });
    }

    private void updateView() {
        FirebaseHelper.instance.setProfileImage(trip.getDriver().getProfilePicture(), avatar);
        driveTV.setText(trip.getDriver().getFirstName() + " " + trip.getDriver().getLastName());
        dateTV.setText(DateHelper.dateToString(trip.getDate()));
        fromTV.setText("From " + trip.getFrom());
        toTV.setText("To " + trip.getTo());
        timeTV.setText("Departure at " + trip.getTime());
        seatsTV.setText("Seats left " + trip.getSeats());
        priceTV.setText("CAD$" + trip.getPrice());

        conditionAdapter.submitList(trip.getConditions());
        conditionRV.setLayoutManager(new LinearLayoutManager(TripRideDetailActivity.this));
        conditionRV.setHasFixedSize(true);
        conditionRV.setAdapter(conditionAdapter);

        passengerRVAdapter.submitList(trip.getPassengers());
        passengerRV.setLayoutManager(new LinearLayoutManager(TripRideDetailActivity.this));
        passengerRV.setHasFixedSize(true);
        passengerRV.setAdapter(passengerRVAdapter);

        if(trip.getUsers().get(0).equals(FirebaseHelper.instance.getUserId())) {
            isDriver = true;
            if(DateHelper.didDatePassed(DateHelper.dateToString(trip.getDate()))) {
                deleteBtn.setText("Complete Ride");
            } else {
                deleteBtn.setText("Cancel Ride");
            }
        } else {
            isDriver = false;
            if(!trip.getStatus().equals(Status.inprogress) || DateHelper.didDatePassed(DateHelper.dateToString(trip.getDate()))) {
                deleteBtn.setText("Close Trip");
            } else {
                deleteBtn.setText("Cancel Trip");
            }
        }
    }
}