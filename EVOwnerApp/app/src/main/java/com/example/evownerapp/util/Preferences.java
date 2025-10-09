package com.example.evownerapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static final String NAME = "ev_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_NIC   = "current_nic";
    private static final String KEY_NAME  = "current_name";

    private final SharedPreferences sp;

    public Preferences(Context ctx) {
        this.sp = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void setToken(String token) {
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sp.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        sp.edit().remove(KEY_TOKEN).apply();
    }

    public void setCurrentNic(String nic) { sp.edit().putString(KEY_NIC, nic).apply(); }
    public String getCurrentNic()         { return sp.getString(KEY_NIC, null); }

    public void setCurrentName(String name) { sp.edit().putString(KEY_NAME, name).apply(); }
    public String getCurrentName()          { return sp.getString(KEY_NAME, null); }
}
