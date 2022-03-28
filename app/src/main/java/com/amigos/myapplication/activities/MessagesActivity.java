package com.amigos.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.adapters.MessageRVAdapter;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Message;
import com.amigos.myapplication.models.MessageResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesActivity extends AppCompatActivity {
    private List<Message> messages = new ArrayList<>();
    private RecyclerView messagesRV;
    private EditText msgTV;
    private Button sendBtn, backBtutton;
    private TextView receiverName;
    private Message msg = new Message();
    private List<Message> msgList = new ArrayList<>();
    private String msgDocumentID;
    private MessageRVAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        messagesAdapter = new MessageRVAdapter(msgList);

        messagesRV = findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(MessagesActivity.this);
        llm.setReverseLayout(true);
        messagesRV.setLayoutManager(llm);
        messagesRV.setHasFixedSize(true);
        messagesRV.setAdapter(messagesAdapter);

        msgTV = findViewById(R.id.messageText);
        sendBtn = findViewById(R.id.messageSendBtn);
        backBtutton = findViewById(R.id.messageBack);
        receiverName = findViewById(R.id.messageSenderName);


        backBtutton.setOnClickListener(view ->{
            this.finish();
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            msgDocumentID = extras.getString("msg_id");
            receiverName.setText(extras.getString("msg_name"));
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgStr = msgTV.getText().toString();

                if(msgStr.equals("")) {
                    return;
                }
                msg.setDate(new Date());
                msg.setSender(FirebaseHelper.instance.getUserId());
                msg.setText(msgStr);

                Map<String,Object> details = new HashMap<>();
                details.put("", msg);

                msgList.add(msg);

                //FirebaseHelper.instance.getDB().collection("Messages").document(msgDocumentID).set(details);
                FirebaseHelper.instance.getDB().collection("Messages").document(msgDocumentID).update("messages", FieldValue.arrayUnion(msg));

                msgTV.setText("");
                messagesRV.smoothScrollToPosition(msgList.size() - 1);
            }
        });

        DocumentReference docRef = FirebaseHelper.instance.getDB().collection("Messages").document(msgDocumentID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documents = task.getResult();
                if (task.isSuccessful()) {
                    MessageResponse tempStruc = documents.toObject(MessageResponse.class);
                    formatMessagesResult(tempStruc);
                }
            }
        });

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("ERROR", "Listen failed: " + error);
                    return;
                }

                if (value != null && value.exists()) {
                    MessageResponse tempStruc = value.toObject(MessageResponse.class);
                    formatMessagesResult(tempStruc);
                    Log.e("ERROR","Current data: " + value.getData());
                } else {
                    Log.e("ERROR","Current data: null");
                }
            }
        });
    }

    private void formatMessagesResult(MessageResponse tempStruc) {
        msgList.clear();

        for (Message tempMsg : tempStruc.getMessages()) {
            msgList.add(0, tempMsg);
        }

        messagesAdapter.notifyDataSetChanged();
        messagesRV.smoothScrollToPosition(0);
    }
}