package com.example.gpapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DiagnosesActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Diagnoses";
    }

    @Override
    protected String getSubcollectionName() {
        return "diagnoses";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"diagnosis", "date"};
    }
}
