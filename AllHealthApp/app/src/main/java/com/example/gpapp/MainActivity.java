package com.example.gpapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        setupTopAppBar();
        setupBottomNavigation();
        setupButtons();
    }

    private void setupTopAppBar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AllHealth");

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_home) {
                // Already on home, no need to navigate
                return true;
            } else if (itemId == R.id.nav_appointments) {
                Toast.makeText(this, "Appointments", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_ai_assistant) {
                Toast.makeText(this, "AI Assistant", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_payments) {
                Toast.makeText(this, "Payments", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_linked_apps) {
                Toast.makeText(this, "Linked Apps", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        setupButton(R.id.btn_diagnoses, DiagnosesActivity.class);
        setupButton(R.id.btn_allergies, AllergiesActivity.class);
        setupButton(R.id.btn_medications, MedicationsActivity.class);
        setupButton(R.id.btn_results, ResultsActivity.class);
        setupButton(R.id.btn_documents, DocumentsActivity.class);
        setupButton(R.id.btn_vaccines, VaccinesActivity.class);
    }

    private void setupButton(int buttonId, Class<?> activityClass) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
        });
    }

    private void setupButton(int buttonId, String category) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v ->
                Toast.makeText(this, category + " - Coming Soon", Toast.LENGTH_SHORT).show()
        );
    }
}
