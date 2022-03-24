package com.amigos.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amigos.myapplication.models.Message;
import com.amigos.myapplication.R;

public class MessageRVAadapter extends ListAdapter<Message, MessageRVAadapter.ViewHolder> {
    public static Message taskSelected;
    private OnItemClickListener listener;

    public MessageRVAadapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(Message oldItem, Message newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Message oldItem, Message newItem) {
            return oldItem.getDriverName().equals(newItem.getDriverName());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message_cell, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message model = getTaskAt(position);
        holder.nameTV.setText(model.getDriverName());
        holder.directionTV.setText("Trip From " + model.getFrom() + " to " + model.getTo());
        holder.msgTV.setText(model.getLastMsg());
    }

    public Message getTaskAt(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV;
        TextView directionTV;
        TextView msgTV;
        ImageView imageV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.chatNameRV);
            directionTV = itemView.findViewById(R.id.chatRoutRV);
            msgTV = itemView.findViewById(R.id.chatMsgRV);
            imageV = itemView.findViewById(R.id.chatImageRV);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Context context = v.getContext();
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Message model);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
