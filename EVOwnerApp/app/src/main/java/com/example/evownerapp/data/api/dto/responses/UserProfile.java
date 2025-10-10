package com.example.evownerapp.data.api.dto.responses;

public class UserProfile {
    private String nic;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String vehicleModel;
    private String licensePlate;

    public UserProfile(String nic, String name, String email, String phone,
                       String address, String vehicleModel, String licensePlate) {
        this.nic = nic;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.vehicleModel = vehicleModel;
        this.licensePlate = licensePlate;
    }

    public String getNic() { return nic; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getVehicleModel() { return vehicleModel; }
    public String getLicensePlate() { return licensePlate; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
}

