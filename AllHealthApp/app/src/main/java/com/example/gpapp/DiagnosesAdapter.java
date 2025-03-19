package com.example.gpapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DiagnosesAdapter extends RecyclerView.Adapter<DiagnosesAdapter.DiagnosisViewHolder> {
    private List<Patient> patients;
    private OnDiagnosisClickListener listener;

    public interface OnDiagnosisClickListener {
        void onDiagnosisClick(Patient patient);
    }

    public DiagnosesAdapter(List<Patient> patients, OnDiagnosisClickListener listener) {
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiagnosisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diagnosis, parent, false);
        return new DiagnosisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiagnosisViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.patientNameTextView.setText(patient.getName());
        holder.diagnosisTextView.setText(patient.getDiagnoses());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDiagnosisClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class DiagnosisViewHolder extends RecyclerView.ViewHolder {
        TextView patientNameTextView;
        TextView diagnosisTextView;

        DiagnosisViewHolder(View itemView) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.patientNameTextView);
            diagnosisTextView = itemView.findViewById(R.id.diagnosisTextView);
        }
    }
} 