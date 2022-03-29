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
    FirebaseFirestore db;

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


        db = FirebaseFirestore.getInstance();

//        double fromLat = fromT.latitude;
//        double fromLng = fromT.longitude;
//        String fromHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(fromLat, fromLng));
//        double toLat = toT.latitude;
//        double toLng = toT.longitude;
//        String toHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(toLat, toLng));
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("from geohash", fromHash);
//        updates.put("from lat", fromLat);
//        updates.put("from lng", fromLng);
//        updates.put("to geohash", toHash);
//        updates.put("to lat", toLat);
//        updates.put("to lng", toLng);
//        DocumentReference londonRef = db.collection("Trips").document("Zhzl5AVrJI2zn8NeQGHd");
//        londonRef.update(updates)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        // ...
//                    }
//                });



        final GeoLocation centerFrom = new GeoLocation(fromT.latitude, fromT.longitude);
        final GeoLocation centerTo = new GeoLocation(toT.latitude, toT.longitude);
        //List<DocumentSnapshot> docsFrom = getRadiusTrips(centerFrom,"from geohash", "from lat", "from lng");
        //List<DocumentSnapshot> docsTo = getRadiusTrips(centerTo,"to geohash", "to lat", "to lng");

        getRadiusTrips(centerFrom,"from geohash", "from lat", "from lng", inputDate);


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
                            //tripsList.add(new Trip(fromTrip,toTrip,map.get("name")));
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getRadiusTrips(GeoLocation center, String whereGeo, String whereLat, String whereLng, String inputDate){
        final double radiusInM = 50 * 1000;
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("Trips")
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
                            double lat = doc.getDouble(whereLat);
                            double lng = doc.getDouble(whereLng);
                            GeoLocation docLocation = new GeoLocation(lat, lng);
                            double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc);
                            }
                        }
                        // load docs
                        for (int i = 0; i < matchingDocs.size(); i++) {
                            System.out.println(matchingDocs.get(i).get("to"));
                            String fromS = (String) matchingDocs.get(i).get("from");
                            String toS = (String) matchingDocs.get(i).get("to");
                            matchingDocs.get(i).get("name");
                            HashMap<String, String> map = (HashMap<String, String>) matchingDocs.get(i).get("driver");
                            Timestamp timestamp = matchingDocs.get(i).getTimestamp("date");
                            LocalDateTime ldt = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault());
                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd - MMM - yyyy");
                            System.out.println(dateTimeFormatter.format(ldt));
                            if(inputDate.equalsIgnoreCase(dateTimeFormatter.format(ldt))){
                                tripsList.add(new Trip(fromS,toS,map.get("driver")));
                                tripAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });
    }


}