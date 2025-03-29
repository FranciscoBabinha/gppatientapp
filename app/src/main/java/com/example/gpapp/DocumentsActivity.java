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
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            return "http://192.168.1.40/GP/gp_app/"; // Home Wi-Fi URL
        } else if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
            return "http://172.20.10.3/GP/gp_app/"; // Mobile Data URL
        }
        return null;
    }
}
