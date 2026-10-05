package com.example.aeroalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PresetTimerAdapter extends RecyclerView.Adapter<PresetTimerAdapter.PresetViewHolder> {

    private List<TimerModel> timers;
    private OnPresetTimerClickListener listener;

    public interface OnPresetTimerClickListener {
        void onPresetTimerClick(TimerModel timer);
        void onPresetTimerEdit(TimerModel timer);
    }

    public PresetTimerAdapter(List<TimerModel> timers, OnPresetTimerClickListener listener) {
        this.timers = timers;
        this.listener = listener;
    }

    public void updateData(List<TimerModel> newTimers) {
        this.timers = newTimers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preset_timer, parent, false);
        return new PresetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresetViewHolder holder, int position) {
        TimerModel timer = timers.get(position);

        holder.tvName.setText(timer.getName());
        holder.tvDuration.setText(timer.getFormattedDuration());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPresetTimerClick(timer);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onPresetTimerEdit(timer);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onPresetTimerEdit(timer);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return timers != null ? timers.size() : 0;
    }

    static class PresetViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDuration;
        ImageButton btnEdit;

        public PresetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPresetTimerName);
            tvDuration = itemView.findViewById(R.id.tvPresetTimerDuration);
            btnEdit = itemView.findViewById(R.id.btnEditPresetTimer);
        }
    }
}
