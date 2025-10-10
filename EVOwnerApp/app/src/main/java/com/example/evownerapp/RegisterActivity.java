package com.example.evownerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evownerapp.R;
import com.example.evownerapp.core.AppExecutors;
import com.example.evownerapp.core.Result;
import com.example.evownerapp.data.api.endpoints.AuthService;
import com.example.evownerapp.data.api.dto.responses.LoginResponse;
import com.example.evownerapp.ui.Dashboard.DashboardFragment;
import com.example.evownerapp.util.Preferences;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNic, etName, etEmail, etPhone, etAddress, etVehicleModel, etLicensePlate, etPassword;
    private Button btnCreateAccount;
    private TextView tvGoLogin;

    private AuthService authService;
    private Preferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String baseUrl = "http://192.168.1.5:5148/";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNic = findViewById(R.id.etRegNic);
        etName = findViewById(R.id.etRegName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etRegPhone);
        etAddress = findViewById(R.id.etRegAddress);
        tvGoLogin = findViewById(R.id.tvGoLogin);
        etVehicleModel = findViewById(R.id.etRegVehicleModel);
        etLicensePlate = findViewById(R.id.etRegLicensePlate);
        etPassword = findViewById(R.id.etRegPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        authService = new AuthService(this, baseUrl);
        prefs       = new Preferences(this);

        btnCreateAccount.setOnClickListener(v -> onCreateAccount());

        tvGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }

    private void onCreateAccount() {
        final String nic  = trim(etNic);
        final String name = trim(etName);
        final String email= trim(etEmail);
        final String phone= trim(etPhone);
        final String Address= trim(etAddress);
        final String vehicleModel= trim(etVehicleModel);
        final String licensePlate= trim(etLicensePlate);
        final String pass = etPassword.getText().toString();

        if (!validate(nic, name, email, phone, Address, vehicleModel, licensePlate, pass)) return;

        setLoading(true);

        AppExecutors.io().execute(() -> {
            boolean result = authService.register(nic, name, email, phone, Address, vehicleModel, licensePlate, pass);

            AppExecutors.main().post(() -> {
                setLoading(false);
                if (result) {
                    Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
                    openLoginAndFinish();
                } else {
                    String msg = "Registration failed";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean validate(String nic, String name, String email, String phone, String Address, String vehicleModel, String licensePlate, String pass) {
        if (TextUtils.isEmpty(nic))  { etNic.setError("NIC is required"); etNic.requestFocus(); return false; }
        if (TextUtils.isEmpty(name)) { etName.setError("Name is required"); etName.requestFocus(); return false; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email is required"); etEmail.requestFocus(); return false; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email"); etEmail.requestFocus(); return false;
        }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Phone is required"); etPhone.requestFocus(); return false; }
        if (TextUtils.isEmpty(Address)) { etAddress.setError("Address is required"); etAddress.requestFocus(); return false; }
        if (TextUtils.isEmpty(vehicleModel)) { etVehicleModel.setError("Vehicle Model is required"); etVehicleModel.requestFocus(); return false; }
        if (TextUtils.isEmpty(licensePlate)) { etLicensePlate.setError("License Plate is required"); etLicensePlate.requestFocus(); return false; }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus(); return false;
        }
        return true;
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
        etNic.setEnabled(!loading);
        etName.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPhone.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        btnCreateAccount.setText(loading ? "Creating..." : getString(R.string.action_create_account));
    }

    private static String trim(EditText et) { return et.getText().toString().trim(); }

    private void openLoginAndFinish() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
