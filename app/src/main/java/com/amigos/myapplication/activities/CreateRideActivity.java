package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.AutoCompleteAdapter;
import com.amigos.myapplication.helpers.AlertDialogHelper;
import com.amigos.myapplication.helpers.DateHelper;
import com.amigos.myapplication.helpers.DateTimeHelper;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.helpers.UserHelper;
import com.amigos.myapplication.models.Geopoint;
import com.amigos.myapplication.models.Trip;
import com.amigos.myapplication.models.User;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateRideActivity extends AppCompatActivity  implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    MapView mapView;
    private Button createButton;
    private Button backBtutton;
    private ImageView tripLessSeat,tripMoreSeat;
    private EditText inputPrice ;
    private TextView inputSeats, inputDate, inputTime;
    private AutoCompleteTextView inputFrom,inputTo;
    private CheckBox boxPet, boxSmoke, boxDrink, boxEat;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap googleMap;
    private static int AUTOCOMPLETE_REQUEST_CODE = 100;
    private final String TAG = this.getClass().getName();

    PlacesClient placesClient;
    AutoCompleteAdapter adapter,adapter1;

    private String fName,fAddress,fPlaceID,fDate;
    private String tName,tAddress,tPlaceID,tDate;
    private Double fLat,fLong;
    private Double tLat,tLong;
    protected LatLng start=null;
    protected LatLng end=null;
    private List<Polyline> polylines=null;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ride);


        initialize(); // initializing all views

        checkLocationPermission();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_geo_api_key));
        }
        placesClient = Places.createClient(this);



        inputDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                DateTimeHelper.selectDate(inputDate,CreateRideActivity.this);
            }
        });

        inputTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeHelper.selectTime(inputTime,CreateRideActivity.this);

            }
        });

        createButton.setOnClickListener(view ->{
            Trip trip = new Trip();

            if(fName == null || fName.equals("")){
                AlertDialogHelper.show(CreateRideActivity.this, "Invalid From", "Plase select a valid direction.");
                return;
            } else {
                trip.setFrom(fName);

                Geopoint fromGP = new Geopoint();
                fromGP.setLatitude(fLat);
                fromGP.setLongitude(fLong);
                trip.setFromPoints(fromGP);
            }

            if(tName == null || tName.equals("")) {
                AlertDialogHelper.show(CreateRideActivity.this, "Invalid To", "Plase select a valid direction.");
                return;
            } else {
                trip.setTo(tName);

                Geopoint toGP = new Geopoint();
                toGP.setLatitude(tLat);
                toGP.setLongitude(tLong);
                trip.setToPoints(toGP);
            }

            try {
                int seats = Integer.parseInt(inputSeats.getText().toString());
                double price = Double.parseDouble(inputPrice.getText().toString());

                if (seats == 0) {
                    AlertDialogHelper.show(CreateRideActivity.this, "Invalid Seat", "Please insert a valid amount.");
                    return;
                } else {
                    trip.setSeats(seats);
                }

                if (price == 0) {
                    AlertDialogHelper.show(CreateRideActivity.this, "Invalid Price", "Please insert a valid amount.");
                    return;
                } else {
                    trip.setPrice(price);
                }
            } catch (NumberFormatException error) {
                return;
            }

            List<User> passengers = new ArrayList<>();
            trip.setPassengers(passengers);

            User driver = UserHelper.user;
            trip.setDriver(driver);

            List<String> users = new ArrayList<>();
            users.add(FirebaseHelper.instance.getUserId());
            trip.setUsers(users);

            List<String> conditions = new ArrayList<>();

            if(boxPet.isChecked()) {
                conditions.add("No Pets");
            }

            if(boxSmoke.isChecked()) {
                conditions.add("No Smoking");
            }

            if(boxDrink.isChecked()) {
                conditions.add("No Drinking");
            }

            if(boxEat.isChecked()) {
                conditions.add("No Eating");
            }

            if(conditions.size() <= 0) {
                conditions.add("No restrictions.");
            }

            trip.setConditions(conditions);

            trip.setTime(inputTime.getText().toString());

            try {
                trip.setDate(DateHelper.stringToDate(inputDate.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            FirebaseHelper.instance.getDB().collection("Trips").document().set(trip)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                    inputFrom.setText("");
                    inputTo.setText("");
                    inputSeats.setText("0");
                    inputPrice.setText("");
                    boxPet.setChecked(false);
                    boxSmoke.setChecked(false);
                    boxDrink.setChecked(false);
                    boxEat.setChecked(false);

                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar snackbar= Snackbar.make(parentLayout,"Ride Created", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        });

        backBtutton.setOnClickListener(view ->{
            this.finish();
        });

        checkLocationPermission();

        Places.initialize(getApplicationContext(), getString(R.string.google_geo_api_key));

        mapView.onCreate(savedInstanceState);

        mapView.onResume();

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
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
                            googleMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("Your Location"));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(arg0.getLatitude(), arg0.getLongitude())).zoom(12).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        }
                    });
                }
            }
        });


        inputFrom.setOnItemClickListener(autocompleteClickListener);
        adapter = new AutoCompleteAdapter(this, placesClient);
        inputFrom.setAdapter(adapter);

        inputTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inputFrom.getText().toString().isEmpty()) {
                    inputTo.setOnItemClickListener(autocompleteClickListenerTo);
                    adapter1 = new AutoCompleteAdapter(CreateRideActivity.this, placesClient);
                    inputTo.setAdapter(adapter1);
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar snackbar= Snackbar.make(parentLayout, "Add Start Point", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        tripLessSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!inputSeats.getText().toString().equals("")){
                    if(!inputSeats.getText().toString().equals("0")) {
                        inputSeats.setText(String.valueOf(Integer.parseInt(inputSeats.getText().toString()) - 1));
                    }
                }
            }
        });
        tripMoreSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputSeats.getText().toString()=="") {
                    inputSeats.setText(""+1);
                }else {
                    if(!inputSeats.getText().toString().equals("8")) {
                        inputSeats.setText(String.valueOf(Integer.parseInt(inputSeats.getText().toString())+1));
                    }
                }
            }
        });
    }

    private AdapterView.OnItemClickListener autocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            try {
                final AutocompletePrediction item = adapter.getItem(i);
                String placeID = null;
                if (item != null) {
                    placeID = item.getPlaceId();
                }

//                To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
//                Use only those fields which are required.

                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

                FetchPlaceRequest request = null;
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields).build();
                }

                if (request != null) {
                    placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @SuppressLint({"SetTextI18n", "MissingPermission"})
                        @Override
                        public void onSuccess(FetchPlaceResponse task) {
                            Log.e(TAG,"from click");

                            inputTo.requestFocus();
                            //InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            googleMap.setMyLocationEnabled(true);
                            // For zooming automatically to the location of the marker

                            if (googleMap != null) {

                                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                                    @Override
                                    public void onMyLocationChange(Location arg0) {

//                                        if(fromClick) {


                                        googleMap.addMarker(new MarkerOptions().position(task.getPlace().getLatLng()).title(task.getPlace().getName()));
                                        CameraPosition cameraPosition = new CameraPosition.Builder().target(task.getPlace().getLatLng()).zoom(12).build();
                                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                        fName = task.getPlace().getName();
                                        fAddress = task.getPlace().getAddress();
                                        fPlaceID = task.getPlace().getId();
                                        fLat = task.getPlace().getLatLng().latitude;
                                        fLong = task.getPlace().getLatLng().longitude;
                                        inputFrom.setText(fName);

                                    }
                                });
                            }
                            // The user canceled the operation.

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
//                            responseView.setText(e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private AdapterView.OnItemClickListener autocompleteClickListenerTo = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            try {
                final AutocompletePrediction item = adapter1.getItem(i);
                String placeID = null;
                if (item != null) {
                    placeID = item.getPlaceId();
                }

//                To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
//                Use only those fields which are required.

                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS
                        , Place.Field.LAT_LNG);

                FetchPlaceRequest request = null;
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields)
                            .build();
                }

                if (request != null) {

                    placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @SuppressLint({"SetTextI18n", "MissingPermission"})
                        @Override
                        public void onSuccess(FetchPlaceResponse task) {

                            inputTo.clearFocus();// MainActivity is the name of the class and v is the View parameter used in the button listener method onClick.

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            googleMap.setMyLocationEnabled(true);
                            // For zooming automatically to the location of the marker

                            if (googleMap != null) {

                                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                                    @Override
                                    public void onMyLocationChange(Location arg0) {

//
//                                            googleMap.addMarker(new MarkerOptions().position(task.getPlace().getLatLng()).title(task.getPlace().getName()));
//                                            CameraPosition cameraPosition1 = new CameraPosition.Builder().target(task.getPlace().getLatLng()).zoom(12).build();
//                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));


                                        tName = task.getPlace().getName();
                                        tAddress = task.getPlace().getAddress();
                                        tPlaceID = task.getPlace().getId();
                                        tLat = task.getPlace().getLatLng().latitude;
                                        tLong = task.getPlace().getLatLng().longitude;
                                        inputTo.setText(tName);

                                        start = new LatLng(fLat,fLong);
                                        end = new LatLng(tLat,tLong);

                                        Findroutes(start,end);
                                    }
                                });
                            }
                            // The user canceled the operation.

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
//                            responseView.setText(e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End) {

        if(Start==null || End==null) {
            Toast.makeText(CreateRideActivity.this,"Unable to get location", Toast.LENGTH_LONG).show();
        }
        else
        {

            Log.v(TAG," st" + Start+" end"+ end);

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

    private void initialize() {

        mapView = findViewById(R.id.mapView);
        createButton = findViewById(R.id.btnCreateRide);
        backBtutton = findViewById(R.id.createRideBack);

        inputFrom = findViewById(R.id.createFrom);
        inputTo = findViewById(R.id.createTo);
        inputDate = findViewById(R.id.createDate);
        inputTime = findViewById(R.id.createTime);
        inputPrice = findViewById(R.id.createPrice);
        inputSeats = findViewById(R.id.createSeats);
        tripLessSeat = findViewById(R.id.tripLessSeat);
        tripMoreSeat = findViewById(R.id.tripMoreSeat);

        boxPet = findViewById(R.id.createSelectPet);
        boxSmoke = findViewById(R.id.createSelectSmoke);
        boxDrink = findViewById(R.id.createSelectDrink);
        boxEat = findViewById(R.id.createSelectEat);

        inputDate.setText(DateHelper.getCurrentDateAsString());
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
                                ActivityCompat.requestPermissions(CreateRideActivity.this,
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

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onStart();
        }else {

            Toast.makeText(CreateRideActivity.this,"Allow Location Permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onStart();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onStart();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onStart();
        }    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onStart();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
//        View parentLayout = findViewById(android.R.id.content);
//        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
//        snackbar.show();
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

            if(i==shortestRouteIndex)
            {
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
        Log.e(TAG,"cancelled");
        Findroutes(start,end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"failed");

        Findroutes(start,end);

    }
}