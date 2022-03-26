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

import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Message;
import com.amigos.myapplication.R;

public class ChatRVAadapter extends ListAdapter<Chat, ChatRVAadapter.ViewHolder> {
    public static Chat chatSelected;
    private OnItemClickListener listener;

    public ChatRVAadapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Chat> DIFF_CALLBACK = new DiffUtil.ItemCallback<Chat>() {
        @Override
        public boolean areItemsTheSame(Chat oldItem, Chat newItem) {
            return oldItem.getDriver() == newItem.getDriver();
        }

        @Override
        public boolean areContentsTheSame(Chat oldItem, Chat newItem) {
            return oldItem.getDriver().equals(newItem.getDriver());
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
        Chat model = getTaskAt(position);

        if(FirebaseHelper.instance.getUserId().equals(model.getUsers().get(0))) {
            holder.nameTV.setText(model.getPassenger());
        } else {
            holder.nameTV.setText(model.getDriver());
        }
        //holder.directionTV.setText("Trip From " + model.getFrom() + " to " + model.getTo());
        //holder.msgTV.setText(model.getLastMsg());
    }

    public Chat getTaskAt(int position) {
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
        void onItemClick(Chat model);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
