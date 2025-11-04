package com.example.gpapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MedicationsActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Medications";
    }

    @Override
    protected String getSubcollectionName() {
        return "medications";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"medication_name", "dosage"};
    }
}
