package com.example.aeroalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StopwatchLapAdapter extends RecyclerView.Adapter<StopwatchLapAdapter.LapViewHolder> {

    public static class LapItem {
        public int lapNumber;
        public long lapTimeMs;
        public long totalTimeMs;

        public LapItem(int lapNumber, long lapTimeMs, long totalTimeMs) {
            this.lapNumber = lapNumber;
            this.lapTimeMs = lapTimeMs;
            this.totalTimeMs = totalTimeMs;
        }
    }

    private List<LapItem> lapList;

    public StopwatchLapAdapter(List<LapItem> lapList) {
        this.lapList = lapList;
    }

    public void updateData(List<LapItem> newLapList) {
        this.lapList = newLapList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stopwatch_lap, parent, false);
        return new LapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LapViewHolder holder, int position) {
        LapItem item = lapList.get(position);

        holder.tvLapNumber.setText(String.format(Locale.US, "Lap %d", item.lapNumber));
        holder.tvLapTime.setText(formatTime(item.lapTimeMs));
        holder.tvTotalTime.setText(formatTime(item.totalTimeMs));
    }

    private String formatTime(long ms) {
        long minutes = (ms / 1000) / 60;
        long seconds = (ms / 1000) % 60;
        long hundredths = (ms % 1000) / 10;
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths);
    }

    @Override
    public int getItemCount() {
        return lapList != null ? lapList.size() : 0;
    }

    static class LapViewHolder extends RecyclerView.ViewHolder {
        TextView tvLapNumber, tvLapTime, tvTotalTime;

        public LapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLapNumber = itemView.findViewById(R.id.tvLapNumber);
            tvLapTime = itemView.findViewById(R.id.tvLapTime);
            tvTotalTime = itemView.findViewById(R.id.tvTotalTime);
        }
    }
}
