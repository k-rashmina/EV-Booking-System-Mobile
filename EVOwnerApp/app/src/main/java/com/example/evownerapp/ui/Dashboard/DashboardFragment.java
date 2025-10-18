package com.example.evownerapp.ui.Dashboard;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.evownerapp.R;
import com.example.evownerapp.ui.MapActivity;

public class DashboardFragment extends Fragment {

    public DashboardFragment() { /* required empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Button btnOpenMap     = v.findViewById(R.id.btnOpenMap);
        Button btnMyBookings  = v.findViewById(R.id.btnMyBookings);

        // Optional tiles
        LinearLayout tilePending  = v.findViewById(R.id.tilePending);
        LinearLayout tileApproved = v.findViewById(R.id.tileApproved);

        btnOpenMap.setOnClickListener(view ->
                startActivity(new Intent(requireContext(), MapActivity.class))
        );

        btnMyBookings.setOnClickListener(view ->
                Navigation.findNavController(view).navigate(R.id.nav_reservation)
        );

        if (tilePending != null) {
            tilePending.setOnClickListener(view ->
                    Navigation.findNavController(view).navigate(R.id.nav_reservation));
        }
        if (tileApproved != null) {
            tileApproved.setOnClickListener(view ->
                    Navigation.findNavController(view).navigate(R.id.nav_reservation));
        }
    }
}