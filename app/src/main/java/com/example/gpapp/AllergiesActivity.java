package com.example.gpapp;

public class AllergiesActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Allergies";
    }

    @Override
    protected String getUrl() {
        return "http://192.168.1.40/GP/gp_app/allergies.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"allergy", "severity"};
    }
} 