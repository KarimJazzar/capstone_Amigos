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
import android.util.Log;
import android.widget.ListView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.TripAdapter;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Trip;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
//import com.google.type.LatLng;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    RecyclerView trips;
    ArrayList<Trip> tripsList;
    TripAdapter tripAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        String fromTrip = intent.getStringExtra("from");
        String toTrip = intent.getStringExtra("to");
        String inputDate = intent.getStringExtra("date");
        LatLng fromT = intent.getParcelableExtra("fromLatLong");
        LatLng toT = intent.getParcelableExtra("toLatLong");


        trips = findViewById(R.id.tripsResult);
        tripsList = new ArrayList<>();

        final GeoLocation centerFrom = new GeoLocation(fromT.latitude, fromT.longitude);
        final GeoLocation centerTo = new GeoLocation(toT.latitude, toT.longitude);
        //List<DocumentSnapshot> docsFrom = getRadiusTrips(centerFrom,"from geohash", "from lat", "from lng");
        //List<DocumentSnapshot> docsTo = getRadiusTrips(centerTo,"to geohash", "to lat", "to lng");

        getRadiusTrips(centerFrom,"fromGeohash",inputDate);


//        FirebaseHelper.instance.getDB().collection("Trips").whereEqualTo("from",fromTrip).whereEqualTo("to",toTrip).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()){
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        HashMap<String, String> map = (HashMap<String, String>) document.getData().get("driver");
//                        Timestamp timestamp = document.getTimestamp("date");
//                        LocalDateTime ldt = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault());
//                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd - MMM - yyyy");
//                        Log.e("ERROR", dateTimeFormatter.format(ldt));
//
//                        if(inputDate.equalsIgnoreCase(dateTimeFormatter.format(ldt))){
//                            //tripsList.add(new Trip(fromTrip,toTrip,map.get("name")));
//                            tripAdapter.notifyDataSetChanged();
//                        }
//                    }
//                }
//            }
//        });
        setAdapter();
    }

    private void setAdapter(){
        tripAdapter = new TripAdapter(tripsList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        trips.setLayoutManager(layoutManager);
        trips.setItemAnimator(new DefaultItemAnimator());
        trips.setAdapter(tripAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getRadiusTrips(GeoLocation center, String whereGeo, String inputDate){
        final double radiusInM = 50 * 1000;
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = FirebaseHelper.instance.getDB().collection("Trips")
                    .orderBy(whereGeo)
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }
        List<DocumentSnapshot> matchingDocs = new ArrayList<>();
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(t -> {
                    for (Task<QuerySnapshot> task : tasks) {
                        QuerySnapshot snap = task.getResult();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            Map<String, Double> map = new HashMap<>();
                            map = (Map<String, Double>) doc.get("fromPoints");
                            double lat = map.get("latitude");
                            double lng = map.get("longitude");
                            System.out.println(lat + " hello " + lng);
                            GeoLocation docLocation = new GeoLocation(lat, lng);
                            double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc);
                            }
                        }
                        // load docs
                        for (int i = 0; i < matchingDocs.size(); i++) {
                            String fromS = (String) matchingDocs.get(i).get("from");
                            String toS = (String) matchingDocs.get(i).get("to");
                            matchingDocs.get(i).get("name");

                            Timestamp timestamp = matchingDocs.get(i).getTimestamp("date");
                            LocalDateTime ldt = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault());
                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd - MMM - yyyy");
                            System.out.println(dateTimeFormatter.format(ldt));

                            if(inputDate.equalsIgnoreCase(dateTimeFormatter.format(ldt))){
                                Trip trip = matchingDocs.get(i).toObject(Trip.class);
                                tripsList.add(trip);
                                tripAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });
    }


}