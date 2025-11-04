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
    protected String getSubcollectionName() {
        return "vaccines";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"vaccine_name", "date_administered"};
    }
}
