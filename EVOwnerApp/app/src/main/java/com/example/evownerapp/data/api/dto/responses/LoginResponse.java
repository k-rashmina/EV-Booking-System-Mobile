package com.example.evownerapp.data.api.dto.responses;

public class LoginResponse {
    private final String token;
    private final String userNic;
    private final String userName;

    public LoginResponse(String token, String userNic, String userName) {
        this.token = token;
        this.userNic = userNic;
        this.userName = userName;
    }

    public String getToken()    { return token; }
    public String getUserNic()  { return userNic; }
    public String getUserName() { return userName; }
}

