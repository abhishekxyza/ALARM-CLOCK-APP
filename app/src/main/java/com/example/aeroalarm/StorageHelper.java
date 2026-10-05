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
    private static final String KEY_PRESET_TIMERS = "preset_timers";
    private static final String KEY_TIMER_SOUND_ENABLED = "timer_sound_enabled";
    private static final String KEY_STOPWATCH_SOUND_ENABLED = "stopwatch_sound_enabled";
    private static final String KEY_AUTO_SET_TIME = "auto_set_time";
    private static final String KEY_USE_24_HOUR = "use_24_hour";
    private static final String KEY_AUTO_TIME_ZONE = "auto_time_zone";
    private static final String KEY_SYSTEM_DUAL_CLOCK = "system_dual_clock";
    private static final String KEY_HOME_CITY = "home_city";
    private static final String KEY_HOLIDAY_COUNTRY = "holiday_country";
    private static final String KEY_TIMER_TONE = "timer_tone";
    private static final String KEY_CLOCK_DISPLAY_MODE = "clock_display_mode";

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

    public static void savePresetTimers(Context context, List<TimerModel> timers) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(timers);
        prefs.edit().putString(KEY_PRESET_TIMERS, json).apply();
    }

    public static List<TimerModel> getPresetTimers(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_PRESET_TIMERS, null);
        Type type = new TypeToken<ArrayList<TimerModel>>() {}.getType();
        List<TimerModel> timers = gson.fromJson(json, type);
        if (timers == null) {
            timers = new ArrayList<>();
            // Default preset timers matching Image 1
            timers.add(new TimerModel(1, "Meeting", 1200, true)); // 00:20:00
            timers.add(new TimerModel(2, "Sleep", 600, true));    // 00:10:00
            timers.add(new TimerModel(3, "Exercise", 900, true)); // 00:15:00
            savePresetTimers(context, timers);
        }
        return timers;
    }

    public static void setTimerSoundEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_TIMER_SOUND_ENABLED, enabled).apply();
    }

    public static boolean isTimerSoundEnabled(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TIMER_SOUND_ENABLED, true);
    }

    public static void setStopwatchSoundEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_STOPWATCH_SOUND_ENABLED, enabled).apply();
    }

    public static boolean isStopwatchSoundEnabled(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_STOPWATCH_SOUND_ENABLED, true);
    }

    public static void setAutoSetTime(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_AUTO_SET_TIME, enabled).apply();
    }

    public static boolean isAutoSetTime(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_AUTO_SET_TIME, true);
    }

    public static void setUse24HourFormat(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_USE_24_HOUR, enabled).apply();
    }

    public static boolean isUse24HourFormat(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_USE_24_HOUR, false);
    }

    public static void setAutoTimeZone(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_AUTO_TIME_ZONE, enabled).apply();
    }

    public static boolean isAutoTimeZone(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_AUTO_TIME_ZONE, true);
    }

    public static void setSystemDualClock(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_SYSTEM_DUAL_CLOCK, enabled).apply();
    }

    public static boolean isSystemDualClock(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SYSTEM_DUAL_CLOCK, true);
    }

    public static void setHomeCity(Context context, String city) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_HOME_CITY, city).apply();
    }

    public static String getHomeCity(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_HOME_CITY, "Predicted Home City (Kandivali West)");
    }

    public static void setHolidayCountry(Context context, String country) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_HOLIDAY_COUNTRY, country).apply();
    }

    public static String getHolidayCountry(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_HOLIDAY_COUNTRY, "India");
    }

    public static void setTimerTone(Context context, String tone) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_TIMER_TONE, tone).apply();
    }

    public static String getTimerTone(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_TIMER_TONE, "Classic Beep");
    }

    public static void setClockDisplayMode(Context context, String mode) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_CLOCK_DISPLAY_MODE, mode).apply();
    }

    public static String getClockDisplayMode(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_CLOCK_DISPLAY_MODE, "analog");
    }

    public static String getGeminiApiKey(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString("gemini_api_key", "YOUR_GEMINI_API_KEY_HERE");
    }
}
