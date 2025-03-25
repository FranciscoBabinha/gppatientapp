package com.example.gpapp;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private RecyclerView timeSlotsRecyclerView;
    private TimeSlotAdapter timeSlotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setElevation(0);
        }

        // Setup top navigation
        setupTopNavigation();

        // Setup calendar
        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Update available time slots for selected date
            updateTimeSlots(year, month, dayOfMonth);
        });

        // Setup time slots RecyclerView
        timeSlotsRecyclerView = findViewById(R.id.timeSlotsRecyclerView);
        timeSlotsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        timeSlotAdapter = new TimeSlotAdapter(this::onTimeSlotSelected);
        timeSlotsRecyclerView.setAdapter(timeSlotAdapter);

        // Load initial time slots for today
        Calendar calendar = Calendar.getInstance();
        updateTimeSlots(calendar.get(Calendar.YEAR),
                       calendar.get(Calendar.MONTH),
                       calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void setupTopNavigation() {
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton appointmentsButton = findViewById(R.id.appointmentsButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        homeButton.setOnClickListener(v -> {
            // Navigate back to main activity
            finish();
        });

        appointmentsButton.setOnClickListener(v -> {
            // Already on appointments screen
            Toast.makeText(this, "Appointments", Toast.LENGTH_SHORT).show();
        });

        settingsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to settings
        });
    }

    private void updateTimeSlots(int year, int month, int dayOfMonth) {
        // TODO: Fetch available time slots from your backend
        // For now, we'll use dummy data
        List<String> timeSlots = new ArrayList<>();
        timeSlots.add("09:00 AM");
        timeSlots.add("10:00 AM");
        timeSlots.add("11:00 AM");
        timeSlots.add("02:00 PM");
        timeSlots.add("03:00 PM");
        timeSlots.add("04:00 PM");
        
        timeSlotAdapter.updateTimeSlots(timeSlots);
    }

    private void onTimeSlotSelected(String timeSlot) {
        // TODO: Handle appointment booking
        Toast.makeText(this, "Selected time slot: " + timeSlot, Toast.LENGTH_SHORT).show();
    }
} 