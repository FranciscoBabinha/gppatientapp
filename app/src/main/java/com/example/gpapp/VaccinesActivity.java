package com.example.gpapp;

public class VaccinesActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Vaccines";
    }

    @Override
    protected String getUrl() {
        return "http://192.168.1.40/GP/gp_app/vaccines.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"vaccine_name", "date_administered"};
    }
} 