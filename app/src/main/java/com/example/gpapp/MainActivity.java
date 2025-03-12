package com.example.gpapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupTopAppBar();
        setupBottomNavigation();
        setupButtons();
    }

    private void setupTopAppBar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
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
        setupButton(R.id.btn_diagnoses, "Diagnoses");
        setupButton(R.id.btn_allergies, "Allergies");
        setupButton(R.id.btn_medications, "Medications");
        setupButton(R.id.btn_results, "Results");
        setupButton(R.id.btn_documents, "Documents");
        setupButton(R.id.btn_vaccines, "Vaccines");
    }

    private void setupButton(int buttonId, String category) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> 
            Toast.makeText(this, category, Toast.LENGTH_SHORT).show()
        );
    }
}