package com.example.evownerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evownerapp.core.AppExecutors;
import com.example.evownerapp.core.Result;
import com.example.evownerapp.data.api.endpoints.AuthService;
import com.example.evownerapp.data.api.dto.responses.LoginResponse;
import com.example.evownerapp.util.Preferences;
import com.example.evownerapp.ui.Dashboard.DashboardFragment;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button btnLogin;
    private TextView tvGoRegister;
    private ProgressBar progress;

    private AuthService authService;
    private Preferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String baseUrl = "http://192.168.1.5:5148/";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail       = findViewById(R.id.editTextEmail);
        editTextPassword  = findViewById(R.id.editTextPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvGoRegister= findViewById(R.id.tvGoRegister);
        progress    = findViewById(R.id.progress);

        authService = new AuthService(this, baseUrl);
        prefs       = new Preferences(this);

        // If already logged in, jump to dashboard
        if (prefs.getToken() != null) {
            openDashboardAndFinish();
            return;
        }

        btnLogin.setOnClickListener(v -> doLogin());
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void doLogin() {
        String email = editTextEmail.getText().toString().trim();
        String pw  = editTextPassword.getText().toString();

        if (email.isEmpty()) {
            editTextEmail.setError("Please Enter a Valid Email");
            editTextEmail.requestFocus();
            return;
        }
        if (pw.isEmpty()) {
            editTextPassword.setError("Please Enter a Password");
            editTextPassword.requestFocus();
            return;
        }

        setLoading(true);

        AppExecutors.io().execute(() -> {
            Result<LoginResponse> result = authService.login(email, pw);
            AppExecutors.main().post(() -> {
                setLoading(false);
                if (result.isSuccess()) {
                    LoginResponse r = result.getData();
                    // Save token & basic identity
                    prefs.setToken(r.getToken());
                    prefs.setCurrentName(r.getUserName());
                    Toast.makeText(this, "Welcome " + r.getUserName(), Toast.LENGTH_SHORT).show();
                    openDashboardAndFinish();
                } else {
                    String msg = result.getError() != null
                            ? result.getError().getMessage()
                            : "Login failed";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void openDashboardAndFinish() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        editTextEmail.setEnabled(!loading);
        editTextPassword.setEnabled(!loading);
    }
}
