package com.example.aeroalarm;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class RingingActivity extends AppCompatActivity {
    private Ringtone ringtone;
    private long alarmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show on lock screen and turn on screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_ringing);

        alarmId = getIntent().getLongExtra("ALARM_ID", -1);
        
        TextView tvTime = findViewById(R.id.tvRingingTime);
        TextView tvLabel = findViewById(R.id.tvRingingLabel);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnSnooze = findViewById(R.id.btnSnooze);

        List<AlarmModel> alarms = StorageHelper.getAlarms(this);
        AlarmModel currentAlarm = null;
        for (AlarmModel a : alarms) {
            if (a.getId() == alarmId) {
                currentAlarm = a;
                break;
            }
        }

        if (currentAlarm != null) {
            String time = String.format("%02d:%02d %s", currentAlarm.getHour(), currentAlarm.getMinute(), currentAlarm.getAmpm());
            tvTime.setText(time);
            tvLabel.setText(currentAlarm.getLabel() == null || currentAlarm.getLabel().isEmpty() ? "Wake Up!" : currentAlarm.getLabel());
        }

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        if (ringtone != null) {
            ringtone.play();
        }

        btnDismiss.setOnClickListener(v -> dismissAlarm());
        btnSnooze.setOnClickListener(v -> snoozeAlarm());
    }

    private void dismissAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        finish();
    }

    private void snoozeAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        // Create a temporary snooze alarm (5 mins later)
        List<AlarmModel> alarms = StorageHelper.getAlarms(this);
        AlarmModel snooze = new AlarmModel();
        snooze.setId(System.currentTimeMillis());
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MINUTE, 5);
        
        int h = cal.get(java.util.Calendar.HOUR);
        snooze.setHour(h == 0 ? 12 : h);
        snooze.setMinute(cal.get(java.util.Calendar.MINUTE));
        snooze.setAmpm(cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM ? "AM" : "PM");
        snooze.setLabel("Snooze");
        snooze.setActive(true);
        snooze.setDays(new java.util.ArrayList<>()); // Once

        alarms.add(snooze);
        StorageHelper.saveAlarms(this, alarms);
        AlarmManagerHelper.scheduleAlarm(this, snooze);
        
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
