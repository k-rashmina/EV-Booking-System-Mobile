package com.example.evownerapp.data.api;

import android.content.Context;

import com.example.evownerapp.util.Preferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {
    private final String baseUrl;
    private final Preferences prefs;

    public ApiClient(Context ctx, String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.prefs = new Preferences(ctx);
    }

    public String postJson(String path, String jsonBody) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        try {
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            addAuth(c);
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
            }
            return readBody(c);
        } finally {
            c.disconnect();
        }
    }

    public String get(String path) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        try {
            c.setRequestMethod("GET");
            addAuth(c);
            return readBody(c);
        } finally {
            c.disconnect();
        }
    }

    private void addAuth(HttpURLConnection c) {
        String token = prefs.getToken();
        if (token != null) {
            c.setRequestProperty("Authorization", "Bearer " + token);
        }
    }

    private String readBody(HttpURLConnection c) throws IOException {
        InputStream is = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
        return bos.toString("UTF-8");
    }
}

