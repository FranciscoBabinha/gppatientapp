package com.example.gpapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AllergiesAdapter extends RecyclerView.Adapter<AllergiesAdapter.AllergyViewHolder> {
    private List<Patient> patients;
    private OnAllergyClickListener listener;

    public interface OnAllergyClickListener {
        void onAllergyClick(Patient patient);
    }

    public AllergiesAdapter(List<Patient> patients, OnAllergyClickListener listener) {
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AllergyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_allergy, parent, false);
        return new AllergyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllergyViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.patientNameTextView.setText(patient.getName());
        holder.allergyTextView.setText(patient.getAllergies());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAllergyClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class AllergyViewHolder extends RecyclerView.ViewHolder {
        TextView patientNameTextView;
        TextView allergyTextView;

        AllergyViewHolder(View itemView) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.patientNameTextView);
            allergyTextView = itemView.findViewById(R.id.allergyTextView);
        }
    }
} 