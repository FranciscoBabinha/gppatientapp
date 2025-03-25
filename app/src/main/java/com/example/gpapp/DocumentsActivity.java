package com.example.gpapp;

public class DocumentsActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Documents";
    }

    @Override
    protected String getUrl() {
        return "http://192.168.1.40/GP/gp_app/documents.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"document_title", "upload_date"};
    }
} 