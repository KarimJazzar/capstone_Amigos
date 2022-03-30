package com.amigos.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amigos.myapplication.R;
import com.amigos.myapplication.activities.BookingActivity;
import com.amigos.myapplication.activities.MessagesActivity;
import com.amigos.myapplication.helpers.FirebaseHelper;
import com.amigos.myapplication.helpers.UserHelper;
import com.amigos.myapplication.models.Chat;
import com.amigos.myapplication.models.Trip;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.MyViewHolder> {
    private ArrayList<Trip> data = new ArrayList<>();
    private TripAdapter.OnItemClickListener listener;
    public TripAdapter(ArrayList<Trip>data)
    {
        this.data=data;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView driverName;
        private TextView date;
        private TextView price;
        private TextView seats;
        private ImageView profPic;

        public MyViewHolder(@NonNull View view) {
            super(view);
            driverName = view.findViewById(R.id.chatNameRV);
            date = view.findViewById(R.id.chatMsgRV);
            price = view.findViewById(R.id.chatNameRV2);
            seats = view.findViewById(R.id.chatMsgRV2);
            profPic = view.findViewById(R.id.chatImageRV);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Context context = v.getContext();
                    Intent intent = new Intent(context, BookingActivity.class);
                    intent.putExtra("trip_obj", data.get(position));
                    context.startActivity(intent);
                }
            });
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
        holder.seats.setText(String.valueOf(data.get(position).getSeats()) + " seats available");
        FirebaseHelper.instance.setProfileImage(data.get(position).getDriver().getProfilePicture(), holder.profPic);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Chat model);
    }

    public void setOnItemClickListener(TripAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}

