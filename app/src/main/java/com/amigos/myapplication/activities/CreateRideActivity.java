package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.amigos.myapplication.R;
import com.amigos.myapplication.models.Geopoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateRideActivity extends AppCompatActivity  implements OnMapReadyCallback{

    MapView mapView;
    private Button createButton;
    private Button backBtutton;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap googleMap;
    private static int AUTOCOMPLETE_REQUEST_CODE = 100;
    private final String TAG = this.getClass().getName();


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ride);

        createButton = findViewById(R.id.btnCreateRide);
        backBtutton = findViewById(R.id.createRideBack);

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

            db.collection("Trips").document()
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

        initialize();
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