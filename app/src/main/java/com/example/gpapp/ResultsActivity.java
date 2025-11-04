package com.example.gpapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ResultsActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Results";
    }

    @Override
    protected String getSubcollectionName() {
        return "results";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"result_description", "date"};
    }
}
