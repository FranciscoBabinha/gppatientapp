package com.example.gpapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ImageButton;
import android.widget.PopupMenu;

public class MainActivity extends AppCompatActivity {
    private TextView tvGreeting;
    private ImageButton settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setElevation(0);
        }

        // Setup top navigation buttons
        setupTopNavigation();

        // Setup greeting text
        tvGreeting = findViewById(R.id.tv_greeting);
        fetchPatientName();

        // Setup buttons
        setupButtons();

        // Initialize settings button
        settingsButton = findViewById(R.id.settingsButton);
        
        // Set click listener for settings button
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
    }

    private void setupTopNavigation() {
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton appointmentsButton = findViewById(R.id.appointmentsButton);

        homeButton.setOnClickListener(v -> {
            // Already on home
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        appointmentsButton.setOnClickListener(v -> {
            // Navigate to appointments screen
            Intent intent = new Intent(this, AppointmentsActivity.class);
            startActivity(intent);
        });
    }

    private void setupButtons() {
        setupButton(R.id.btn_diagnoses, DiagnosesActivity.class);
        setupButton(R.id.btn_allergies, AllergiesActivity.class);
        setupButton(R.id.btn_medications, MedicationsActivity.class);
        setupButton(R.id.btn_results, ResultsActivity.class);
        setupButton(R.id.btn_documents, DocumentsActivity.class);
        setupButton(R.id.btn_vaccines, VaccinesActivity.class);
        
        // Setup bottom navigation buttons
        MaterialButton linkedAppsButton = findViewById(R.id.nav_linked_apps);
        linkedAppsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LinkedAppsLandingActivity.class);
            startActivity(intent);
        });

        MaterialButton paymentsButton = findViewById(R.id.nav_payments);
        paymentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PaymentsActivity.class);
            startActivity(intent);
        });
    }

    private void setupButton(int buttonId, Class<?> activityClass) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, activityClass);
            startActivity(intent);
        });
    }

    private void fetchPatientName() {
        int patientId = UserSession.getInstance(this).getPatientId();
        if (patientId == -1) {
            tvGreeting.setText("Hello!");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("patients").document(String.valueOf(patientId)).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String firstName = documentSnapshot.getString("first_name");
                        if (firstName == null) firstName = "";
                        String greeting = "Hello, " + firstName + "!";
                        tvGreeting.setText(greeting);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("MainActivity", "Failed to fetch patient", e);
                        tvGreeting.setText("Hello!");
                    }
                });
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_logout) {
                    // Logout and return to login screen
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Already on home
            return true;
        } else if (id == R.id.nav_appointments) {
            Toast.makeText(this, "Appointments", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to appointments
            return true;
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to settings
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
