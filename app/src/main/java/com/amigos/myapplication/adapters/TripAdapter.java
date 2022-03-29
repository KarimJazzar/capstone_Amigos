package com.amigos.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.models.Trip;

import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.MyViewHolder> {
    private ArrayList<Trip> data=new ArrayList<>();

    public TripAdapter(ArrayList<Trip>data)
    {
        this.data=data;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView driver;
        private TextView from;
        private TextView to;

        public MyViewHolder(@NonNull View view) {
            super(view);
            //driver =view.findViewById(R.id.driverName);
            //from =view.findViewById(R.id.tripFrom);
            //to = view.findViewById(R.id.tripTo);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_trips_rides_cell,parent,false);
            return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //holder.driver.setText(data.get(position).getDriver());
        holder.from.setText(String.valueOf(data.get(position).getFrom()));
        holder.to.setText(String.valueOf(data.get(position).getTo()));
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

}
