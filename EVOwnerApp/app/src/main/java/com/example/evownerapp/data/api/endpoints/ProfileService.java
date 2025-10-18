package com.example.evownerapp.data.api.endpoints;

import android.content.Context;

import com.example.evownerapp.core.Result;
import com.example.evownerapp.data.api.ApiClient;
import com.example.evownerapp.data.api.dto.responses.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ProfileService {
    private final ApiClient client;

    public ProfileService(Context ctx, String baseUrl) {
        this.client = new ApiClient(ctx, baseUrl);
    }

        public Result<UserProfile> getProfile(String nic) {
        try {
            // e.g. GET /users/{nic}
            String body = client.get("users/" + nic);
            JSONObject json = new JSONObject(body);

            // Contract A: { success: true, user: { ... } }
            JSONObject user = json.optJSONObject("user");
            if (json.optBoolean("success", user != null) && user != null) {
                return Result.success(parseUser(user));
            }

            // Contract B: flat user
            if (json.has("nic")) {
                return Result.success(parseUser(json));
            }

            String message = json.optString("message", "Failed to load profile");
            return Result.error(new Exception(message));
        } catch (IOException | JSONException e) {
            return Result.error(e);
        }
    }

    public Result<UserProfile> updateProfile(UserProfile p) {
        try {
            JSONObject body = new JSONObject()
                    .put("nic", p.getNic())
                    .put("name", p.getName())
                    .put("email", p.getEmail())
                    .put("phone", p.getPhone())
                    .put("address", p.getAddress())
                    .put("vehicleModel", p.getVehicleModel())
                    .put("licensePlate", p.getLicensePlate());

            // e.g. PUT /users/{nic}
            String resp = client.postJson("users/" + p.getNic(), body.toString()); // if your API uses PUT, add a putJson method similarly
            JSONObject json = new JSONObject(resp);

            boolean ok = json.optBoolean("success", false)
                    || "ok".equalsIgnoreCase(json.optString("status"));

            if (ok) {
                JSONObject user = json.optJSONObject("user");
                return Result.success(user != null ? parseUser(user) : p);
            } else {
                String msg = json.optString("message", "Update failed");
                return Result.error(new Exception(msg));
            }
        } catch (IOException | JSONException e) {
            return Result.error(e);
        }
    }

    public Result<Void> deactivate(String nic) {
        try {
            // e.g. POST /users/{nic}/deactivate
            JSONObject body = new JSONObject().put("nic", nic);
            String resp = client.postJson("users/" + nic + "/deactivate", body.toString());
            JSONObject json = new JSONObject(resp);

            boolean ok = json.optBoolean("success", false)
                    || "ok".equalsIgnoreCase(json.optString("status"));

            if (ok) return Result.success(null);
            String msg = json.optString("message", "Deactivation failed");
            return Result.error(new Exception(msg));
        } catch (IOException | JSONException e) {
            return Result.error(e);
        }
    }

    private static UserProfile parseUser(JSONObject u) {
        return new UserProfile(
                u.optString("nic", ""),
                u.optString("name", ""),
                u.optString("email", ""),
                u.optString("phone", ""),
                u.optString("address", ""),
                u.optString("vehicleModel", ""),
                u.optString("licensePlate", "")
        );
    }
}

