package com.example.aeroalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long alarmId = intent.getLongExtra("ALARM_ID", -1);
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
            Intent ringingIntent = new Intent(context, RingingActivity.class);
            ringingIntent.putExtra("ALARM_ID", alarmId);
            ringingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(ringingIntent);
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
