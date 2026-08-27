package com.example.aeroalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class AlarmManagerHelper {

    public static void scheduleAlarm(Context context, AlarmModel alarm) {
        if (!alarm.isActive()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarm.getId());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                (int) alarm.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getAmpm().equals("PM") && alarm.getHour() != 12 ? alarm.getHour() + 12 : 
                                            (alarm.getAmpm().equals("AM") && alarm.getHour() == 12 ? 0 : alarm.getHour()));
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, alarm.getSecond());
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.SECOND, 1); // If exact second passed, try next second or day
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                // Reset seconds if we move to next day to avoid weird offsets unless intended
                // Actually, keep the seconds as set by user.
            }
        }

        // Just scheduling as exact for now. Repeat logic can be handled in Receiver by rescheduling
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelAlarm(Context context, AlarmModel alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                (int) alarm.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}
