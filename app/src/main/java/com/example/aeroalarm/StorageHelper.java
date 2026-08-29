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
}
