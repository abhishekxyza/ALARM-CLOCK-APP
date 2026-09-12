package com.example.aeroalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WorldClockAdapter extends RecyclerView.Adapter<WorldClockAdapter.CityViewHolder> {

    private List<WorldCityModel> cities;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(WorldCityModel city);
        void onCityDelete(WorldCityModel city);
    }

    public WorldClockAdapter(List<WorldCityModel> cities, OnCityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    public void updateData(List<WorldCityModel> newCities) {
        this.cities = newCities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_world_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        WorldCityModel city = cities.get(position);

        holder.tvCityName.setText(city.getCityName());

        TimeZone targetTz = TimeZone.getTimeZone(city.getTimeZoneId());
        TimeZone localTz = TimeZone.getDefault();

        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.US);
        timeFormat.setTimeZone(targetTz);

        SimpleDateFormat ampmFormat = new SimpleDateFormat("a", Locale.US);
        ampmFormat.setTimeZone(targetTz);

        holder.tvCityTime.setText(timeFormat.format(now));
        holder.tvCityAmPm.setText(ampmFormat.format(now));

        // Calculate time difference in hours
        long localOffset = localTz.getOffset(now.getTime());
        long targetOffset = targetTz.getOffset(now.getTime());
        double diffHours = (targetOffset - localOffset) / (1000.0 * 60.0 * 60.0);

        String offsetStr;
        if (diffHours == 0) {
            offsetStr = "Same time as local";
        } else if (diffHours > 0) {
            offsetStr = String.format(Locale.US, "+%.1f hrs ahead", diffHours).replace(".0", "");
        } else {
            offsetStr = String.format(Locale.US, "%.1f hrs behind", diffHours).replace(".0", "");
        }
        holder.tvCityOffset.setText(offsetStr);

        holder.btnDeleteCity.setOnClickListener(v -> {
            if (listener != null) listener.onCityDelete(city);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCityClick(city);
        });
    }

    @Override
    public int getItemCount() {
        return cities != null ? cities.size() : 0;
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName, tvCityOffset, tvCityTime, tvCityAmPm;
        ImageButton btnDeleteCity;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            tvCityOffset = itemView.findViewById(R.id.tvCityOffset);
            tvCityTime = itemView.findViewById(R.id.tvCityTime);
            tvCityAmPm = itemView.findViewById(R.id.tvCityAmPm);
            btnDeleteCity = itemView.findViewById(R.id.btnDeleteCity);
        }
    }
}
