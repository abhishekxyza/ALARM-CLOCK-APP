package com.example.aeroalarm;

public class WorldCityModel {
    private String cityName;
    private String countryName;
    private String timeZoneId; // e.g. "Asia/Tokyo"

    public WorldCityModel() {
    }

    public WorldCityModel(String cityName, String countryName, String timeZoneId) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.timeZoneId = timeZoneId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
