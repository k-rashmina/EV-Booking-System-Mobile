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

    public Result<LoginResponse> login(String nic, String password) {
        try {
            // Build JSON body according to your API contract
            JSONObject body = new JSONObject()
                    .put("nic", nic)
                    .put("password", password);

            String response = client.postJson("auth/login", body.toString());

            // Parse response (adjust to your API)
            JSONObject json = new JSONObject(response);
            if (json.optBoolean("success", false)) {
                String token = json.getString("token");
                JSONObject user = json.optJSONObject("user");
                String userNic = user != null ? user.optString("nic", nic) : nic;
                String userName = user != null ? user.optString("name", "User") : "User";

                return Result.success(new LoginResponse(token, userNic, userName));
            } else {
                String message = json.optString("message", "Invalid credentials");
                return Result.error(new Exception(message));
            }
        } catch (IOException | JSONException e) {
            return Result.error(e);
        }
    }
}

