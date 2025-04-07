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
    protected String getUrl() {
        String baseUrl = getBaseUrl();
        if (baseUrl == null) {
            return ""; // Return empty string if no network is detected
        }
        return baseUrl + "diagnoses.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"diagnosis", "date"};
    }

    private String getBaseUrl() {
        // Change to own IP
        //return "http://172.20.10.3/GP/gp_app/";
        return "http://192.168.1.40/GP/gp_app/";
    }
}
