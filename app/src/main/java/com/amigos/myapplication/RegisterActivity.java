package com.amigos.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;
    TextInputEditText etRegPassword2;
    TextInputEditText etRegFirst;
    TextInputEditText etRegLast;
    TextInputEditText etRegNumber;

    ImageView profPic;

    Button backBtutton;
    Button btnRegister;

    FirebaseAuth mAuth;
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    //DatabaseReference documentReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Uri imageUri;
    StorageReference storageReference;
    private FirebaseStorage storage;
    Boolean check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        check = false;

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        etRegEmail = findViewById(R.id.registerEmail);
        etRegPassword = findViewById(R.id.registerPassword);
        etRegPassword2 = findViewById(R.id.registerRepeatPassword);
        etRegFirst = findViewById(R.id.registerFirstName);
        etRegLast = findViewById(R.id.registerLastName);
        etRegNumber = findViewById(R.id.registerPhone);

        backBtutton = findViewById(R.id.registerBack);
        btnRegister = findViewById(R.id.btnRegister);
        profPic = findViewById(R.id.chatImageRV);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(view ->{
            createUser();

        });

        backBtutton.setOnClickListener(view ->{
            this.finish();
            //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        profPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            profPic.setImageURI(imageUri);
            check = true;
        }
    }

    private void uploadImage(String randomName) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Profile picture being uploaded");
        pd.show();


        StorageReference picsRef = storageReference.child("images/" + randomName);
        picsRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Percentage: " + (int) progressPercent + "%");
            }
        });

    }

    private void createUser(){
        String email = etRegEmail.getText().toString();
        String password = etRegPassword.getText().toString();
        String password2 = etRegPassword2.getText().toString();

        String firstName = etRegFirst.getText().toString();
        String lastName = etRegLast.getText().toString();
        String number = etRegNumber.getText().toString();

        if (TextUtils.isEmpty(email)){
            etRegEmail.setError("Email cannot be empty");
            etRegEmail.requestFocus();
        }else if (TextUtils.isEmpty(password)){
            etRegPassword.setError("Password cannot be empty");
            etRegPassword.requestFocus();
        }else if (!password.equals(password2)){
            etRegPassword.setError("Passwords are not the same");
            etRegPassword.requestFocus();
        }else if(!check){
            Toast.makeText(this, "Please upload a profile picture", Toast.LENGTH_SHORT).show();
        }else{
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String currentuid = task.getResult().getUser().getUid();
                        Map<String,String> details = new HashMap<>();
                        details.put("email", email);
                        details.put("number", number);
                        details.put("first name", firstName);
                        details.put("last name", lastName);
                        final String randomName = UUID.randomUUID().toString();
                        details.put("profile picture", randomName);
                        uploadImage(randomName);


                        db.collection("User Info").document(currentuid)
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

                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        finish();
                        //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    }else{
                        Toast.makeText(RegisterActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}