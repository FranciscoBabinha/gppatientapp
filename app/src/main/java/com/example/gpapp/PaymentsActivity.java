package com.example.gpapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class PaymentsActivity extends AppCompatActivity {
    private TextView tvAmount;
    private EditText etPayment;
    private MaterialButton btnPay;
    private double totalAmount = 500.0;
    private double remainingAmount = 500.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        tvAmount = findViewById(R.id.tv_amount);
        etPayment = findViewById(R.id.et_payment);
        btnPay = findViewById(R.id.btn_pay);

        // Update amount display
        updateAmountDisplay();

        // Setup payment input listener
        etPayment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    try {
                        double paymentAmount = Double.parseDouble(input);
                        if (paymentAmount > remainingAmount) {
                            Toast.makeText(PaymentsActivity.this, 
                                "Payment amount cannot exceed remaining balance", 
                                Toast.LENGTH_SHORT).show();
                            etPayment.setText("");
                        }
                    } catch (NumberFormatException e) {
                        // Handle invalid number format
                        etPayment.setText("");
                    }
                }
            }
        });

        // Setup pay button
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paymentStr = etPayment.getText().toString();
                if (!paymentStr.isEmpty()) {
                    try {
                        double paymentAmount = Double.parseDouble(paymentStr);
                        if (paymentAmount <= remainingAmount) {
                            remainingAmount -= paymentAmount;
                            updateAmountDisplay();
                            etPayment.setText("");
                            Toast.makeText(PaymentsActivity.this, 
                                "Payment of " + paymentAmount + "€ processed", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(PaymentsActivity.this, 
                            "Please enter a valid amount", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentsActivity.this, 
                        "Please enter an amount", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAmountDisplay() {
        tvAmount.setText(String.format("%.2f€", remainingAmount));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 