package com.example.evownerapp.ui.Reservation;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evownerapp.R;

import java.util.ArrayList;
import java.util.List;

public class ReservationFragment extends Fragment {


    private RecyclerView rvOngoing, rvHistory;

    public ReservationsFragment() { /* required empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rvOngoing = v.findViewById(R.id.rvOngoing);
        rvHistory = v.findViewById(R.id.rvHistory);

        // 1) independent LayoutManagers
        rvOngoing.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        rvOngoing.setHasFixedSize(true);
        rvHistory.setHasFixedSize(true);

        // 2) stub data (replace with DB/API later)
        List<ReservationItem> ongoing = new ArrayList<>();
        ongoing.add(new ReservationItem("Colombo EV Hub", "2025-10-11", "8.00 – 8.30 AM", "Pending"));
        ongoing.add(new ReservationItem("Borella Station", "2025-10-12", "9.00 – 9.45 AM", "Approved"));

        List<ReservationItem> history = new ArrayList<>();
        history.add(new ReservationItem("Galle Road Station", "2025-09-12", "10.00 – 10.30 AM", "Completed"));
        history.add(new ReservationItem("Union Place", "2025-09-02", "11.00 – 11.30 AM", "Completed"));

        // 3) adapters (reuse the adapter we created earlier)
        ReservationAdapter ongoingAdapter = new ReservationAdapter(ongoing, true);
        ReservationAdapter historyAdapter = new ReservationAdapter(history, false);

        rvOngoing.setAdapter(ongoingAdapter);
        rvHistory.setAdapter(historyAdapter);
    }

    // Simple model for this fragment (use your shared model if you already have one)
    public static class ReservationItem {
        private final String stationName;
        private final String date;
        private final String time;
        private final String status;

        public ReservationItem(String stationName, String date, String time, String status) {
            this.stationName = stationName; this.date = date; this.time = time; this.status = status;
        }
        public String getStationName() { return stationName; }
        public String getDate()        { return date; }
        public String getTime()        { return time; }
        public String getStatus()      { return status; }
    }
}
