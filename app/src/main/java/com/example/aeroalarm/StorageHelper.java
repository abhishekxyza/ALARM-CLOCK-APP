package com.example.aeroalarm;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageHelper {
    private static final String PREF_NAME = "AeroAlarmPrefs";
    private static final String KEY_ALARMS = "alarms";
    private static final String KEY_OSHI_URL = "oshi_url";
    private static final String KEY_OSHI_LOCKED = "oshi_locked";
    private static final String KEY_SAVED_BARCODE = "saved_barcode";
    private static final String KEY_WORLD_CITIES = "world_cities";
    private static final String KEY_WALLPAPER_TYPE = "wallpaper_type";
    private static final String KEY_WALLPAPER_URI = "wallpaper_uri";

    public static void saveAlarms(Context context, List<AlarmModel> alarms) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarms);
        editor.putString(KEY_ALARMS, json);
        editor.apply();
    }

    public static List<AlarmModel> getAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_ALARMS, null);
        Type type = new TypeToken<ArrayList<AlarmModel>>() {}.getType();
        List<AlarmModel> alarms = gson.fromJson(json, type);
        if (alarms == null) {
            alarms = new ArrayList<>();
        }
        return alarms;
    }

    public static void setOshiCharacter(Context context, String url, boolean locked) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_OSHI_URL, url)
                .putBoolean(KEY_OSHI_LOCKED, locked)
                .apply();
    }

    public static String getOshiUrl(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_OSHI_URL, null);
    }

    public static boolean isOshiLocked(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_OSHI_LOCKED, false);
    }

    public static void saveBarcode(Context context, String barcode) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SAVED_BARCODE, barcode).apply();
    }

    public static String getSavedBarcode(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_SAVED_BARCODE, null);
    }

    public static void saveWorldCities(Context context, List<WorldCityModel> cities) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(cities);
        prefs.edit().putString(KEY_WORLD_CITIES, json).apply();
    }

    public static List<WorldCityModel> getWorldCities(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_WORLD_CITIES, null);
        Type type = new TypeToken<ArrayList<WorldCityModel>>() {}.getType();
        List<WorldCityModel> cities = gson.fromJson(json, type);
        if (cities == null) {
            cities = new ArrayList<>();
            // Add default initial cities
            cities.add(new WorldCityModel("Tokyo", "Japan", "Asia/Tokyo"));
            cities.add(new WorldCityModel("London", "United Kingdom", "Europe/London"));
            cities.add(new WorldCityModel("New York", "United States", "America/New_York"));
            saveWorldCities(context, cities);
        }
        return cities;
    }

    public static void saveWallpaper(Context context, String type, String uri) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_WALLPAPER_TYPE, type)
                .putString(KEY_WALLPAPER_URI, uri)
                .apply();
    }

    public static String getWallpaperType(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_WALLPAPER_TYPE, "default_black");
    }

    public static String getWallpaperUri(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_WALLPAPER_URI, null);
    }
}
