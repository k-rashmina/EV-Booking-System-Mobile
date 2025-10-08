package com.ev.evstationoperatorapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private TextView ownerNameTextView, nicTextView, vehicleModelTextView, licensePlateTextView;
    private TextView stationNameTextView, bookingDateTextView, startTimeTextView, endTimeTextView;
    private TextView slotTypeTextView, slotIdTextView, statusTextView;
    private Button finalizeButton;

    // --- Session & Network ---
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private String currentBookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        initializeViews();
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);

        String bookingDetailsJson = getIntent().getStringExtra("bookingDetailsJson");
        if (bookingDetailsJson != null && !bookingDetailsJson.isEmpty()) {
            parseAndDisplayBookingDetails(bookingDetailsJson);
        } else {
            Toast.makeText(this, "Could not load booking details.", Toast.LENGTH_LONG).show();
            finish();
        }

        finalizeButton.setOnClickListener(v -> {
            if (currentBookingId != null) {
                finalizeSession(currentBookingId);
            } else {
                Toast.makeText(this, "Cannot finalize: Booking ID is missing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        // Owner & Vehicle
        ownerNameTextView = findViewById(R.id.textOwnerName);
        nicTextView = findViewById(R.id.textNic);
        vehicleModelTextView = findViewById(R.id.textVehicleModel);
        licensePlateTextView = findViewById(R.id.textLicensePlate);
        // Booking
        stationNameTextView = findViewById(R.id.textStationName);
        bookingDateTextView = findViewById(R.id.textBookingDate);
        startTimeTextView = findViewById(R.id.textStartTime);
        endTimeTextView = findViewById(R.id.textEndTime);
        slotTypeTextView = findViewById(R.id.textSlotType);
        slotIdTextView = findViewById(R.id.textSlotId);
        statusTextView = findViewById(R.id.textStatus);
        // Button
        finalizeButton = findViewById(R.id.finalizeButton);
    }

    private void parseAndDisplayBookingDetails(String jsonString) {
        try {
            JSONObject response = new JSONObject(jsonString);

            // --- Parse Nested Objects ---
            JSONObject booking = response.optJSONObject("bookingDetails");
            JSONObject station = response.optJSONObject("stationDetails");
            JSONObject owner = response.optJSONObject("evOwnerProfile");

            if (booking == null || station == null || owner == null) {
                Toast.makeText(this, "Incomplete booking data received.", Toast.LENGTH_LONG).show();
                return;
            }

            // --- Populate Owner & Vehicle Info ---
            ownerNameTextView.setText(Html.fromHtml("<b>Owner Name:</b> " + owner.optString("fullName", "N/A")));
            nicTextView.setText(Html.fromHtml("<b>NIC:</b> " + owner.optString("nic", "N/A")));
            vehicleModelTextView.setText(Html.fromHtml("<b>Vehicle:</b> " + owner.optString("vehicleModel", "N/A")));
            licensePlateTextView.setText(Html.fromHtml("<b>License Plate:</b> " + owner.optString("licensePlate", "N/A")));

            // --- Populate Booking Info ---
            currentBookingId = booking.optString("id");
            stationNameTextView.setText(Html.fromHtml("<b>Station:</b> " + station.optString("stationName", "N/A")));
            bookingDateTextView.setText(Html.fromHtml("<b>Date:</b> " + formatIsoDateOnly(booking.optString("bookingDate"))));
            startTimeTextView.setText(Html.fromHtml("<b>Start Time:</b> " + formatIsoDateTime(booking.optString("startTime"))));
            endTimeTextView.setText(Html.fromHtml("<b>End Time:</b> " + formatIsoDateTime(booking.optString("endTime"))));
            slotTypeTextView.setText(Html.fromHtml("<b>Slot Type:</b> " + booking.optString("slotType", "N/A")));
            slotIdTextView.setText(Html.fromHtml("<b>Slot ID:</b> " + booking.optString("slotId", "N/A")));
            statusTextView.setText(Html.fromHtml("<b>Status:</b> " + booking.optString("status", "N/A")));


        } catch (JSONException e) {
            Log.e("BookingDetailsActivity", "Error parsing booking JSON", e);
            Toast.makeText(this, "Error displaying details.", Toast.LENGTH_SHORT).show();
        }
    }

    private void finalizeSession(String bookingId) {
        String authToken = sharedPreferences.getString(ApiConfig.KEY_AUTH_TOKEN, null);
        if (authToken == null) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "/api/Bookings/finalize/" + bookingId;

        JsonObjectRequest finalizeRequest = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    Toast.makeText(this, "Session Finalized Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
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

    private String formatIsoDateTime(String isoDateString) {
        if (isoDateString == null || isoDateString.equals("N/A")) return "N/A";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());
        try {
            Date date = inputFormat.parse(isoDateString.split("\\.")[0]);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDateString;
        }
    }

    private String formatIsoDateOnly(String isoDateString) {
        if (isoDateString == null || isoDateString.equals("N/A")) return "N/A";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());
        try {
            Date date = inputFormat.parse(isoDateString.split("\\.")[0]);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDateString;
        }
    }
}

