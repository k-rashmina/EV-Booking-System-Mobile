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

    // --- Constants ---
    private static final String LOGIN_URL = "http://10.0.2.2:5148/api/Auth/login";
    private static final String PREFS_NAME = "OperatorPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_OPERATOR_NAME = "operatorName";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_ROLE = "role";
    private static final String REQUIRED_ROLE = "StationOperator";


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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            navigateToMainActivity();
            return;
        }

        requestQueue = Volley.newRequestQueue(this);

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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                LOGIN_URL,
                requestBody,
                response -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Log.d("LoginActivity", "Login Response: " + response.toString());
                    try {
                        String userRole = response.optString(KEY_ROLE);
                        if (REQUIRED_ROLE.equals(userRole)) {
                            String authToken = response.getString("token");
                            String operatorName = response.optString("fullName", "Operator");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_IS_LOGGED_IN, true);
                            editor.putString(KEY_OPERATOR_NAME, operatorName);
                            editor.putString(KEY_AUTH_TOKEN, authToken);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, "Access Denied: This app is for Station Operators only.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Login successful, but response is invalid.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Log.e("LoginActivity", "Login Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("LoginActivity", "Status Code: " + error.networkResponse.statusCode);
                    }
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Check network connection.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // --- FIX: Disable caching for this request ---
        // This ensures that Volley always goes to the network for a fresh response.
        jsonObjectRequest.setShouldCache(false);

        requestQueue.add(jsonObjectRequest);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

