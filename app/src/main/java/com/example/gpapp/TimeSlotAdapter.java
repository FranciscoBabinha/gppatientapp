package com.example.gpapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private List<String> timeSlots = new ArrayList<>();
    private final OnTimeSlotClickListener listener;
    private String selectedTimeSlot;

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
                .inflate(R.layout.time_slot_item, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        String timeSlot = timeSlots.get(position);
        holder.timeSlotText.setText(timeSlot);
        boolean isSelected = timeSlot.equals(selectedTimeSlot);
        holder.bindSelection(isSelected);
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

    public void setSelectedTimeSlot(String timeSlot) {
        selectedTimeSlot = timeSlot;
        notifyDataSetChanged();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView timeSlotCard;
        TextView timeSlotText;

        TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeSlotCard = itemView.findViewById(R.id.timeSlotCard);
            timeSlotText = itemView.findViewById(R.id.timeSlotText);
        }

        void bindSelection(boolean isSelected) {
            Context context = itemView.getContext();
            int selectedColor = ContextCompat.getColor(context, android.R.color.holo_blue_light);
            int defaultColor = ContextCompat.getColor(context, android.R.color.white);
            int selectedTextColor = ContextCompat.getColor(context, android.R.color.white);
            int defaultTextColor = ContextCompat.getColor(context, android.R.color.black);

            timeSlotCard.setCardBackgroundColor(isSelected ? selectedColor : defaultColor);
            timeSlotText.setTextColor(isSelected ? selectedTextColor : defaultTextColor);

            float density = context.getResources().getDisplayMetrics().density;
            int strokeWidth = isSelected ? (int) (2 * density) : 0;
            timeSlotCard.setStrokeWidth(strokeWidth);
        }
    }
} 