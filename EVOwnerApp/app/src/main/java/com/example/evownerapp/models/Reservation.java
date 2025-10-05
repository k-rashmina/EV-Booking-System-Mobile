package com.example.evownerapp.models;

public class Reservation {
    private String stationName;
    private String date;
    private String time;
    private String status;

    public Reservation(String stationName, String date, String time, String status) {
        this.stationName = stationName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getStationName() { return stationName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
}

