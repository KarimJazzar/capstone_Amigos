package com.amigos.myapplication.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amigos.myapplication.R;
import com.amigos.myapplication.activities.LoginActivity;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.helpers.UserHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import im.crisp.client.ChatActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private Button btnLogOut;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String,Object> details;
    private EditText firstName, lastName, number;
    private MaterialTextView email;
    private Button updateProf, btmHelp;
    private ImageView profPic;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userid = user.getUid();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        btnLogOut = view.findViewById(R.id.profileLogout);
        firstName = view.findViewById(R.id.profileFirstName);
        lastName = view.findViewById(R.id.profileLastName);
        email = view.findViewById(R.id.profileEmail);
        number = view.findViewById(R.id.profileNumber);
        updateProf = view.findViewById(R.id.updateProfile);
        profPic = view.findViewById(R.id.profileChatImageRV);
        btmHelp = view.findViewById(R.id.btnHelp);

        firstName.setText(UserHelper.user.getFirstName());
        lastName.setText(UserHelper.user.getLastName());
        email.setText(UserHelper.user.getEmail());
        number.setText(UserHelper.user.getPhoneNuber());
        FirebaseHelper.instance.setProfileImage(UserHelper.user.getProfilePicture(), profPic);
        //profPic.setImageBitmap(UserHelper.user.getProfilePicture());
        profPic.setImageURI(imageUri);

        updateProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> data1 = new HashMap<>();
                CollectionReference userInfo = db.collection("User Info");
                String firstN = firstName.getText().toString();
                String lastN = lastName.getText().toString();
                String numb = number.getText().toString();
                String mail = email.getText().toString();
                
                data1.put("first name", firstN);
                data1.put("last name", lastN);
                data1.put("number", numb);
                data1.put("email", mail);
                //String profilePicId = (String) details.get("profile picture");
                String profilePicId = UserHelper.user.getProfilePicture();

                if(imageUri != null){
                    StorageReference pathReference = storageRef.child("images/" + profilePicId);
                    pathReference.delete();
                    final String randomName = UUID.randomUUID().toString();
                    UserHelper.user.setProfilePicture(randomName);
                    data1.put("profile picture", randomName);
                    uploadImage(randomName);
                }else{
                    data1.put("profile picture", profilePicId);
                }

                userInfo.document(userid).set(data1);
                Toast.makeText(getContext(), "Profile info updated.", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                getActivity().finish();
                LoginActivity.canLogin = true;
                //startActivity(new Intent(getContext(), LoginActivity.class));
            }
        });

        profPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btmHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent to open the customer service chat activity
                Intent crispIntent = new Intent(getView().getContext(), ChatActivity.class);
                startActivity(crispIntent);
            }
        });
    }

    Uri imageUri;

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            profPic.setImageURI(imageUri);
        }
    }

    private void uploadImage(String randomName) {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Profile picture being uploaded");
        pd.show();


        StorageReference picsRef = storageRef.child("images/" + randomName);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

}