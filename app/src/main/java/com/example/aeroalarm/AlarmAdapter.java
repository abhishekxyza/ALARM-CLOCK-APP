package com.example.aeroalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<AlarmModel> alarms;
    private OnAlarmClickListener listener;

    public interface OnAlarmClickListener {
        void onAlarmClick(AlarmModel alarm);
        void onAlarmToggle(AlarmModel alarm, boolean isChecked);
    }

    public AlarmAdapter(List<AlarmModel> alarms, OnAlarmClickListener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    public void updateData(List<AlarmModel> newAlarms) {
        this.alarms = newAlarms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmModel alarm = alarms.get(position);

        String time = String.format("%02d:%02d", alarm.getHour(), alarm.getMinute());
        holder.tvAlarmTime.setText(time);
        holder.tvAlarmAmPm.setText(alarm.getAmpm());
        holder.tvAlarmLabel.setText(alarm.getLabel() == null || alarm.getLabel().isEmpty() ? "Alarm" : alarm.getLabel());

        String daysStr = "ONCE";
        if (alarm.getDays() != null && !alarm.getDays().isEmpty()) {
            if (alarm.getDays().size() == 7) {
                daysStr = "EVERY DAY";
            } else if (alarm.getDays().size() == 5 && !alarm.getDays().contains(0) && !alarm.getDays().contains(6)) {
                daysStr = "WEEKDAYS";
            } else {
                daysStr = "CUSTOM";
            }
        }
        holder.tvAlarmDays.setText(daysStr);

        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(alarm.isActive());
        
        // Alpha for inactive
        holder.itemView.setAlpha(alarm.isActive() ? 1.0f : 0.5f);

        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onAlarmToggle(alarm, isChecked);
            holder.itemView.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        holder.itemView.setOnClickListener(v -> {
            listener.onAlarmClick(alarm);
        });
    }

    @Override
    public int getItemCount() {
        return alarms != null ? alarms.size() : 0;
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlarmTime, tvAlarmAmPm, tvAlarmLabel, tvAlarmDays;
        SwitchCompat switchAlarm;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlarmTime = itemView.findViewById(R.id.tvAlarmTime);
            tvAlarmAmPm = itemView.findViewById(R.id.tvAlarmAmPm);
            tvAlarmLabel = itemView.findViewById(R.id.tvAlarmLabel);
            tvAlarmDays = itemView.findViewById(R.id.tvAlarmDays);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
        }
    }
}
