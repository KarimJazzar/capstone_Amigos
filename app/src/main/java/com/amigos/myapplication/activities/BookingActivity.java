package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigos.myapplication.configurations.PaypalClientIDConfigClass;
import com.amigos.myapplication.R;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.helpers.UserHelper;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Message;
import com.amigos.myapplication.models.Passenger;
import com.amigos.myapplication.models.Trip;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BookingActivity extends AppCompatActivity {
    private String tripId;
    private Button btnBook, bookingBack;
    private TextView nameTV, timeTV, seatTV, priceTV, thanksTV;
    private ImageView bookingImage;
    private List<TextView> conditionTV = new ArrayList<>();
    private double totalPrice = 00.00;
    private String reciptDescription = "";
    private int PAYPAL_REQ_CODE = 12;
    private Trip trip;
    private static PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PaypalClientIDConfigClass.PAYPAL_CLIENT_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        nameTV = findViewById(R.id.bookingName);
        timeTV = findViewById(R.id.bookingTime);
        seatTV = findViewById(R.id.bookingSeats);
        priceTV = findViewById(R.id.bookingPrice);
        thanksTV = findViewById(R.id.bookingThanks);
        bookingImage = findViewById(R.id.bookingImage);

        conditionTV.add(findViewById(R.id.bookingCondition1));
        conditionTV.add(findViewById(R.id.bookingCondition2));
        conditionTV.add(findViewById(R.id.bookingCondition3));
        conditionTV.add(findViewById(R.id.bookingCondition4));

        btnBook = findViewById(R.id.btnBook);
        bookingBack = findViewById(R.id.bookingBack);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tripId = extras.getString("trip_id");
            trip = (Trip) extras.getSerializable("trip_obj");

            DocumentReference tripRef = FirebaseHelper.instance.getDB().collection("Trips").document(tripId);

            tripRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null && value.exists()) {
                        trip = (Trip) value.toObject(Trip.class);
                        updateView();
                    }
                }
            });

            if (trip.getUsers().get(0).equals(FirebaseHelper.instance.getUserId())) {
                btnBook.setVisibility(View.GONE);
            } else {
                for(Passenger p: trip.getPassengers()) {
                    if(p.getUserID().equals(FirebaseHelper.instance.getUserId())) {
                        btnBook.setVisibility(View.GONE);
                        thanksTV.setText("Already Booked");
                        thanksTV.setVisibility(View.VISIBLE);
                    }
                }
            }

            updateView();
        }

        //Intent to start the paypal service and configure basic settings
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        startService(intent);

        //Onclick method to run the method
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaypalpaymentsMethod();
            }
        });

        bookingBack.setOnClickListener(view ->{
            this.finish();
        });
    }

    private void updateView() {
        nameTV.setText("Trip with " + trip.getDriver().getFirstName());
        timeTV.setText("Departure time " + trip.getTime());
        seatTV.setText(trip.getSeats() + " open seats");
        priceTV.setText("" + trip.getPrice());

        totalPrice = trip.getPrice() * ResultActivity.passengerNumber;
        if (ResultActivity.passengerNumber == 1) {
            reciptDescription= "Rent 1 seat.";
        } else {
            reciptDescription = "Rent " + ResultActivity.passengerNumber + " seats.";
        }

        FirebaseHelper.instance.setProfileImage(trip.getDriver().getProfilePicture(), bookingImage);

        int i = 0;
        for(String condition : trip.getConditions()) {
            conditionTV.get(i).setText(condition);
            i++;
        }
    }

    //Method to open the payment screen
    private void PaypalpaymentsMethod() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(totalPrice), "CAD", reciptDescription, PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starts next screen and receives back the result
        startActivityForResult(intent, PAYPAL_REQ_CODE);
    }


    //Method to receive the payment status from the payment screen and display the relative toast
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYPAL_REQ_CODE){

            if(resultCode == Activity.RESULT_OK){
                Passenger passenger = new Passenger();
                passenger.setUserID(FirebaseHelper.instance.getUserId());
                passenger.setName(UserHelper.getUserFullname());
                passenger.setSeats(ResultActivity.passengerNumber);
                passenger.setProfilePic(UserHelper.user.getProfilePicture());

                trip.getPassengers().add(passenger);
                trip.getUsers().add(FirebaseHelper.instance.getUserId());
                trip.setSeats(trip.getSeats() - ResultActivity.passengerNumber);

                FirebaseHelper.instance.getDB().collection("Trips").document(tripId).set(trip);


                Chat chat = new Chat();
                List<String> users = new ArrayList<>();
                final String randomName = UUID.randomUUID().toString();

                users.add(trip.getUsers().get(0));
                users.add(FirebaseHelper.instance.getUserId());
                chat.setUsers(users);

                chat.setDriver(trip.getDriver().getFirstName() + " " + trip.getDriver().getLastName());
                chat.setDriverAvatar(trip.getDriver().getProfilePicture());
                chat.setPassenger(UserHelper.getUserFullname());
                chat.setPassengerAvatar(UserHelper.user.getProfilePicture());
                chat.setFrom(trip.getFrom());
                chat.setTo(trip.getTo());
                chat.setMsgID(randomName);
                chat.setTripID(tripId);

                Message msg = new Message();
                chat.setMessages(msg);

                FirebaseHelper.instance.getDB().collection("Chat").document(randomName)
                .set(chat)
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


                List<Message> messages = new ArrayList<>();
                Map<String,Object> msgMap = new HashMap<>();
                msgMap.put("messages", messages);

                FirebaseHelper.instance.getDB().collection("Messages").document(randomName)
                .set(msgMap)
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

                btnBook.setVisibility(View.GONE);
                thanksTV.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Payment Made Successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Payment Unsuccessful, \n Please Try Again", Toast.LENGTH_LONG).show();
            }
        }

    }

    //Destroys the paypal service after finishing the task
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

}