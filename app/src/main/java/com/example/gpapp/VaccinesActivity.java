package com.example.gpapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class VaccinesActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Vaccines";
    }


    @Override
    protected String getUrl() {
        String baseUrl = getBaseUrl();
        if (baseUrl == null) {
            return ""; // Return empty string if no network is detected
        }
        return baseUrl + "vaccines.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"vaccine_name", "date_administered"};
    }

    private String getBaseUrl() {
        // Change to own IP
        //return "http://172.20.10.3/GP/gp_app/";
        return "http://192.168.1.43/GP/gp_app/";
    }
}
