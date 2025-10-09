package com.example.evownerapp.ui.Reservation;

import com.example.evownerapp.databinding.ActivityMainBinding;
import com.example.evownerapp.data.models.Reservation;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evownerapp.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ReservationFragment extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView rvOngoing, rvHistory;
        ReservationAdapter ongoingAdapter, historyAdapter;
        ActivityMainBinding binding;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_reservation);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.appBarMain.fab.setOnClickListener(   new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });

        rvOngoing = findViewById(R.id.rvOngoing);
        rvHistory = findViewById(R.id.rvHistory);

        // --- 1) Set up LayoutManagers ---
        rvOngoing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // --- 2) Optional: improve performance if item size doesn't change ---
        rvOngoing.setHasFixedSize(true);
        rvHistory.setHasFixedSize(true);

        // --- 3) Create dummy data (replace with API/DB later) ---
        List<Reservation> ongoingList = new ArrayList<>();
        ongoingList.add(new Reservation("Colombo EV Hub", "2025-09-28", "8.00 – 8.30 AM", "Pending"));
        ongoingList.add(new Reservation("Colombo EV Hub", "2025-09-28", "8.00 – 8.30 AM",  "Approved"));
        ongoingList.add(new Reservation("Colombo EV Hub", "2025-09-28", "8.00 – 8.30 AM", "Pending"));

        List<Reservation> historyList = new ArrayList<>();
        historyList.add(new Reservation("Colombo EV Hub", "2025-08-28", "8.00 – 8.30 AM", "Completed"));
        historyList.add(new Reservation("Colombo EV Hub", "2025-08-18", "8.00 – 8.30 AM", "Completed"));
        historyList.add(new Reservation("Colombo EV Hub", "2025-08-08", "8.00 – 8.30 AM", "Completed"));

        // --- 4) Create adapters ---
        ongoingAdapter = new ReservationAdapter(ongoingList, true);
        historyAdapter = new ReservationAdapter(historyList, false);

        // --- 5) Attach adapters ---
        rvOngoing.setAdapter(ongoingAdapter);
        rvHistory.setAdapter(historyAdapter);
    }
}
