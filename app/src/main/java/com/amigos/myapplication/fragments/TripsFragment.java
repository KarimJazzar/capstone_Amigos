package com.amigos.myapplication.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.ChatRVAadapter;
import com.amigos.myapplication.adapters.TripRidesRVAdapter;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripsFragment extends Fragment {

    private RecyclerView tripsRV;
    private List<Trip> tripList = new ArrayList<>();
    private TripRidesRVAdapter tripAdapter = new TripRidesRVAdapter();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TripsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TripsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripsFragment newInstance(String param1, String param2) {
        TripsFragment fragment = new TripsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getActivity().getApplicationContext();

        tripsRV = (RecyclerView) view.findViewById(R.id.tripsRidesRV);
        tripsRV.setLayoutManager(new LinearLayoutManager(context));
        tripsRV.setHasFixedSize(true);
        tripsRV.setAdapter(tripAdapter);

        String id = FirebaseHelper.instance.getUserId();

        CollectionReference chatRef = FirebaseHelper.instance.getDB().collection("Trips");
        Query yourChat =  chatRef.whereArrayContains("users", id);

        Log.e("ERROR", "" + FirebaseHelper.instance.getUserId());

        yourChat.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                tripList.clear();

                QuerySnapshot documents = task.getResult();
                for(DocumentSnapshot document : documents) {
                    tripList.add(document.toObject(Trip.class));
                    Log.e("ERROR", "" + tripList);
                }

                tripAdapter.submitList(tripList);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trips, container, false);
    }
}