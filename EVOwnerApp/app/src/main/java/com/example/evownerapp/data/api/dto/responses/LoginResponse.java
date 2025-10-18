package com.example.evownerapp.data.api.dto.responses;

public class LoginResponse {
    private final String token;
    private final String userRole;
    private final String userName;

    public LoginResponse(String token, String role, String fullName) {
        this.token = token;
        this.userRole = role;
        this.userName = fullName;
    }

    public String getToken()    { return token; }
    public String getUserRole()  { return userRole; }
    public String getUserName() { return userName; }
}

