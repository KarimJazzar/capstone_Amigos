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
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private RecyclerView chatRV;
    private List<Chat> chatList = new ArrayList<>();
    private ChatRVAadapter messagesAdapter = new ChatRVAadapter();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
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
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    private List<Chat> chats = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getActivity().getApplicationContext();

        //messages.add(m1);
        //messagesAdapter.submitList(messages);

        chatRV = (RecyclerView) view.findViewById(R.id.tripsRidesRV);
        chatRV.setLayoutManager(new LinearLayoutManager(context));
        chatRV.setHasFixedSize(true);
        chatRV.setAdapter(messagesAdapter);

        String id = FirebaseHelper.instance.getUserId();

        CollectionReference chatRef = FirebaseHelper.instance.getDB().collection("Chat");
        Query yourChat =  chatRef.whereArrayContains("users", id);

        yourChat.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot documents = task.getResult();
                for(DocumentSnapshot document : documents) {
                    chats.add(document.toObject(Chat.class));
                    Log.e("CHECK", " " + document.toString());
                }

                chatList = chats;
                messagesAdapter.submitList(chatList);

                Log.e("CHECK", " " + chats.get(0).getDriver());
                Log.e("CHECK", " " + chats.get(0).getPassenger());
                Log.e("CHECK", " " + chats.get(0).getMessages().get(0));
            }
        });

        //yourChat.get()

        Message msg = new Message();
        Date date = new Date();
        msg.setDate(date);
        msg.setSender("Daniel Miolan");
        msg.setText("Hey there!");

        List<Message> msgList = new ArrayList<>();
        msgList.add(msg);

        Map<String,Object> details = new HashMap<>();
        details.put("", msg);

        //FirebaseHelper.instance.getDB().collection("Messages").document("1").set(details);
        //FirebaseHelper.instance.getDB().collection("Messages").document("1").update("messages", FieldValue.arrayUnion(msg));

        DocumentReference docRef = FirebaseHelper.instance.getDB().collection("Messages").document("1");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    System.err.println("Listen failed: " + error);
                    return;
                }

                if (value != null && value.exists()) {
                    System.out.println("Current data: " + value.getData());
                } else {
                    System.out.print("Current data: null");
                }
            }
        });

        //Task<QuerySnapshot> docRef = FirebaseHelper.instance.getDB().collectionGroup("Chat").whereEqualTo("driver.id", id).get();
        //Log.e("CHECK", "" + docRef.toString());

    }
}