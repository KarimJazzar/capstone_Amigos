package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amigos.myapplication.R;
import com.amigos.myapplication.helpers.AlertDialogHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotActivity extends AppCompatActivity {

    private Button backBtutton, resetPassword;
    private LinearLayout formLL, sentLL;
    private boolean canClick = true;
    FirebaseAuth firebaseAuth;
    EditText resetMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        formLL = findViewById(R.id.prResetContainer);
        sentLL = findViewById(R.id.prSentContainer);

        backBtutton = findViewById(R.id.resetBack);
        resetPassword = findViewById(R.id.btnResetPass);
        resetMail = findViewById(R.id.resetEmailt);
        firebaseAuth = FirebaseAuth.getInstance();

        resetMail.setText("");

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(resetMail.getText().toString().equals("")) {
                    AlertDialogHelper.show(ForgotActivity.this, "Invalid Email", "Plase insert a valid email.");
                    return;
                }

                if(!canClick) { return; }

                canClick = false;

                String mail = resetMail.getText().toString();
                firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        formLL.setVisibility(View.GONE);
                        sentLL.setVisibility(View.VISIBLE);
                        Toast.makeText(ForgotActivity.this, "Reset Link Has Been Sent To Your Email.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        canClick = true;
                        Toast.makeText(ForgotActivity.this, "Error! Reset Link was not sent. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        backBtutton.setOnClickListener(view ->{
            this.finish();
        });
    }
}