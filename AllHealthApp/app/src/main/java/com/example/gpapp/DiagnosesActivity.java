package com.example.gpapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class DiagnosesActivity extends AppCompatActivity implements DiagnosesAdapter.OnDiagnosisClickListener {
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private DiagnosesAdapter adapter;
    private List<Patient> patients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnoses);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("patients");

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Diagnoses");

        recyclerView = findViewById(R.id.diagnosesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        patients = new ArrayList<>();
        adapter = new DiagnosesAdapter(patients, this);
        recyclerView.setAdapter(adapter);

        fetchPatients();
    }

    private void fetchPatients() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patients.clear();
                
                // If no data exists, add test data
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    addTestData();
                    return;
                }
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    if (patient != null) {
                        patient.setId(dataSnapshot.getKey());
                        patients.add(patient);
                    }
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(DiagnosesActivity.this, "Loaded " + patients.size() + " patients", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DiagnosesActivity.this, "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTestData() {
        // Clear existing data first
        databaseReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Add test patients with diagnoses
                Patient patient1 = new Patient("John O'Sullivan", "Penicillin (rash)");
                patient1.setDiagnoses("Hypertension, Type 2 Diabetes");
                
                Patient patient2 = new Patient("Mary Doyle", "None known");
                patient2.setDiagnoses("Asthma, Seasonal Allergies");
                
                Patient patient3 = new Patient("Patrick Byrne", "Dust mites, Pollen");
                patient3.setDiagnoses("Rheumatoid Arthritis");
                
                Patient patient4 = new Patient("Fiona Kelly", "None known");
                patient4.setDiagnoses("GERD, Anxiety");
                
                Patient patient5 = new Patient("Liam Fitzpatrick", "NSAIDs (stomach upset)");
                patient5.setDiagnoses("Coronary Artery Disease");
                
                Patient patient6 = new Patient("Emily O'Brien", "Peanuts, Cat dander");
                patient6.setDiagnoses("Migraine, Depression");

                databaseReference.child("patient1").setValue(patient1);
                databaseReference.child("patient2").setValue(patient2);
                databaseReference.child("patient3").setValue(patient3);
                databaseReference.child("patient4").setValue(patient4);
                databaseReference.child("patient5").setValue(patient5);
                databaseReference.child("patient6").setValue(patient6);

                Toast.makeText(DiagnosesActivity.this, "Test data added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DiagnosesActivity.this, "Failed to clear database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDiagnosisClick(Patient patient) {
        showEditDialog(patient);
    }

    private void showEditDialog(Patient patient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_diagnosis, null);
        
        EditText diagnosisEditText = view.findViewById(R.id.diagnosisEditText);
        diagnosisEditText.setText(patient.getDiagnoses());

        builder.setTitle("Edit Diagnoses")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newDiagnoses = diagnosisEditText.getText().toString();
                    updateDiagnoses(patient, newDiagnoses);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDiagnoses(Patient patient, String newDiagnoses) {
        if (patient.getId() == null) {
            Toast.makeText(this, "Error: Patient ID not found", Toast.LENGTH_SHORT).show();
            return;
        }


        DatabaseReference patientRef = databaseReference.child(patient.getId());
        

        patientRef.child("diagnoses").setValue(newDiagnoses)
                .addOnSuccessListener(aVoid -> {
                    // Update local data too
                    patient.setDiagnoses(newDiagnoses);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Diagnoses updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update diagnoses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}