package com.amigos.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Message;

import java.util.List;

public class MessageRVAdapter extends RecyclerView.Adapter {

    List<Message> msgList;
    String senderID = FirebaseHelper.instance.getUserId();

    public MessageRVAdapter (List<Message> messages) {
        msgList = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if(msgList.get(position).getSender().equals(senderID)) {
            return 0;
        }
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        if(viewType == 0) {
            view = layoutInflater.inflate(R.layout.row_message_cell_v2, parent,false);
            return new ViewHolderOne(view);
        }

        view = layoutInflater.inflate(R.layout.row_message_cell_v1, parent, false);
        return new ViewHolderTwo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String text = msgList.get(position).getText();

        if (msgList.get(position).getSender().equals(senderID)) {
            ViewHolderTwo viewH = (ViewHolderTwo) holder;
            viewH.msgTV.setText(text);
        } else {
            ViewHolderOne viewH = (ViewHolderOne) holder;
            viewH.msgTV.setText(text);
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    class ViewHolderOne extends RecyclerView.ViewHolder {
        TextView msgTV;

        public ViewHolderOne(@NonNull View itemView) {
            super(itemView);
            msgTV = itemView.findViewById(R.id.messageText);
        }
    }

    class ViewHolderTwo extends RecyclerView.ViewHolder {
        TextView msgTV;

        public ViewHolderTwo(@NonNull View itemView) {
            super(itemView);
            msgTV = itemView.findViewById(R.id.messageText);
        }
    }
}
