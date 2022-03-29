package com.amigos.myapplication.adapters;

import android.annotation.SuppressLint;
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

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.MyViewHolder> {
    private ArrayList<Trip> data=new ArrayList<>();

    public TripAdapter(ArrayList<Trip>data)
    {
        this.data=data;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView driverName;
        private TextView date;
        private TextView price;
        private TextView seats;

        public MyViewHolder(@NonNull View view) {
            super(view);
            driverName = view.findViewById(R.id.chatNameRV);
            date = view.findViewById(R.id.chatMsgRV);
            price = view.findViewById(R.id.chatNameRV2);
            seats = view.findViewById(R.id.chatMsgRV2);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_result_cell,parent,false);
            return new MyViewHolder(itemView);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.driverName.setText(data.get(position).getDriver().getFirstName() + " " + data.get(position).getDriver().getLastName());
        holder.date.setText(data.get(position).getDate().toString());
        holder.price.setText("$" + data.get(position).getPrice().toString());
        holder.seats.setText(String.valueOf(data.get(position).getSeats()) + " seats");
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

}
