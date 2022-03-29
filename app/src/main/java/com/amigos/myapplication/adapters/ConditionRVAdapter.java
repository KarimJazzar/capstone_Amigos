package com.amigos.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amigos.myapplication.R;

public class ConditionRVAdapter extends ListAdapter<String, ConditionRVAdapter.ViewHolder> {
    private ConditionRVAdapter.OnItemClickListener listener;

    public ConditionRVAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<String> DIFF_CALLBACK = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(String oldItem, String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(String oldItem, String newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ConditionRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_condition_cell, parent, false);
        return new ConditionRVAdapter.ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ConditionRVAdapter.ViewHolder holder, int position) {
        holder.conditionTV.setText(getConditionAt(position));
    }

    public String getConditionAt(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView conditionTV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            conditionTV = itemView.findViewById(R.id.conditionTV);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String model);
    }

    public void setOnItemClickListener(ConditionRVAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}