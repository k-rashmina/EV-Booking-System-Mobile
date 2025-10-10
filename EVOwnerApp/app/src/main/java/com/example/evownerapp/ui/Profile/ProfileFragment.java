package com.example.evownerapp.ui.Profile;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.evownerapp.R;
import com.example.evownerapp.data.api.endpoints.ProfileService;
import com.example.evownerapp.ui.Profile.ProfileFragment;
import com.example.evownerapp.util.Preferences;

public class ProfileFragment extends Fragment {

    private EditText etNic, etName, etEmail, etPhone, etAddress, etVehicleModel, etLicensePlate;
    private Button btnUpdate, btnDeactivate;
    private ImageView imgProfile;

    private ProfileService profileService;
    private Preferences prefs;

    public ProfileFragment() { /* required empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Button btnOpen = v.findViewById(R.id.btnOpenProfile);
        btnOpen.setOnClickListener(view ->
                startActivity(new Intent(requireContext(), ProfileActivity.class))
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        imgProfile     = findViewById(R.id.imgProfile);
        etNic          = findViewById(R.id.etProfileNic);
        etName         = findViewById(R.id.etProfileName);
        etEmail        = findViewById(R.id.etProfileEmail);
        etPhone        = findViewById(R.id.etProfilePhone);
        etAddress      = findViewById(R.id.etProfileAddress);
        etVehicleModel = findViewById(R.id.etProfileVehicleModel);
        etLicensePlate = findViewById(R.id.etProfileLicensePlate);
        btnUpdate      = findViewById(R.id.btnUpdateProfile);
        btnDeactivate  = findViewById(R.id.btnDeactivate);

        profileService = new ProfileService(this, "https://api.example.com/");
        prefs          = new Preferences(this);

        // Prefill NIC + cached name while loading full profile
        etName.setText(prefs.getCurrentName() != null ? prefs.getCurrentName() : "");

        loadProfile();

        btnUpdate.setOnClickListener(v -> onUpdate());
        btnDeactivate.setOnClickListener(v -> confirmDeactivate());
    }

    private void loadProfile() {
        // Optional: show a lightweight “loading” state
        btnUpdate.setEnabled(false);
        btnDeactivate.setEnabled(false);

        final String nic = etNic.getText().toString();

        AppExecutors.io().execute(() -> {
            Result<UserProfile> res = profileService.getProfile(nic);
            AppExecutors.main().post(() -> {
                btnUpdate.setEnabled(true);
                btnDeactivate.setEnabled(true);

                if (res.isSuccess() && res.getData() != null) {
                    bind(res.getData());
                } else {
                    // If server fails, keep the cached prefill and inform the user
                    Toast.makeText(this, "Could not load full profile; using cached values.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void bind(UserProfile p) {
        etNic.setText(nvl(p.getNic()));
        etName.setText(nvl(p.getName()));
        etEmail.setText(nvl(p.getEmail()));
        etPhone.setText(nvl(p.getPhone()));
        etAddress.setText(nvl(p.getAddress()));
        etVehicleModel.setText(nvl(p.getVehicleModel()));
        etLicensePlate.setText(nvl(p.getLicensePlate()));

        // Cache name for greetings elsewhere
        if (!TextUtils.isEmpty(p.getName())) {
            prefs.setCurrentName(p.getName());
        }
    }

    private void onUpdate() {
        final String nic    = etNic.getText().toString();
        final String name   = etName.getText().toString().trim();
        final String email  = etEmail.getText().toString().trim();
        final String phone  = etPhone.getText().toString().trim();
        final String addr   = etAddress.getText().toString().trim();
        final String model  = etVehicleModel.getText().toString().trim();
        final String plate  = etLicensePlate.getText().toString().trim();

        if (!validate(name, email, phone)) return;

        setUpdating(true);

        UserProfile payload = new UserProfile(nic, name, email, phone, addr, model, plate);

        AppExecutors.io().execute(() -> {
            Result<UserProfile> res = profileService.updateProfile(payload);
            AppExecutors.main().post(() -> {
                setUpdating(false);
                if (res.isSuccess()) {
                    // Re-bind in case the server normalized any fields
                    bind(res.getData() != null ? res.getData() : payload);
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = res.getError() != null ? res.getError().getMessage() : "Update failed";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void confirmDeactivate() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate account")
                .setMessage("Are you sure you want to deactivate your account? You will be logged out.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Deactivate", (DialogInterface dialog, int which) -> doDeactivate())
                .show();
    }

    private void doDeactivate() {
        setUpdating(true);

        final String nic = etNic.getText().toString();

        AppExecutors.io().execute(() -> {
            Result<Void> res = profileService.deactivate(nic);
            AppExecutors.main().post(() -> {
                setUpdating(false);
                if (res.isSuccess()) {
                    // Clear local auth & cached user info
                    prefs.clearToken();
                    prefs.setCurrentName(null);
                    Toast.makeText(this, "Account deactivated", Toast.LENGTH_SHORT).show();

                    // Navigate to login
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    String msg = res.getError() != null ? res.getError().getMessage() : "Deactivation failed";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean validate(String name, String email, String phone) {
        if (TextUtils.isEmpty(name))  { etName.setError("Name is required"); etName.requestFocus(); return false; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email is required"); etEmail.requestFocus(); return false; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email"); etEmail.requestFocus(); return false;
        }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Phone is required"); etPhone.requestFocus(); return false; }
        return true;
    }

    private void setUpdating(boolean updating) {
        btnUpdate.setEnabled(!updating);
        btnDeactivate.setEnabled(!updating);
        etName.setEnabled(!updating);
        etEmail.setEnabled(!updating);
        etPhone.setEnabled(!updating);
        etAddress.setEnabled(!updating);
        etVehicleModel.setEnabled(!updating);
        etLicensePlate.setEnabled(!updating);

        btnUpdate.setText(updating ? "Updating..." : getString(R.string.action_update));
    }

    private static String nvl(String s) { return s == null ? "" : s; }
}
