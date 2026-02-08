package com.example.gpapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 2001;
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
    private LocalDate pendingCalendarDate;
    private TimeSlotDefinition pendingCalendarSlot;
    private String selectedTimeSlot;

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

        MaterialButton confirmAppointmentButton = findViewById(R.id.confirmAppointmentButton);
        confirmAppointmentButton.setOnClickListener(v -> onConfirmAppointment());

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
        boolean dateChanged = selectedDate != null && !selectedDate.equals(date);
        selectedDate = date;
        String dateKey = DATE_FORMATTER.format(date);

        if (dateChanged) {
            selectedTimeSlot = null;
            timeSlotAdapter.setSelectedTimeSlot(null);
        }

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
                    if (selectedTimeSlot != null && !availableSlots.contains(selectedTimeSlot)) {
                        selectedTimeSlot = null;
                        timeSlotAdapter.setSelectedTimeSlot(null);
                    }
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

        selectedTimeSlot = timeSlot;
        timeSlotAdapter.setSelectedTimeSlot(timeSlot);
    }

    private void onConfirmAppointment() {
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date first.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Please select a time slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlotDefinition slotDefinition = findTimeSlotDefinition(selectedTimeSlot);
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

        bookAppointment(selectedTimeSlot, slotDefinition);
    }

    private void bookAppointment(String timeSlot, TimeSlotDefinition slotDefinition) {
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
                    if (slotDefinition != null) {
                        ensureCalendarPermissionThenAdd(selectedDate, slotDefinition);
                    }
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

    private void ensureCalendarPermissionThenAdd(LocalDate date, TimeSlotDefinition slotDefinition) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {
            addAppointmentToCalendar(date, slotDefinition);
            return;
        }

        pendingCalendarDate = date;
        pendingCalendarSlot = slotDefinition;
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                CALENDAR_PERMISSION_REQUEST_CODE
        );
    }

    private void addAppointmentToCalendar(LocalDate date, TimeSlotDefinition slotDefinition) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Calendar permission is required to save the event.", Toast.LENGTH_SHORT).show();
            return;
        }

        Long calendarId = getPrimaryCalendarId();
        if (calendarId == null) {
            Toast.makeText(this, "No calendar available on this device.", Toast.LENGTH_SHORT).show();
            return;
        }

        ZonedDateTime startTime = ZonedDateTime.of(date, slotDefinition.time, ZoneId.systemDefault());
        ZonedDateTime endTime = startTime.plusHours(1);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.TITLE, "Doctor's Appointment");
        values.put(CalendarContract.Events.DESCRIPTION, "Booked via GP Patient App");
        values.put(CalendarContract.Events.DTSTART, startTime.toInstant().toEpochMilli());
        values.put(CalendarContract.Events.DTEND, endTime.toInstant().toEpochMilli());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, ZoneId.systemDefault().getId());
        values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        ContentResolver resolver = getContentResolver();
        try {
            android.net.Uri eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, values);
            if (eventUri == null) {
                Toast.makeText(this, "Failed to add appointment to calendar.", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "Inserted calendar event uri=" + eventUri);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Missing calendar permission", e);
            Toast.makeText(this, "Calendar permission is required to save the event.", Toast.LENGTH_SHORT).show();
        }
    }

    private Long getPrimaryCalendarId() {
        ContentResolver resolver = getContentResolver();
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        };

        String writableVisibleSelection = CalendarContract.Calendars.VISIBLE + " = 1 AND "
                + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= "
                + CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR;

        Long visibleMatch = queryCalendarId(
                resolver,
                projection,
                writableVisibleSelection
        );
        if (visibleMatch != null) {
            return visibleMatch;
        }

        String writableAnySelection = CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= "
                + CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR;
        Long anyMatch = queryCalendarId(resolver, projection, writableAnySelection);
        if (anyMatch != null) {
            return anyMatch;
        }

        return createLocalCalendar(resolver);
    }

    private Long queryCalendarId(ContentResolver resolver, String[] projection, String selection) {
        try (android.database.Cursor cursor = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                null,
                null
        )) {
            if (cursor == null) {
                Log.w(TAG, "Calendar query returned null cursor");
                return null;
            }
            Long firstId = null;
            Long firstGoogleId = null;
            int count = 0;
            StringBuilder accountTypes = new StringBuilder();
            while (cursor.moveToNext()) {
                count++;
                long id = cursor.getLong(0);
                if (firstId == null) {
                    firstId = id;
                }
                int isPrimary = cursor.getInt(1);
                String accountType = cursor.getString(2);
                int accessLevel = cursor.getInt(3);
                if (accountType != null) {
                    if (accountTypes.length() > 0) {
                        accountTypes.append(", ");
                    }
                    accountTypes.append(accountType).append("(").append(accessLevel).append(")");
                }
                if ("com.google".equals(accountType) && firstGoogleId == null) {
                    firstGoogleId = id;
                }
                if (isPrimary == 1 && "com.google".equals(accountType)) {
                    return id;
                }
            }
            Log.i(TAG, "Calendar rows=" + count + " accountTypes=" + accountTypes);
            if (firstGoogleId != null) {
                return firstGoogleId;
            }
            return firstId;
        } catch (SecurityException e) {
            Log.e(TAG, "Missing calendar permission", e);
            return null;
        }
    }

    private Long createLocalCalendar(ContentResolver resolver) {
        String accountName = "gpapp-local";
        String accountType = CalendarContract.ACCOUNT_TYPE_LOCAL;

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, accountType);
        values.put(CalendarContract.Calendars.NAME, "GP Patient App");
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "GP Patient App");
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF1976D2);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);

        android.net.Uri uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
                .build();

        try {
            android.net.Uri result = resolver.insert(uri, values);
            if (result == null) {
                Log.w(TAG, "Failed to create local calendar");
                return null;
            }
            long id = Long.parseLong(result.getLastPathSegment());
            Log.i(TAG, "Created local calendar id=" + id);
            return id;
        } catch (SecurityException e) {
            Log.e(TAG, "Missing calendar permission while creating local calendar", e);
            return null;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse calendar id", e);
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != CALENDAR_PERMISSION_REQUEST_CODE) {
            return;
        }

        boolean granted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }

        if (granted && pendingCalendarDate != null && pendingCalendarSlot != null) {
            addAppointmentToCalendar(pendingCalendarDate, pendingCalendarSlot);
        } else {
            Toast.makeText(this, "Calendar permission denied.", Toast.LENGTH_SHORT).show();
        }

        pendingCalendarDate = null;
        pendingCalendarSlot = null;
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