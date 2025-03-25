package com.example.gpapp;

public class MedicationsActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Medications";
    }

    @Override
    protected String getUrl() {
        return "http://192.168.1.40/GP/gp_app/medications.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"medication_name", "dosage"};
    }
} 