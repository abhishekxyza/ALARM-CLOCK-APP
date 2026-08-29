package com.example.aeroalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;

public class AlarmManagerHelper {
    private static final String TAG = "AlarmManagerHelper";

    public static void scheduleAlarm(Context context, AlarmModel alarm) {
        if (!alarm.isActive()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarm.getId());
        
        int requestCode = (int) (alarm.getId() % Integer.MAX_VALUE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        int hour24 = alarm.getAmpm().equals("PM") && alarm.getHour() != 12 ? alarm.getHour() + 12 : 
                     (alarm.getAmpm().equals("AM") && alarm.getHour() == 12 ? 0 : alarm.getHour());
        
        calendar.set(Calendar.HOUR_OF_DAY, hour24);
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, alarm.getSecond());
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                calendar.getTimeInMillis(),
                pendingIntent
        );
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        Log.d(TAG, "Alarm scheduled for: " + calendar.getTime().toString() + " (ID: " + alarm.getId() + ")");
    }

    public static void cancelAlarm(Context context, AlarmModel alarm) {
        if (alarm == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        
        int requestCode = (int) (alarm.getId() % Integer.MAX_VALUE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}
