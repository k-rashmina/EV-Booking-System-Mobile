package com.ev.evstationoperatorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // --- UI Elements ---
    private TextView welcomeTextView;
    private Button scanQrButton;
    private Button logoutButton;

    // --- Session & Network ---
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    // --- Constants ---
    private static final String PREFS_NAME = "OperatorPrefs";
    private static final String KEY_OPERATOR_NAME = "operatorName";
    private static final String KEY_AUTH_TOKEN = "authToken";
    // IMPORTANT: Confirm this URL with your backend team
    private static final String BOOKING_DETAILS_URL_BASE = "http://10.0.2.2:5148/api/Bookings/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Link UI Elements
        welcomeTextView = findViewById(R.id.welcomeTextView);
        scanQrButton = findViewById(R.id.scanQrButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Set Welcome Message
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String operatorName = sharedPreferences.getString(KEY_OPERATOR_NAME, "Operator");
        welcomeTextView.setText("Welcome, " + operatorName + "!");

        // Set up Logout Button
        logoutButton.setOnClickListener(v -> logout());

        // Set up Scan Button
        scanQrButton.setOnClickListener(v -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
            intentIntegrator.setPrompt("Scan a Booking QR Code");
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.initiateScan();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() != null) {
                String bookingId = intentResult.getContents();
                // A QR code was successfully scanned, now fetch its details from the server
                fetchBookingDetails(bookingId);
            } else {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Fetches booking details from the server using an authenticated GET request.
     * @param bookingId The ID of the booking to fetch.
     */
    private void fetchBookingDetails(String bookingId) {
        // Retrieve the saved authentication token
        String authToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null);
        if (authToken == null) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            logout();
            return;
        }

        String url = BOOKING_DETAILS_URL_BASE + bookingId;

        // Create a custom request to include the Authorization header
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, // No request body for a GET request
                response -> {
                    // --- On Success ---
                    Log.d("MainActivity", "Booking Details Response: " + response.toString());
                    Intent intent = new Intent(MainActivity.this, BookingDetailsActivity.class);
                    // Pass the entire JSON response to the next activity
                    intent.putExtra("bookingDetailsJson", response.toString());
                    startActivity(intent);
                },
                error -> {
                    // --- On Failure ---
                    Log.e("MainActivity", "Fetch Booking Error: " + error.toString());
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;

                    if (statusCode == 404) {
                        Toast.makeText(this, "Booking not found.", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 401) {
                        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                        logout();
                    } else {
                        Toast.makeText(this, "Error fetching booking details.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // This is where we add the authentication token to the request header
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Logs the user out by clearing SharedPreferences and returning to LoginActivity.
     */
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

