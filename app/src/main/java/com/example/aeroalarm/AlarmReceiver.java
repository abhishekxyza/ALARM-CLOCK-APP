package com.example.aeroalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long alarmId = intent.getLongExtra("ALARM_ID", -1);
        Log.d(TAG, "Broadcast received for alarm ID: " + alarmId);
        if (alarmId == -1) return;

        List<AlarmModel> alarms = StorageHelper.getAlarms(context);
        AlarmModel triggeredAlarm = null;
        for (AlarmModel a : alarms) {
            if (a.getId() == alarmId) {
                triggeredAlarm = a;
                break;
            }
        }

        if (triggeredAlarm == null || !triggeredAlarm.isActive()) return;

        // Check if today is a valid repeat day
        boolean shouldRing = true;
        if (triggeredAlarm.getDays() != null && !triggeredAlarm.getDays().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            int today = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
            if (!triggeredAlarm.getDays().contains(today)) {
                shouldRing = false;
            }
        }

        if (shouldRing) {
            Intent serviceIntent = new Intent(context, AlarmService.class);
            serviceIntent.putExtra("ALARM_ID", alarmId);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }

        // Reschedule or deactivate
        if (triggeredAlarm.getDays() == null || triggeredAlarm.getDays().isEmpty()) {
            triggeredAlarm.setActive(false);
        } else {
            AlarmManagerHelper.scheduleAlarm(context, triggeredAlarm);
        }
        StorageHelper.saveAlarms(context, alarms);
    }
}
