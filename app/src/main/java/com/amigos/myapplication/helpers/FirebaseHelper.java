package com.amigos.myapplication.helpers;

import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;

public class FirebaseHelper {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    public static final FirebaseHelper instance = new FirebaseHelper();

    public FirebaseFirestore getDB() {
        return db;
    }

    public StorageReference getStorageRef() {
        return storageReference;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public boolean isUserAuth() {
        return user == null;
    }

    public String getUserId() {
        return user.getUid();
    }

    public void getUserDataFromAuth() {
        getUserData(user.getUid());
    }

    public void getUserData(String userid) {
        DocumentReference docRef = db.collection("User Info").document(userid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String,Object> details = document.getData();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                    UserHelper.user.setFirstName((String) details.get("first name"));
                    UserHelper.user.setLastName((String) details.get("last name"));
                    UserHelper.user.setEmail((String) details.get("email"));
                    UserHelper.user.setPhoneNuber((String) details.get("number"));


                    String profilePicId = (String) details.get("profile picture");
                    StorageReference pathReference = storageRef.child("images/" + profilePicId);

                    final File localFile;
                    try {
                        localFile = File.createTempFile(profilePicId,"jpg");
                        pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                UserHelper.user.setProfilePicture(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
