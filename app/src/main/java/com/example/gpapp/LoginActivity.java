package com.example.gpapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
 

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Initialize UserSession
        userSession = UserSession.getInstance(this);

        // Initialize views
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform login
                performLogin(username, password);
            }
        });
    }

    private void performLogin(final String username, final String password) {
        final String email = username.contains("@") ? username : (username + "@example.local");
        final String usernameKey = username.contains("@") ? username.substring(0, username.indexOf("@")) : username;
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Prefer existing mapping at users/{username}
                        db.collection("users").document(usernameKey).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            String patientIdStr = documentSnapshot.getString("patient_id");
                                            completeLoginWithPatientId(patientIdStr);
                                            return;
                                        }
                                        // Fallback: userProfiles/{uid}
                                        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                                        if (uid == null) {
                                            Toast.makeText(LoginActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        db.collection("userProfiles").document(uid).get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot profile) {
                                                        String patientIdStr = profile.getString("patient_id");
                                                        completeLoginWithPatientId(patientIdStr);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        Toast.makeText(LoginActivity.this, "Failed to fetch user mapping", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(LoginActivity.this, "Failed to fetch user mapping", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void completeLoginWithPatientId(String patientIdStr) {
        int patientId = -1;
        try {
            if (patientIdStr != null) patientId = Integer.parseInt(patientIdStr);
        } catch (NumberFormatException ignored) {}
        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient mapping", Toast.LENGTH_SHORT).show();
            return;
        }
        userSession.setUserId(1);
        userSession.setPatientId(patientId);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}