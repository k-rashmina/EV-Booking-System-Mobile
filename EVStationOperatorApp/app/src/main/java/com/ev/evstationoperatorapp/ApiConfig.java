package com.ev.evstationoperatorapp;

public class ApiConfig {

    /**
     * This is the single source of truth for the server's base URL.
     */
    public static final String BASE_URL = "http://192.168.1.5:5148";

    // --- SharedPreferences Constants ---
    public static final String PREFS_NAME = "OperatorPrefs";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_OPERATOR_NAME = "operatorName";
    public static final String KEY_AUTH_TOKEN = "authToken";

    // --- Role Constants ---
    public static final String REQUIRED_ROLE = "StationOperator";
    public static final String KEY_ROLE = "role";

}

