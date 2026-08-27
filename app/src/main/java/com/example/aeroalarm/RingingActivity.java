package com.example.aeroalarm;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

public class RingingActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private long alarmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
            String time = String.format("%02d:%02d:%02d %s", currentAlarm.getHour(), currentAlarm.getMinute(), currentAlarm.getSecond(), currentAlarm.getAmpm());
            tvTime.setText(time);
            tvLabel.setText(currentAlarm.getLabel() == null || currentAlarm.getLabel().isEmpty() ? "Wake Up!" : currentAlarm.getLabel());
            
            playAlarmSound(currentAlarm);
        } else {
            playDefaultSound();
        }

        btnDismiss.setOnClickListener(v -> dismissAlarm());
        btnSnooze.setOnClickListener(v -> snoozeAlarm());
    }

    private void playAlarmSound(AlarmModel alarm) {
        Uri soundUri = null;
        
        if (alarm.getCustomMusicUri() != null) {
            soundUri = Uri.parse(alarm.getCustomMusicUri());
            startMediaPlayer(soundUri);
        } else {
            // Default tones mapping to system ringtones
            int ringtoneType;
            switch (alarm.getTone()) {
                case "Digital Retro":
                    ringtoneType = RingtoneManager.TYPE_RINGTONE;
                    break;
                case "Calm Chimes":
                    ringtoneType = RingtoneManager.TYPE_NOTIFICATION;
                    break;
                case "Sci-Fi Pulse":
                    ringtoneType = RingtoneManager.TYPE_RINGTONE; // Different system sound
                    break;
                case "Classic Beep":
                default:
                    ringtoneType = RingtoneManager.TYPE_ALARM;
                    break;
            }
            
            soundUri = RingtoneManager.getDefaultUri(ringtoneType);
            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
            startMediaPlayer(soundUri);
        }
    }

    private void playDefaultSound() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        startMediaPlayer(alarmUri);
    }

    private void startMediaPlayer(Uri uri) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to default if custom fails
            if (uri != RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) {
                playDefaultSound();
            }
        }
    }

    private void dismissAlarm() {
        stopMediaPlayer();
        finish();
    }

    private void snoozeAlarm() {
        stopMediaPlayer();
        List<AlarmModel> alarms = StorageHelper.getAlarms(this);
        AlarmModel snooze = new AlarmModel();
        snooze.setId(System.currentTimeMillis());
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MINUTE, 5);
        
        int h = cal.get(java.util.Calendar.HOUR);
        snooze.setHour(h == 0 ? 12 : h);
        snooze.setMinute(cal.get(java.util.Calendar.MINUTE));
        snooze.setSecond(cal.get(java.util.Calendar.SECOND));
        snooze.setAmpm(cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM ? "AM" : "PM");
        snooze.setLabel("Snooze");
        snooze.setActive(true);
        snooze.setDays(new java.util.ArrayList<>());

        alarms.add(snooze);
        StorageHelper.saveAlarms(this, alarms);
        AlarmManagerHelper.scheduleAlarm(this, snooze);
        
        finish();
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMediaPlayer();
    }
}
