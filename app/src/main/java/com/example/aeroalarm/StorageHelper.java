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
}
