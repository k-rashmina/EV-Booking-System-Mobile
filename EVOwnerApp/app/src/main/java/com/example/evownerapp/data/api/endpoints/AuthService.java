package com.example.evownerapp.data.api.endpoints;

import android.content.Context;

import com.example.evownerapp.core.Result;
import com.example.evownerapp.data.api.ApiClient;
import com.example.evownerapp.data.api.dto.responses.LoginResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AuthService {
    private final ApiClient client;

    public AuthService(Context ctx, String baseUrl) {
        this.client = new ApiClient(ctx, baseUrl);
    }

    public Result<LoginResponse> login(String email, String password) {
        try {
            // Build JSON body according to your API contract
            JSONObject body = new JSONObject()
                    .put("email", email)
                    .put("password", password);

            String response = client.postJson("api/Auth/login", body.toString());

            // Parse response (adjust to your API)
            JSONObject json = new JSONObject(response);
            if (!json.getString("token").isEmpty()) {
                String token = json.getString("token");
                String role = json.getString("role");
                String userName = json.getString("fullName");

                return Result.success(new LoginResponse(token, role, userName));
            } else {
                String message = json.optString("message", "Invalid credentials");
                return Result.error(new Exception(message));
            }
        } catch (IOException | JSONException e) {
            return Result.error(e);
        }
    }

    /** Registers a new user */
    public boolean register(String nic, String name, String email, String phone, String address, String vehicleModel, String licensePlate, String password) {
        try {
            JSONObject body = new JSONObject()
                    .put("nic", nic)
                    .put("fullName", name)
                    .put("email", email)
                    .put("phone", phone)
                    .put("address", address)
                    .put("role", "EVOwner")
                    .put("vehicleModel", vehicleModel)
                    .put("licensePlate", licensePlate)
                    .put("password", password);

            String response = client.postJson("api/Auth/register", body.toString());
            JSONObject json = new JSONObject(response);

            return json.getString("message").equals("EV Owner registered successfully.");
        } catch (IOException | JSONException e) {
            return false;
        }
    }
}

