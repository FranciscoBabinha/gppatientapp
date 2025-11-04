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
    protected String getSubcollectionName() {
        return "documents";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"document_title", "upload_date"};
    }
}
