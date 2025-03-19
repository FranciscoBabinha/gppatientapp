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

public class MedicationsActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private List<Patient> patients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medications);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("patients");

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Medications");

        recyclerView = findViewById(R.id.medicationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        patients = new ArrayList<>();

        recyclerView.setAdapter(new PatientAdapter(patients));

        fetchPatients();
    }

    private void fetchPatients() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patients.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    Toast.makeText(MedicationsActivity.this, "No patient data found", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    if (patient != null) {
                        patients.add(patient);
                    }
                }
                
                recyclerView.getAdapter().notifyDataSetChanged();
                Toast.makeText(MedicationsActivity.this, "Loaded " + patients.size() + " patients", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MedicationsActivity.this, "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 