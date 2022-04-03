package com.amigos.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.activities.MessagesActivity;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Passenger;

public class PassengerRVAdapter extends ListAdapter<Passenger, PassengerRVAdapter.ViewHolder> {
    private PassengerRVAdapter.OnItemClickListener listener;

    public PassengerRVAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Passenger> DIFF_CALLBACK = new DiffUtil.ItemCallback<Passenger>() {
        @Override
        public boolean areItemsTheSame(Passenger oldItem, Passenger newItem) {
            return oldItem.getUserId().equals(newItem.getUserId());
        }

        @Override
        public boolean areContentsTheSame(Passenger oldItem, Passenger newItem) {
            return oldItem.getUserId().equals(newItem.getUserId());
        }
    };

    @NonNull
    @Override
    public PassengerRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_passenger_cell, parent, false);
        return new PassengerRVAdapter.ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerRVAdapter.ViewHolder holder, int position) {
        Passenger model = getPassengerAt(position);
        holder.nameTV.setText(model.getName());
        String seatStr = model.getSeats() == 1 ? " seat." : " seats.";
        holder.seatsTV.setText(model.getSeats() + seatStr);
        FirebaseHelper.instance.setProfileImage(model.getProfilePic(), holder.imageV);
    }

    public Passenger getPassengerAt(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV;
        TextView seatsTV;
        ImageView imageV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.passengerNameRV);
            seatsTV = itemView.findViewById(R.id.passengerSeatsRV);
            imageV = itemView.findViewById(R.id.passengerPicRV);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Chat model);
    }

    public void setOnItemClickListener(PassengerRVAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
