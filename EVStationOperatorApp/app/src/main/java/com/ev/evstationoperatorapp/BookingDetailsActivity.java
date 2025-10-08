package com.ev.evstationoperatorapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class BookingDetailsActivity extends AppCompatActivity {

    // --- UI Elements ---
    private TextView bookingIdTextView, ownerNameTextView, dateTimeTextView, stationLocationTextView, statusTextView;
    private Button finalizeButton;

    // --- Session & Network ---
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private String currentBookingId; // To store the booking ID for the finalize call

    // --- Constants ---
    private static final String PREFS_NAME = "OperatorPrefs";
    private static final String KEY_AUTH_TOKEN = "authToken";
    // IMPORTANT: Confirm this URL with your backend team
    private static final String FINALIZE_BOOKING_URL_BASE = "http://192.168.1.5:5148/api/Bookings/finalize/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Volley and SharedPreferences
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Link UI elements using the CORRECT IDs from your new XML
        bookingIdTextView = findViewById(R.id.textBookingId);
        ownerNameTextView = findViewById(R.id.textOwnerName);
        dateTimeTextView = findViewById(R.id.textReservationTime);
        stationLocationTextView = findViewById(R.id.textStationLocation);
        statusTextView = findViewById(R.id.textStatus);
        finalizeButton = findViewById(R.id.finalizeButton);

        // Get the JSON data passed from MainActivity
        String bookingDetailsJson = getIntent().getStringExtra("bookingDetailsJson");
        if (bookingDetailsJson != null && !bookingDetailsJson.isEmpty()) {
            parseAndDisplayBookingDetails(bookingDetailsJson);
        } else {
            Toast.makeText(this, "Could not load booking details.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if there's no data
        }

        finalizeButton.setOnClickListener(v -> {
            if (currentBookingId != null) {
                finalizeSession(currentBookingId);
            } else {
                Toast.makeText(this, "Cannot finalize: Booking ID is missing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Parses the JSON string and populates the TextViews.
     * @param jsonString The JSON response from the server.
     */
    private void parseAndDisplayBookingDetails(String jsonString) {
        try {
            JSONObject details = new JSONObject(jsonString);

            // IMPORTANT: Confirm these JSON keys with your backend team.
            // Using optString to avoid crashing if a key is missing.
            currentBookingId = details.optString("id", "N/A");
            String ownerName = details.optString("evOwnerName", "N/A");
            String reservationTime = details.optString("reservationTime", "N/A");
            String stationLocation = details.optString("stationLocation", "N/A");
            String status = details.optString("status", "N/A");

            bookingIdTextView.setText(currentBookingId);
            ownerNameTextView.setText(ownerName);
            dateTimeTextView.setText(formatIsoDate(reservationTime));
            stationLocationTextView.setText(stationLocation);
            statusTextView.setText(status);

        } catch (JSONException e) {
            Log.e("BookingDetailsActivity", "Error parsing booking JSON", e);
            Toast.makeText(this, "Error displaying details.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Makes an authenticated API call to finalize the booking session.
     * @param bookingId The ID of the booking to finalize.
     */
    private void finalizeSession(String bookingId) {
        String authToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null);
        if (authToken == null) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = FINALIZE_BOOKING_URL_BASE + bookingId;

        // A PUT request is often used for updates. Your backend might use POST.
        // We don't expect a JSON object in the response, just a success status, so we can use a StringRequest.
        // For simplicity, we stick with JsonObjectRequest and just check for a successful response.
        JsonObjectRequest finalizeRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null, // No request body needed for this action
                response -> {
                    // --- On Success ---
                    Toast.makeText(this, "Session Finalized Successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Close this activity and return to the main screen
                },
                error -> {
                    // --- On Failure ---
                    Log.e("BookingDetailsActivity", "Finalize Error: " + error.toString());
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    if (statusCode == 401) {
                        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to finalize session.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(finalizeRequest);
    }

    /**
     * Formats an ISO 8601 date string into a more readable format.
     * @param isoDateString The date string from the server (e.g., "2025-10-26T14:00:00Z").
     * @return A formatted string (e.g., "Oct 26, 2025, 02:00 PM").
     */
    private String formatIsoDate(String isoDateString) {
        if (isoDateString == null || isoDateString.equals("N/A")) {
            return "N/A";
        }
        // Input format (ISO 8601 UTC)
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        // Note: The 'Z' for Zulu time can sometimes cause issues, so we often handle it like this
        // or by ensuring the timezone is set correctly.
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


        // Desired output format, adjusted for local time
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault()); // Display in the device's local timezone

        try {
            Date date = inputFormat.parse(isoDateString.split("\\.")[0]); // Split to handle milliseconds if they exist
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDateString; // Return original string if parsing fails
        }
    }
}

