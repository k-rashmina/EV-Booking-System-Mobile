package com.example.evownerapp.util;

import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public final class JWTUtil {

    private JWTUtil() {}

    /** Returns the JWT payload as a JSONObject (null if malformed). */
    public static JSONObject decodePayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null; // header.payload[.signature]

            byte[] decoded = Base64.decode(parts[1],
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return new JSONObject(json);
        } catch (Exception e) {
            return null;
        }
    }

    /** Convenience: read a claim as string (returns null if missing). */
    public static String getClaim(String jwt, String claimName) {
        JSONObject payload = decodePayload(jwt);
        return payload != null && payload.has(claimName) ? payload.optString(claimName, null) : null;
    }

    /** Optional: check token expiry based on `exp` (seconds since epoch). */
    public static boolean isExpired(String jwt) {
        JSONObject payload = decodePayload(jwt);
        if (payload == null) return true;
        long exp = payload.optLong("exp", 0L);
        if (exp == 0L) return false; // no exp claim present
        long nowSeconds = System.currentTimeMillis() / 1000L;
        return nowSeconds >= exp;
    }
}
