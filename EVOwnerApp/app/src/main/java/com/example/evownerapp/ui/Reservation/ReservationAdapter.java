package com.example.evownerapp.ui.Reservation;

import com.example.evownerapp.data.models.Reservation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evownerapp.R;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private final List<Reservation> list;
    private final boolean isOngoing; // to decide which layout to inflate

    public ReservationAdapter(List<Reservation> list, boolean isOngoing) {
        this.list = list;
        this.isOngoing = isOngoing;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isOngoing
                ? R.layout.item_reservation_ongoing
                : R.layout.item_reservation_history;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation res = list.get(position);
        holder.tvTitle.setText(res.getStationName());
        holder.tvDate.setText(res.getDate());
        holder.tvTimeRange.setText(res.getTime());
        holder.tvStatus.setText(res.getStatus());

        // Color logic for status (optional)
        switch (res.getStatus()) {
            case "Pending":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "Approved":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
                break;
            case "Completed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_chip);
                break;
        }

        // QR button click
        holder.btnQr.setOnClickListener(v ->
                // TODO: open QR Activity or show dialog
                System.out.println("QR clicked for " + res.getStationName())
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvTimeRange, tvStatus;
        LinearLayout btnQr;
        ImageView imgQr;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnQr = itemView.findViewById(R.id.btnQr);
            imgQr = itemView.findViewById(R.id.imgQr);
        }
    }
}
