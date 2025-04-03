package com.example.gpapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DocumentsActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Documents";
    }

    @Override
    protected String getUrl() {
        String baseUrl = getBaseUrl();
        if (baseUrl == null) {
            return ""; // Return empty string if no network is detected
        }
        return baseUrl + "documents.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"document_title", "upload_date"};
    }

    private String getBaseUrl() {
        // Always return the mobile data URL
        return "http://172.20.10.3/GP/gp_app/";
    }
}
