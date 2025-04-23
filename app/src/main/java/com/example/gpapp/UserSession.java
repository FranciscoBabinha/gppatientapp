package com.example.gpapp;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_PATIENT_ID = "patientId";
    private static UserSession instance;
    private SharedPreferences prefs;

    private UserSession(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context.getApplicationContext());
        }
        return instance;
    }

    public void setUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void setPatientId(int patientId) {
        prefs.edit().putInt(KEY_PATIENT_ID, patientId).apply();
    }

    public int getPatientId() {
        return prefs.getInt(KEY_PATIENT_ID, -1);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }
} 