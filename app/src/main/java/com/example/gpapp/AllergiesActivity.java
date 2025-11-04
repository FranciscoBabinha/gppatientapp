package com.example.gpapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AllergiesActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Allergies";
    }

    @Override
    protected String getSubcollectionName() {
        return "allergies";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"allergy", "severity"};
    }
}
