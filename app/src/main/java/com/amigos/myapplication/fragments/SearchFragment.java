package com.amigos.myapplication.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.activities.CreateRideActivity;
import com.amigos.myapplication.activities.ResultActivity;
import com.amigos.myapplication.adapters.AutoCompleteAdapter;
import com.amigos.myapplication.helpers.DateTimeHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {


    AutoCompleteTextView fromTrip,toTrip;
    private static int AUTOCOMPLETE_REQUEST_CODE = 100;
    private final String TAG = this.getClass().getName();

    PlacesClient placesClient;
    AutoCompleteAdapter adapter,adapter1;
    private Double fLat,fLong;
    private Double tLat,tLong;

    ImageView searchLessBtn,searchMoreBtn;
    TextView txtSeats;

    private String fName,fAddress,fPlaceID,fDate;
    private String tName,tAddress,tPlaceID,tDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView openCreateRide = view.findViewById(R.id.openCreateRideBtn);
        TextView dateTV = view.findViewById(R.id.searchDate);
        Button findTrip = view.findViewById(R.id.btnFind);

        fromTrip = view.findViewById(R.id.searchFrom);
        toTrip = view.findViewById(R.id.searchTo);
        searchLessBtn = view.findViewById(R.id.searchLessBtn);
        searchMoreBtn = view.findViewById(R.id.searchMoreBtn);
        txtSeats = view.findViewById(R.id.txtSeats);
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.google_geo_api_key));
        }
        placesClient = Places.createClient(getContext());


//        inputFrom.setOnItemClickListener(autocompleteClickListener);
//        adapter = new AutoCompleteAdapter(this, placesClient);
//        inputFrom.setAdapter(adapter);
        dateTV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                DateTimeHelper.selectDate(dateTV,getContext());
            }
        });


        openCreateRide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), CreateRideActivity.class);
                startActivity(intent);
            }
        });



        fromTrip.setOnItemClickListener(autocompleteClickListener);
        adapter = new AutoCompleteAdapter(getContext(), placesClient);
        fromTrip.setAdapter(adapter);


        toTrip.setOnItemClickListener(autocompleteClickListenerTo);
        adapter1 = new AutoCompleteAdapter(getContext(), placesClient);
        toTrip.setAdapter(adapter1);


        searchLessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!txtSeats.getText().toString().equals("")){
                    if(!txtSeats.getText().toString().equals("0")) {
                        txtSeats.setText(String.valueOf(Integer.parseInt(txtSeats.getText().toString()) - 1));
                    }
                }
            }
        });

        searchMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtSeats.getText().toString()=="") {

                    txtSeats.setText(""+1);
                }else {
                    txtSeats.setText(String.valueOf(Integer.parseInt(txtSeats.getText().toString())+1));

                }
            }
        });

        findTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String from = fName;
                String to = tName;
                String inputDate = dateTV.getText().toString();
                Intent intent = new Intent(getActivity(), ResultActivity.class);
                intent.putExtra("fromLatLong", new LatLng(fLat,fLong));
                intent.putExtra("toLatLong", new LatLng(tLat,tLong));
                intent.putExtra("date", inputDate);
                startActivity(intent);

            }
        });

        super.onViewCreated(view, savedInstanceState);
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
                            Log.e(TAG,"from click");

                            toTrip.requestFocus();




//                                        if(fromClick) {



                                        fName = task.getPlace().getName();
                                        fAddress = task.getPlace().getAddress();
                                        fPlaceID = task.getPlace().getId();
                                        fLat = task.getPlace().getLatLng().latitude;
                                        fLong = task.getPlace().getLatLng().longitude;
                                        fromTrip.setText(fName);


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

                            toTrip.clearFocus();// MainActivity is the name of the class and v is the View parameter used in the button listener method onClick.


                            tName = task.getPlace().getName();
                            tAddress = task.getPlace().getAddress();
                            tPlaceID = task.getPlace().getId();
                            tLat = task.getPlace().getLatLng().latitude;
                            tLong = task.getPlace().getLatLng().longitude;
                            toTrip.setText(tName);


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


}