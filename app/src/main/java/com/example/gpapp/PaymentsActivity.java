package com.example.gpapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class PaymentsActivity extends AppCompatActivity {
    private static final String TAG = "PaymentsActivity";
    private static final double DEFAULT_BALANCE = 500.0;
    private static final String FIELD_BALANCE_DUE = "balance_due";
    private TextView tvAmount;
    private EditText etPayment;
    private MaterialButton btnPay;
    private double remainingAmount = DEFAULT_BALANCE;
    private boolean balanceLoaded = false;
    private FirebaseFirestore firestore;
    private DocumentReference balanceRef;

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

        firestore = FirebaseFirestore.getInstance();
        UserSession session = UserSession.getInstance(this);
        int patientId = session.getPatientId();
        if (patientId == -1) {
            Toast.makeText(this, "Please log in again to view payments.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        balanceRef = firestore.collection("patients").document(String.valueOf(patientId));

        // Update amount display
        setAmountLoading();
        btnPay.setEnabled(false);
        loadBalanceFromDb();

        // Setup payment input listener
        etPayment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!balanceLoaded) {
                    return;
                }
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
                if (!balanceLoaded) {
                    Toast.makeText(PaymentsActivity.this,
                            "Loading your balance. Please wait.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String paymentStr = etPayment.getText().toString();
                if (!paymentStr.isEmpty()) {
                    try {
                        double paymentAmount = Double.parseDouble(paymentStr);
                        processPayment(paymentAmount);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (balanceRef != null) {
            loadBalanceFromDb();
        }
    }

    private void loadBalanceFromDb() {
        btnPay.setEnabled(false);
        balanceRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Double dbBalance = readBalance(documentSnapshot);
                    if (dbBalance == null) {
                        dbBalance = DEFAULT_BALANCE;
                        balanceRef.set(new BalanceWrapper(dbBalance), com.google.firebase.firestore.SetOptions.merge());
                    }
                    remainingAmount = dbBalance;
                    balanceLoaded = true;
                    updateAmountDisplay();
                    btnPay.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PaymentsActivity.this,
                            "Failed to load balance. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    balanceLoaded = true;
                    remainingAmount = DEFAULT_BALANCE;
                    updateAmountDisplay();
                    btnPay.setEnabled(true);
                });
    }

    private Double readBalance(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        Object raw = snapshot.get(FIELD_BALANCE_DUE);
        if (raw instanceof Number) {
            return ((Number) raw).doubleValue();
        }
        return null;
    }

    private void processPayment(double paymentAmount) {
        if (paymentAmount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        btnPay.setEnabled(false);
        firestore.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(balanceRef);
                    double currentBalance = DEFAULT_BALANCE;
                    Double dbBalance = readBalance(snapshot);
                    if (dbBalance != null) {
                        currentBalance = dbBalance;
                    }
                    if (paymentAmount > currentBalance) {
                        throw new FirebaseFirestoreException(
                                "Payment amount cannot exceed remaining balance",
                                FirebaseFirestoreException.Code.ABORTED
                        );
                    }
                    double newBalance = currentBalance - paymentAmount;
                    if (snapshot != null && snapshot.exists()) {
                        transaction.update(balanceRef, FIELD_BALANCE_DUE, newBalance);
                    } else {
                        transaction.set(balanceRef,
                                new BalanceWrapper(newBalance),
                                com.google.firebase.firestore.SetOptions.merge());
                    }
                    return newBalance;
                })
                .addOnSuccessListener(newBalance -> {
                    remainingAmount = newBalance;
                    updateAmountDisplay();
                    etPayment.setText("");
                    Toast.makeText(PaymentsActivity.this,
                            "Payment of " + paymentAmount + "€ processed",
                            Toast.LENGTH_SHORT).show();
                    btnPay.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    String message = "Payment failed. Please try again.";
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException.Code code =
                                ((FirebaseFirestoreException) e).getCode();
                        if (code == FirebaseFirestoreException.Code.ABORTED) {
                            message = "Payment amount cannot exceed remaining balance";
                        } else if (code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            message = "Payment failed (permission denied). Check Firestore rules.";
                        } else if (code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                            message = "Payment failed (network unavailable). Try again.";
                        }
                    }
                    Log.e(TAG, "Payment transaction failed", e);
                    Toast.makeText(PaymentsActivity.this, message, Toast.LENGTH_SHORT).show();
                    btnPay.setEnabled(true);
                });
    }

    private void setAmountLoading() {
        tvAmount.setText("Loading...");
    }

    private void updateAmountDisplay() {
        tvAmount.setText(String.format("%.2f€", remainingAmount));
    }

    private static class BalanceWrapper {
        public double balance_due;

        BalanceWrapper(double balanceDue) {
            this.balance_due = balanceDue;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 