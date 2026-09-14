package com.example.aeroalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HolidayCountryAdapter extends RecyclerView.Adapter<HolidayCountryAdapter.CountryViewHolder> {

    private List<String> allCountries;
    private List<String> filteredCountries;
    private String selectedCountry;
    private OnCountrySelectListener listener;

    public interface OnCountrySelectListener {
        void onCountrySelect(String country);
    }

    public HolidayCountryAdapter(List<String> countries, String selectedCountry, OnCountrySelectListener listener) {
        this.allCountries = new ArrayList<>(countries);
        this.filteredCountries = new ArrayList<>(countries);
        this.selectedCountry = selectedCountry;
        this.listener = listener;
    }

    public void filter(String query) {
        filteredCountries.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredCountries.addAll(allCountries);
        } else {
            String lower = query.toLowerCase().trim();
            for (String c : allCountries) {
                if (c.toLowerCase().contains(lower)) {
                    filteredCountries.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setSelectedCountry(String selectedCountry) {
        this.selectedCountry = selectedCountry;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country_holiday, parent, false);
        return new CountryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder holder, int position) {
        String country = filteredCountries.get(position);

        holder.tvCountryName.setText(country);
        holder.rbCountrySelected.setChecked(country.equalsIgnoreCase(selectedCountry));

        // Section header letter
        String firstLetter = country.substring(0, 1).toUpperCase();
        if (position == 0) {
            holder.tvSectionHeader.setText(firstLetter);
            holder.tvSectionHeader.setVisibility(View.VISIBLE);
        } else {
            String prevFirstLetter = filteredCountries.get(position - 1).substring(0, 1).toUpperCase();
            if (!firstLetter.equalsIgnoreCase(prevFirstLetter)) {
                holder.tvSectionHeader.setText(firstLetter);
                holder.tvSectionHeader.setVisibility(View.VISIBLE);
            } else {
                holder.tvSectionHeader.setVisibility(View.GONE);
            }
        }

        holder.rowCountry.setOnClickListener(v -> {
            selectedCountry = country;
            notifyDataSetChanged();
            if (listener != null) listener.onCountrySelect(country);
        });
    }

    @Override
    public int getItemCount() {
        return filteredCountries != null ? filteredCountries.size() : 0;
    }

    static class CountryViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionHeader, tvCountryName;
        RadioButton rbCountrySelected;
        View rowCountry;

        public CountryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionHeader = itemView.findViewById(R.id.tvSectionHeader);
            tvCountryName = itemView.findViewById(R.id.tvCountryName);
            rbCountrySelected = itemView.findViewById(R.id.rbCountrySelected);
            rowCountry = itemView.findViewById(R.id.rowCountry);
        }
    }
}
