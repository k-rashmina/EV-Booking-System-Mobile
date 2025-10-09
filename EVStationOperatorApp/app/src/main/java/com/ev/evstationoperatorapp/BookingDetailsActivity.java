package com.ev.evstationoperatorapp;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private TextView stationNameTextView, startTimeTextView, endTimeTextView;
    private TextView slotTypeTextView, slotIdTextView, statusTextView;
    private Button finalizeButton;
    private ProgressDialog progressDialog;

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Finalizing Session...");
        progressDialog.setCancelable(false);

        String bookingDetailsJson = getIntent().getStringExtra("bookingDetailsJson");
        if (bookingDetailsJson != null && !bookingDetailsJson.isEmpty()) {
            parseAndDisplayBookingDetails(bookingDetailsJson);
        } else {
            Toast.makeText(this, "Could not load booking details.", Toast.LENGTH_LONG).show();
            finish();
        }

        finalizeButton.setOnClickListener(v -> {
            if (currentBookingId != null && !currentBookingId.equals("N/A")) {
                finalizeSession(currentBookingId);
            } else {
                Toast.makeText(this, "Cannot finalize: Booking ID is missing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        ownerNameTextView = findViewById(R.id.textOwnerName);
        nicTextView = findViewById(R.id.textNic);
        vehicleModelTextView = findViewById(R.id.textVehicleModel);
        licensePlateTextView = findViewById(R.id.textLicensePlate);
        stationNameTextView = findViewById(R.id.textStationName);
        startTimeTextView = findViewById(R.id.textStartTime);
        endTimeTextView = findViewById(R.id.textEndTime);
        slotTypeTextView = findViewById(R.id.textSlotType);
        slotIdTextView = findViewById(R.id.textSlotId);
        statusTextView = findViewById(R.id.textStatus);
        finalizeButton = findViewById(R.id.finalizeButton);
    }

    private void parseAndDisplayBookingDetails(String jsonString) {
        try {
            JSONObject details = new JSONObject(jsonString);
            currentBookingId = details.optString("bookingId", "N/A");
            String ownerName = details.optString("evOwnerFullName", "N/A");
            String ownerNic = details.optString("nic", "N/A");
            String vehicleModel = details.optString("vehicleModel", "N/A");
            String licensePlate = details.optString("licensePlate", "N/A");
            String stationName = details.optString("stationName", "N/A");
            String startTime = details.optString("startTimeLocal", "N/A");
            String endTime = details.optString("endTimeLocal", "N/A");
            String slotType = details.optString("slotType", "N/A");
            String slotId = details.optString("slotId", "N/A");
            String status = details.optString("status", "N/A");

            ownerNameTextView.setText(Html.fromHtml("<b>Owner Name:</b> " + ownerName));
            nicTextView.setText(Html.fromHtml("<b>NIC:</b> " + ownerNic));
            vehicleModelTextView.setText(Html.fromHtml("<b>Vehicle Model:</b> " + vehicleModel));
            licensePlateTextView.setText(Html.fromHtml("<b>License Plate:</b> " + licensePlate));
            stationNameTextView.setText(Html.fromHtml("<b>Station:</b> " + stationName));
            startTimeTextView.setText(Html.fromHtml("<b>Start Time:</b> " + formatIsoDateTime(startTime)));
            endTimeTextView.setText(Html.fromHtml("<b>End Time:</b> " + formatIsoDateTime(endTime)));
            slotTypeTextView.setText(Html.fromHtml("<b>Slot Type:</b> " + slotType));
            slotIdTextView.setText(Html.fromHtml("<b>Slot ID:</b> " + slotId));

            // --- NEW LOGIC FOR SELECTIVE STATUS COLOR ---
            String statusHtml;
            if ("Approved".equalsIgnoreCase(status)) {
                // Get the hex color string for our red color resource
                String redColorHex = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, R.color.status_approved)));
                statusHtml = "<b>Status:</b> <font color='" + redColorHex + "'>" + status + "</font>";
            } else {
                // For any other status, just make it bold without a special color
                statusHtml = "<b>Status:</b> " + status;
            }
            // Set the text using the generated HTML
            statusTextView.setText(Html.fromHtml(statusHtml));
            // --- END OF NEW LOGIC ---

        } catch (JSONException e) {
            Log.e("BookingDetailsActivity", "Error parsing booking JSON", e);
            Toast.makeText(this, "Error displaying details.", Toast.LENGTH_SHORT).show();
        }
    }

    private void finalizeSession(String bookingId) {
        progressDialog.show();
        String authToken = sharedPreferences.getString(ApiConfig.KEY_AUTH_TOKEN, null);
        if (authToken == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "/api/Operator/finalize/" + bookingId;

        JsonObjectRequest finalizeRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Session Finalized Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
                    progressDialog.dismiss();
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
        Date date = parseIsoDate(isoDateString);
        if (date == null) return isoDateString;
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());
        return outputFormat.format(date);
    }

    private Date parseIsoDate(String isoDateString) {
        SimpleDateFormat inputFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        inputFormatWithMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return inputFormatWithMillis.parse(isoDateString);
        } catch (ParseException e) {
            try {
                SimpleDateFormat inputFormatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                inputFormatWithoutMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
                return inputFormatWithoutMillis.parse(isoDateString);
            } catch (ParseException pe) {
                pe.printStackTrace();
                return null;
            }
        }
    }
}

