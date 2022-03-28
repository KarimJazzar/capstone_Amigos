package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.AutoCompleteAdapter;
import com.amigos.myapplication.helpers.DateTimeHelper;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.helpers.UserHelper;
import com.amigos.myapplication.models.Geopoint;
import com.amigos.myapplication.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateRideActivity extends AppCompatActivity  implements OnMapReadyCallback{

    MapView mapView;
    private Button createButton;
    private Button backBtutton;
    private ImageView tripLessSeat,tripMoreSeat;
    private EditText   inputPrice, inputSeats;
    private AutoCompleteTextView inputFrom,inputTo;
    private TextView inputDate,inputTime;

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


        createButton.setOnClickListener(view ->{
            Map<String,Object> details = new HashMap<>();
            details.put("from", "La Romana");
            Geopoint fromGP = new Geopoint();
            fromGP.setCoordinates(1.0,1.30);
            //Map<String,String> fromGP = new HashMap<>();
            details.put("from point", fromGP);
            details.put("to", "Santo Domingo");
            Geopoint toGP = new Geopoint();
            toGP.setCoordinates(0.0,0.4);
            details.put("to point", toGP);
            details.put("seats", 4);
            details.put("price", 0.00);
            details.put("date", 0.00);

            User driver = new User();
            driver.setEmail("");
            driver.setFirstName(UserHelper.user.getFullName());
            driver.setLastName(UserHelper.user.getPhoneNuber());
            driver.setPhoneNuber("");

            details.put("driver", driver);

            List<User> passengers = new ArrayList<>();
            details.put("passengers", passengers);

            List<String> conditions = new ArrayList<>();

            if(boxPet.isSelected()) {
                conditions.add("No Pets");
            }

            if(boxSmoke.isSelected()) {
                conditions.add("No Smoking");
            }

            if(boxDrink.isSelected()) {
                conditions.add("No Drinking");
            }

            if(boxEat.isSelected()) {
                conditions.add("No Eating");
            }

            details.put("restrictions", passengers);

            FirebaseHelper.instance.getDB().collection("Trips").document()
                    .set(details)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log.w(TAG, "Error writing document", e);
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

        boxPet = findViewById(R.id.createSelectPet);
        boxSmoke = findViewById(R.id.createSelectSmoke);
        boxDrink = findViewById(R.id.createSelectDrink);
        boxEat = findViewById(R.id.createSelectEat);

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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                Place place = Autocomplete.getPlaceFromIntent(data);
//                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                    return;
//                }
//                googleMap.setMyLocationEnabled(true);
//                // For zooming automatically to the location of the marker
//
//                if (googleMap != null) {
//                    googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//                        @Override
//                        public void onMyLocationChange(Location arg0) {
//                            googleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
//                            CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getLatLng()).zoom(12).build();
//                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//
////                            pName = place.getName();
////                            pAddrss = place.getAddress();
////                            pPlaceId = place.getId();
////                            pLat = place.getLatLng().latitude;
////                            pLong = place.getLatLng().longitude;
//
//
//                        }
//                    });
//                }
//            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
//                Status status = Autocomplete.getStatusFromIntent(data);
//            } else if (resultCode == RESULT_CANCELED) {
//                // The user canceled the operation.
//            }
//            return;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

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
}