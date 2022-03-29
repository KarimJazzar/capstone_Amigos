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
import com.amigos.myapplication.activities.TripRideDetailActivity;
import com.amigos.myapplication.helpers.DateHelper;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Trip;

import java.io.Serializable;

public class TripRidesRVAdapter extends ListAdapter<Trip, TripRidesRVAdapter.ViewHolder> {
    private TripRidesRVAdapter.OnItemClickListener listener;

    public TripRidesRVAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Trip> DIFF_CALLBACK = new DiffUtil.ItemCallback<Trip>() {
        @Override
        public boolean areItemsTheSame(Trip oldItem, Trip newItem) {
            return oldItem.getUsers().get(0) == newItem.getUsers().get(0);
        }

        @Override
        public boolean areContentsTheSame(Trip oldItem, Trip newItem) {
            return oldItem.getDriver().getFirstName().equals(newItem.getDriver().getFirstName());
        }
    };

    @NonNull
    @Override
    public TripRidesRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_trips_rides_cell, parent, false);
        return new TripRidesRVAdapter.ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull TripRidesRVAdapter.ViewHolder holder, int position) {
        Trip model = getTripAt(position);
        String direction = "Trip from " + model.getFrom() + " to " + model.getTo();
        String date = "" + DateHelper.dateToString(model.getDate()) + " " + model.getTime();

        if (FirebaseHelper.instance.getUserId().equals(model.getUsers().get(0))) {
            holder.typeTV.setText("RIDE");
        } else {
            holder.typeTV.setText("TRIP");
        }
        holder.dateTV.setText(date);
        holder.destinationTV.setText(direction);
    }

    public Trip getTripAt(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTV;
        TextView destinationTV;
        TextView dateTV;
        TextView moreDetailBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTV = itemView.findViewById(R.id.tripRideType);
            destinationTV = itemView.findViewById(R.id.tripRideDestination);
            dateTV = itemView.findViewById(R.id.tripRideDate);
            moreDetailBtn = itemView.findViewById(R.id.tripRideMore);

            moreDetailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Context context = v.getContext();
                    Intent intent = new Intent(context, TripRideDetailActivity.class);
                    intent.putExtra("header", typeTV.getText().toString());
                    intent.putExtra("trip_obj", getTripAt(position));
                    context.startActivity(intent);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Trip model);
    }

    public void setOnItemClickListener(TripRidesRVAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
