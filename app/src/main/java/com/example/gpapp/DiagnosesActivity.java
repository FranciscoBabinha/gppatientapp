package com.example.gpapp;

public class DiagnosesActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Diagnoses";
    }

    @Override
    protected String getUrl() {
        return "http://192.168.1.40/GP/gp_app/diagnoses.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"diagnosis", "date"};
    }
} 