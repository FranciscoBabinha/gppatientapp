package com.example.gpapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BaseWebActivity extends AppCompatActivity {
    protected WebView webView;
    protected abstract String getPageTitle();
    protected abstract String getUrl();
    protected abstract String[] getTableHeaders();

    private String formatHeader(String header) {
        // Convert snake_case to Title Case
        String[] words = header.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return formatted.toString().trim();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getPageTitle());
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup WebView
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        
        // Enable JavaScript and DOM storage
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        
        // Improve readability
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setDefaultFontSize(16);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        
        // Enable zooming
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // Fetch data and display it
        new FetchDataTask().execute(getUrl());
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                return response.toString();
            } catch (Exception e) {
                Log.e("FetchDataTask", "Error fetching data", e);
                return "[]";
            }
        }

        @Override
        protected void onPostExecute(String jsonData) {
            try {
                JSONArray jsonArray = new JSONArray(jsonData);
                String[] headers = getTableHeaders();
                
                // Create HTML table
                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html><html><head>");
                html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                html.append("<style>");
                html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 16px; background-color: #f0f0f0; }");
                html.append(".container { background-color: #ffffff; border-radius: 12px; padding: 20px; margin: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
                html.append(".header { background-color: #4CAF50; color: white; padding: 15px; border-radius: 8px; margin-bottom: 20px; }");
                html.append(".header h1 { margin: 0; font-size: 24px; }");
                html.append(".item { background-color: #f8f8f8; border-radius: 8px; padding: 15px; margin-bottom: 15px; }");
                html.append(".item:last-child { margin-bottom: 0; }");
                html.append(".label { font-weight: bold; color: #4CAF50; font-size: 18px; margin-bottom: 5px; }");
                html.append(".value { font-size: 16px; color: #333; margin-bottom: 10px; }");
                html.append(".value:last-child { margin-bottom: 0; }");
                html.append("</style></head><body>");
                html.append("<div class='container'>");
                html.append("<div class='header'><h1>").append(getPageTitle()).append("</h1></div>");

                // Add data items
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    html.append("<div class='item'>");
                    for (String header : headers) {
                        String value = item.optString(header, "");
                        html.append("<div class='label'>").append(formatHeader(header)).append("</div>");
                        html.append("<div class='value'>").append(value).append("</div>");
                    }
                    html.append("</div>");
                }

                html.append("</div></body></html>");
                
                webView.loadDataWithBaseURL(null, html.toString(), "text/html", "UTF-8", null);
            } catch (Exception e) {
                Log.e("FetchDataTask", "Error processing data", e);
                webView.loadData("Error loading data", "text/plain", "UTF-8");
            }
        }
    }
} 