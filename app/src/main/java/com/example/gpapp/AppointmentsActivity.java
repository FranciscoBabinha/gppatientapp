package com.example.gpapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AppointmentsActivity extends AppCompatActivity {
    private static final String TAG = "AppointmentsActivity";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    private static final List<TimeSlotDefinition> ALL_TIME_SLOTS = Arrays.asList(
            new TimeSlotDefinition("09:00 AM", LocalTime.of(9, 0)),
            new TimeSlotDefinition("10:00 AM", LocalTime.of(10, 0)),
            new TimeSlotDefinition("11:00 AM", LocalTime.of(11, 0)),
            new TimeSlotDefinition("02:00 PM", LocalTime.of(14, 0)),
            new TimeSlotDefinition("03:00 PM", LocalTime.of(15, 0)),
            new TimeSlotDefinition("04:00 PM", LocalTime.of(16, 0))
    );

    private CalendarView calendarView;
    private RecyclerView timeSlotsRecyclerView;
    private TimeSlotAdapter timeSlotAdapter;
    private FirebaseFirestore firestore;
    private LocalDate selectedDate;

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

        firestore = FirebaseFirestore.getInstance();

        // Setup top navigation
        setupTopNavigation();

        // Setup calendar
        calendarView = findViewById(R.id.calendarView);
        calendarView.setMinDate(System.currentTimeMillis());
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
            updateTimeSlots(date);
        });

        // Setup time slots RecyclerView
        timeSlotsRecyclerView = findViewById(R.id.timeSlotsRecyclerView);
        timeSlotsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        timeSlotAdapter = new TimeSlotAdapter(this::onTimeSlotSelected);
        timeSlotsRecyclerView.setAdapter(timeSlotAdapter);

        // Load initial time slots for today
        Calendar calendar = Calendar.getInstance();
        LocalDate today = LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        updateTimeSlots(today);
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

    private void updateTimeSlots(LocalDate date) {
        selectedDate = date;
        String dateKey = DATE_FORMATTER.format(date);

        firestore.collection("appointments")
                .whereEqualTo("date", dateKey)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> bookedSlots = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String slot = doc.getString("timeSlot");
                        if (slot != null) {
                            bookedSlots.add(slot);
                        }
                    }

                    List<String> availableSlots = filterFutureTimeSlots(date, bookedSlots);
                    timeSlotAdapter.updateTimeSlots(availableSlots);
                    if (availableSlots.isEmpty()) {
                        Toast.makeText(
                                this,
                                "No remaining slots for the selected date.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load time slots for " + dateKey, e);
                    Toast.makeText(this,
                            "Failed to load time slots. Showing defaults.",
                            Toast.LENGTH_SHORT).show();
                    timeSlotAdapter.updateTimeSlots(filterFutureTimeSlots(
                            date,
                            Collections.emptySet()
                    ));
                });
    }

    private void onTimeSlotSelected(String timeSlot) {
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date first.", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlotDefinition slotDefinition = findTimeSlotDefinition(timeSlot);
        if (slotDefinition != null && selectedDate.isEqual(LocalDate.now())
                && slotDefinition.time.isBefore(LocalTime.now())) {
            Toast.makeText(
                    this,
                    "This time has already passed. Please choose a future slot.",
                    Toast.LENGTH_SHORT
            ).show();
            updateTimeSlots(selectedDate);
            return;
        }

        UserSession session = UserSession.getInstance(this);
        int patientId = session.getPatientId();
        if (patientId == -1) {
            Toast.makeText(this, "Please log in again to book an appointment.", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateKey = DATE_FORMATTER.format(selectedDate);
        String documentId = buildDocumentId(dateKey, timeSlot);
        DocumentReference slotRef = firestore.collection("appointments").document(documentId);

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("date", dateKey);
        bookingData.put("timeSlot", timeSlot);
        bookingData.put("patientId", String.valueOf(patientId));
        int userId = session.getUserId();
        if (userId != -1) {
            bookingData.put("userId", userId);
        }
        bookingData.put("createdAt", FieldValue.serverTimestamp());

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(slotRef);
                    if (snapshot.exists()) {
                        throw new FirebaseFirestoreException(
                                "Time slot already booked",
                                FirebaseFirestoreException.Code.ABORTED
                        );
                    }
                    transaction.set(slotRef, bookingData);
                    return null;
                })
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            this,
                            "Appointment booked for " + timeSlot + " on " + dateKey,
                            Toast.LENGTH_SHORT
                    ).show();
                    updateTimeSlots(selectedDate);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException
                            && ((FirebaseFirestoreException) e).getCode()
                            == FirebaseFirestoreException.Code.ABORTED) {
                        Toast.makeText(
                                this,
                                "That time slot was just taken. Please pick another.",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                this,
                                "Failed to book appointment. Please try again.",
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.e(TAG, "Failed to book appointment for " + dateKey + " " + timeSlot, e);
                    }
                    updateTimeSlots(selectedDate);
                });
    }

    private String buildDocumentId(String dateKey, String timeSlot) {
        String normalizedTime = timeSlot
                .toLowerCase(Locale.US)
                .replace(" ", "")
                .replace(":", "");
        return dateKey + "_" + normalizedTime;
    }

    private List<String> filterFutureTimeSlots(LocalDate date, Set<String> bookedSlots) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<String> availableSlots = new ArrayList<>();
        for (TimeSlotDefinition slot : ALL_TIME_SLOTS) {
            if (bookedSlots.contains(slot.label)) {
                continue;
            }
            if (date.isEqual(today) && slot.time.isBefore(now)) {
                continue;
            }
            if (date.isBefore(today)) {
                continue;
            }
            availableSlots.add(slot.label);
        }
        return availableSlots;
    }

    private TimeSlotDefinition findTimeSlotDefinition(String label) {
        for (TimeSlotDefinition slot : ALL_TIME_SLOTS) {
            if (slot.label.equals(label)) {
                return slot;
            }
        }
        return null;
    }

    private static class TimeSlotDefinition {
        final String label;
        final LocalTime time;

        TimeSlotDefinition(String label, LocalTime time) {
            this.label = label;
            this.time = time;
        }
    }
}