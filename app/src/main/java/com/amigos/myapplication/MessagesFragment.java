package com.amigos.myapplication;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagesFragment extends Fragment {

    private RecyclerView messagesRV;
    private List<Message> messages = new ArrayList<>();
    private MessageRVAadapter messagesAdapter = new MessageRVAadapter();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MessagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagesFragment newInstance(String param1, String param2) {
        MessagesFragment fragment = new MessagesFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getActivity().getApplicationContext();

        Message m1 = new Message();
        m1.setDriverName("Jose Miolan");
        m1.setFrom("Santo Domingo");
        m1.setTo("Samana");
        m1.setLastMsg("Are you ready for the trip?");

        Message m2 = new Message();
        m2.setDriverName("Alla Ponomarenko");
        m2.setFrom("Santo Domingo");
        m2.setTo("Santiago");
        m2.setLastMsg("Almost there!");

        Message m3 = new Message();
        m3.setDriverName("Thom Solo");
        m3.setFrom("La Romana");
        m3.setTo("Punta Cana");
        m3.setLastMsg("Hello, is possible...");

        messages.add(m1);
        messages.add(m2);
        messages.add(m3);

        messagesAdapter.submitList(messages);

        messagesRV = (RecyclerView) view.findViewById(R.id.tripsRidesRV);
        messagesRV.setLayoutManager(new LinearLayoutManager(context));
        messagesRV.setHasFixedSize(true);
        messagesRV.setAdapter(messagesAdapter);

    }
}