package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.TripAdapter;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Trip;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleMap googleMap;
    private MapView resultMV;
    private RecyclerView trips;
    private TextView noResultTV;
    private ArrayList<Trip> tripsList;
    private TripAdapter tripAdapter;
    private Button resultBack;
    private LatLng fromT,toT;
    private List<Polyline> polylines = null;
    public static Integer passengerNumber;
    public static List<String> tripIDs = new ArrayList<>();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ArrayList<Trip> tempList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        String seats = intent.getStringExtra("seats");
        String inputDate = intent.getStringExtra("date");
        fromT = intent.getParcelableExtra("fromLatLong");
        toT = intent.getParcelableExtra("toLatLong");

        passengerNumber = Integer.parseInt(seats);

        trips = findViewById(R.id.tripsResult);

        resultMV = findViewById(R.id.resultMV);
        checkLocationPermission();

        noResultTV = findViewById(R.id.noResult);

        resultBack = findViewById(R.id.resultBack);

        resultBack.setOnClickListener(view ->{
            this.finish();
        });

        tripsList = new ArrayList<>();
        tempList = new ArrayList<>();

        final GeoLocation centerFrom = new GeoLocation(fromT.latitude, fromT.longitude);
        final GeoLocation centerTo = new GeoLocation(toT.latitude, toT.longitude);
        //List<DocumentSnapshot> docsFrom = getRadiusTrips(centerFrom,"from geohash", "from lat", "from lng");
        //List<DocumentSnapshot> docsTo = getRadiusTrips(centerTo,"to geohash", "to lat", "to lng");

        FirebaseHelper.instance.getDB().collection("Trips").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                getRadiusTrips(centerFrom,"fromGeohash",inputDate, seats);
                if(!tripsList.isEmpty()){
                    System.out.println("hello world " + tripsList);
                }

            }
        });

        setAdapter();

        resultMV.onCreate(savedInstanceState);

        resultMV.onResume();

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        resultMV.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                googleMap.setMyLocationEnabled(true);
                // For zooming automatically to the location of the marker

                if (mMap != null) {

                    mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location arg0) {
                            Findroutes(fromT,toT);

                        }
                    });
                }
            }
        });


    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Requesting Location Permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(ResultActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void Findroutes(LatLng Start, LatLng End) {

        if(Start==null || End==null) {
            Toast.makeText(ResultActivity.this,"Unable to get location", Toast.LENGTH_LONG).show();
        }
        else {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key(getString(R.string.google_geo_api_key))
                    .build();
            routing.execute();
        }
    }


    private void setAdapter(){
        tripAdapter = new TripAdapter(tripsList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        trips.setLayoutManager(layoutManager);
        trips.setItemAnimator(new DefaultItemAnimator());
        trips.setAdapter(tripAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getRadiusTrips(GeoLocation center, String whereGeo, String inputDate, String seats){

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
        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
            if(tasks.isEmpty()) {
                noResultTV.setVisibility(View.VISIBLE);
                return;
            }

            noResultTV.setVisibility(View.GONE);

            for (Task<QuerySnapshot> task : tasks) {
                QuerySnapshot snap = task.getResult();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    Map<String, Double> map = new HashMap<>();
                    map = (Map<String, Double>) doc.get("fromPoints");
                    double lat = map.get("latitude");
                    double lng = map.get("longitude");

                    GeoLocation docLocation = new GeoLocation(lat, lng);
                    double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                    if (distanceInM <= radiusInM) {
                        matchingDocs.add(doc);
                    }
                }


                // load docs
                tripsList.clear();
                tripIDs.clear();
                noResultTV.setVisibility(View.VISIBLE);
                for (int i = 0; i < matchingDocs.size(); i++) {

                    Timestamp timestamp = matchingDocs.get(i).getTimestamp("date");
                    LocalDateTime ldt = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault());
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd - MMM - yyyy");
                    //System.out.println(dateTimeFormatter.format(ldt));
                    //System.out.println(matchingDocs.get(i).toString() + " dio");

                    StringBuilder stringBuilder = new StringBuilder(inputDate);
                    if(inputDate.substring(1,2).equalsIgnoreCase(" ")){
                        stringBuilder.insert(0,"0");
                    }

                    StringBuilder stringBuilder1 = new StringBuilder(dateTimeFormatter.format(ldt));

                    if(stringBuilder.toString().equalsIgnoreCase(stringBuilder1.toString())){
                        Trip trip = matchingDocs.get(i).toObject(Trip.class);
                        if(!tripsList.contains(trip) && Integer.parseInt(seats) <= trip.getSeats() && trip.getStatus().equals("inprogress")){
                            noResultTV.setVisibility(View.GONE);
                            tripIDs.add(matchingDocs.get(i).getId());
                            tripsList.add(trip);
                        }

                        tripAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resultMV.onStart();
        }else {

            Toast.makeText(ResultActivity.this,"Allow Location Permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resultMV.onStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resultMV.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resultMV.onStart();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resultMV.onStart();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resultMV.onStart();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resultMV.onStart();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Findroutes(fromT,toT);
    }

    @Override
    public void onRoutingStart() {
//
//        View parentLayout = findViewById(android.R.id.content);
//        Snackbar snackbar= Snackbar.make(parentLayout, "Finding Route...", Snackbar.LENGTH_LONG);
//        snackbar.show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines!=null) {
            polylines.clear();
        }

        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {
            if(i==shortestRouteIndex) {
                polyOptions.color(getResources().getColor(R.color.brown));
                polyOptions.width(12);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());

                Polyline polyline = googleMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);
            }
            else {

            }
        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        googleMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        googleMap.addMarker(endMarker);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(polylineStartLatLng);
        builder.include(polylineEndLatLng);
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        googleMap.animateCamera(cu);

    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(fromT,toT);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}