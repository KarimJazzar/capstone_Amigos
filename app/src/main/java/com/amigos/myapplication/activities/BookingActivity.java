package com.amigos.myapplication.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigos.myapplication.configurations.PaypalClientIDConfigClass;
import com.amigos.myapplication.R;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Trip;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    private Button btnBook, bookingBack;
    private TextView nameTV, timeTV, seatTV, priceTV;
    private ImageView bookingImage;
    private List<TextView> conditionTV = new ArrayList<>();
    private double testAmount = 78.60;
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
        bookingImage = findViewById(R.id.bookingImage);

        conditionTV.add(findViewById(R.id.bookingCondition1));
        conditionTV.add(findViewById(R.id.bookingCondition2));
        conditionTV.add(findViewById(R.id.bookingCondition3));
        conditionTV.add(findViewById(R.id.bookingCondition4));

        btnBook = findViewById(R.id.btnBook);
        bookingBack = findViewById(R.id.bookingBack);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trip = (Trip) extras.getSerializable("trip_obj");

            nameTV.setText("Trip with " + trip.getDriver().getFirstName());
            timeTV.setText("Departure time " + trip.getTime());
            seatTV.setText(trip.getSeats() + " open seats");
            priceTV.setText("" + trip.getPrice());

            FirebaseHelper.instance.setProfileImage(trip.getDriver().getProfilePicture(), bookingImage);

            int i = 0;
            for(String condition : trip.getConditions()) {
                conditionTV.get(i).setText(condition);
                i++;
            }
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

    //Method to open the payment screen
    private void PaypalpaymentsMethod() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(testAmount), "CAD"
                , "Test Payment", PayPalPayment.PAYMENT_INTENT_SALE);

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