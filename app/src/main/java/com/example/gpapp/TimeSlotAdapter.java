package com.example.gpapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private List<String> timeSlots = new ArrayList<>();
    private final OnTimeSlotClickListener listener;

    public interface OnTimeSlotClickListener {
        void onTimeSlotSelected(String timeSlot);
    }

    public TimeSlotAdapter(OnTimeSlotClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        String timeSlot = timeSlots.get(position);
        holder.timeSlotText.setText(timeSlot);
        holder.itemView.setOnClickListener(v -> listener.onTimeSlotSelected(timeSlot));
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public void updateTimeSlots(List<String> newTimeSlots) {
        this.timeSlots = newTimeSlots;
        notifyDataSetChanged();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView timeSlotText;

        TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeSlotText = itemView.findViewById(android.R.id.text1);
        }
    }
} 