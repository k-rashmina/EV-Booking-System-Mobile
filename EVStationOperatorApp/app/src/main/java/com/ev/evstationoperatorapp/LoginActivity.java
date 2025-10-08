package com.ev.evstationoperatorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    // --- UI Elements ---
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;

    // --- Network & Session Management ---
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences using the central constant
        sharedPreferences = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);

        // Auto-login check
        if (sharedPreferences.getBoolean(ApiConfig.KEY_IS_LOGGED_IN, false)) {
            navigateToMainActivity();
            return;
        }

        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this);

        // Link UI elements
        emailEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            performLogin(email, password);
        });
    }

    /**
     * Performs the login by sending credentials to the backend.
     * Handles both successful responses (with role checking) and error responses.
     */
    private void performLogin(String email, String password) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            loadingProgressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            return;
        }

        // Build the URL from the central ApiConfig
        String loginUrl = ApiConfig.BASE_URL + "/api/Auth/login";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                loginUrl,
                requestBody,
                response -> {
                    // --- SUCCESS RESPONSE (200 OK) HANDLER ---
                    loadingProgressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    Log.d("LoginActivity", "Full Login Response: " + response.toString());

                    try {
                        // Authorization check (what the user is allowed to do)
                        String userRole = response.optString(ApiConfig.KEY_ROLE, "");

                        if (ApiConfig.REQUIRED_ROLE.equals(userRole)) {
                            // User has the correct role, proceed with login
                            String authToken = response.getString("token");
                            String operatorName = response.optString("fullName", "Operator");

                            // Save session data to SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(ApiConfig.KEY_IS_LOGGED_IN, true);
                            editor.putString(ApiConfig.KEY_OPERATOR_NAME, operatorName);
                            editor.putString(ApiConfig.KEY_AUTH_TOKEN, authToken);
                            editor.apply();

                            // Diagnostic logging to confirm what was saved
                            Log.d("LoginActivity", "SAVED operatorName to SharedPreferences: '" + operatorName + "'");

                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            // User is valid but does not have the required role
                            Toast.makeText(LoginActivity.this, "Access Denied: This app is for Station Operators only.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("LoginActivity", "Error parsing successful login response", e);
                        Toast.makeText(LoginActivity.this, "Login successful, but response is invalid.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // --- ERROR RESPONSE HANDLER ---
                    loadingProgressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    Log.e("LoginActivity", "Login Error: " + error.toString());

                    // Authentication check (if the user is who they say they are)
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Check network connection.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // CRITICAL: Disable caching for the login request
        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Navigates to the MainActivity and finishes the LoginActivity.
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

