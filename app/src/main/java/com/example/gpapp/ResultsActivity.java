package com.example.gpapp;

public class ResultsActivity extends BaseWebActivity {
    @Override
    protected String getPageTitle() {
        return "Results";
    }

    @Override
    protected String getUrl() {
        return "http://192.168.1.40/GP/gp_app/results.php";
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{"result_description", "date"};
    }
} 