package com.example.gpapp;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class BaseWebActivity extends AppCompatActivity {
    protected WebView webView;
    protected TextView contentText;
    protected ViewGroup contentContainer;
    private boolean webViewUsable = false;
    protected UserSession userSession;
    protected abstract String getPageTitle();
    protected abstract String getSubcollectionName();
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

        // Initialize UserSession
        userSession = UserSession.getInstance(this);

        // Check if user is logged in
        if (!userSession.isLoggedIn()) {
            // Redirect to login activity
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getPageTitle());
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Container; we'll add either WebView or TextView dynamically to avoid XML inflation crashes
        contentContainer = findViewById(R.id.contentContainer);
        // Configure WebView safely
        try {
            android.content.pm.PackageInfo pkg = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                pkg = android.webkit.WebView.getCurrentWebViewPackage();
            }
            if (pkg == null) {
                throw new RuntimeException("WebView engine package missing");
            }
            webView = new WebView(this);
            webView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDefaultTextEncodingName("utf-8");
            webSettings.setDefaultFontSize(16);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            webView.setWebViewClient(new WebViewClient());
            contentContainer.addView(webView);
            webViewUsable = true;
        } catch (Throwable t) {
            // Fallback to TextView if WebView init has issues on this device
            contentText = new TextView(this);
            contentText.setMovementMethod(LinkMovementMethod.getInstance());
            contentText.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            contentText.setPadding(pad, pad, pad, pad);
            contentContainer.addView(contentText);
            webViewUsable = false;
        }

        // Fetch data from Firestore and display it
        int patientId = userSession.getPatientId();
        if (patientId == -1) {
            showError("Not logged in");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("patients")
                .document(String.valueOf(patientId))
                .collection(getSubcollectionName())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        try {
                            JSONArray jsonArray = new JSONArray();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                JSONObject obj = new JSONObject();
                                for (String header : getTableHeaders()) {
                                    Object val = doc.get(header);
                                    obj.put(header, formatValue(val));
                                }
                                jsonArray.put(obj);
                            }
                            renderHtml(jsonArray);
                        } catch (Exception e) {
                            Log.e("BaseWebActivity", "Error building JSON from Firestore", e);
                            showError("Error loading data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("BaseWebActivity", "Firestore query failed", e);
                        showError("Error loading data");
                    }
                });
    }

    private void renderHtml(JSONArray jsonArray) {
        try {
            String[] headers = getTableHeaders();

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

            showHtml(html.toString());
        } catch (Exception e) {
            Log.e("BaseWebActivity", "Error rendering HTML", e);
            showError("Error loading data");
        }
    }

    private String formatValue(Object val) {
        if (val == null) return "";
        try {
            if (val instanceof Timestamp) {
                Date d = ((Timestamp) val).toDate();
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
            }
            if (val instanceof Date) {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format((Date) val);
            }
        } catch (Exception ignored) {}
        return String.valueOf(val);
    }

    private void showHtml(String html) {
        if (webViewUsable && webView != null) {
            try {
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                return;
            } catch (Throwable t) {
                webViewUsable = false;
                contentContainer.removeView(webView);
                webView = null;
                if (contentText == null) {
                    contentText = new TextView(this);
                    contentText.setMovementMethod(LinkMovementMethod.getInstance());
                    contentText.setLayoutParams(new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    ));
                    int pad = (int) (16 * getResources().getDisplayMetrics().density);
                    contentText.setPadding(pad, pad, pad, pad);
                    contentContainer.addView(contentText);
                }
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            contentText.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        } else {
            contentText.setText(Html.fromHtml(html));
        }
    }

    private void showError(String message) {
        if (webViewUsable && webView != null) {
            try {
                webView.loadData(message, "text/plain", "UTF-8");
                return;
            } catch (Throwable t) {
                webViewUsable = false;
                contentContainer.removeView(webView);
                webView = null;
                if (contentText == null) {
                    contentText = new TextView(this);
                    contentText.setMovementMethod(LinkMovementMethod.getInstance());
                    contentText.setLayoutParams(new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    ));
                    int pad = (int) (16 * getResources().getDisplayMetrics().density);
                    contentText.setPadding(pad, pad, pad, pad);
                    contentContainer.addView(contentText);
                }
            }
        }
        contentText.setText(message);
    }
} 