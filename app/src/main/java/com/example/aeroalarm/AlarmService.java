package com.example.aeroalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private VolumeRampHelper volumeRampHelper;
    private static final String CHANNEL_ID = "ALARM_SERVICE_CHANNEL";
    private static final String TAG = "AlarmService";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        long alarmId = intent.getLongExtra("ALARM_ID", -1);
        Log.d(TAG, "Alarm triggered with ID: " + alarmId);

        List<AlarmModel> alarms = StorageHelper.getAlarms(this);
        AlarmModel currentAlarm = null;
        for (AlarmModel a : alarms) {
            if (a.getId() == alarmId) {
                currentAlarm = a;
                break;
            }
        }

        Intent fullScreenIntent = new Intent(this, RingingActivity.class);
        fullScreenIntent.putExtra("ALARM_ID", alarmId);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Alarm")
                .setContentText("Wake up!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, notification);
        }

        // Explicitly start activity to ensure it shows when unlocked
        try {
            startActivity(fullScreenIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start RingingActivity directly", e);
        }

        if (currentAlarm != null) {
            playAlarmSound(currentAlarm);
        } else {
            playDefaultSound();
        }

        return START_NOT_STICKY;
    }

    private void playAlarmSound(AlarmModel alarm) {
        Uri soundUri = null;
        if (alarm.getCustomMusicUri() != null) {
            soundUri = Uri.parse(alarm.getCustomMusicUri());
            Log.d(TAG, "Playing custom music: " + soundUri);
        } else {
            // Default tones mapping
            int ringtoneType = RingtoneManager.TYPE_ALARM;
            String toneName = alarm.getTone();
            if (toneName != null) {
                switch (toneName) {
                    case "Digital Retro":
                        ringtoneType = RingtoneManager.TYPE_RINGTONE;
                        break;
                    case "Calm Chimes":
                        ringtoneType = RingtoneManager.TYPE_NOTIFICATION;
                        break;
                    case "Sci-Fi Pulse":
                        ringtoneType = RingtoneManager.TYPE_RINGTONE;
                        break;
                    case "Classic Beep":
                    default:
                        ringtoneType = RingtoneManager.TYPE_ALARM;
                        break;
                }
            }
            soundUri = RingtoneManager.getDefaultUri(ringtoneType);
            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
            Log.d(TAG, "Playing tone: " + toneName + " (Type: " + ringtoneType + ")");
        }
        startMediaPlayer(soundUri);
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
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Start gradual volume ramping
            if (volumeRampHelper != null) {
                volumeRampHelper.stop();
            }
            volumeRampHelper = new VolumeRampHelper(mediaPlayer);
            volumeRampHelper.startRamping();
        } catch (IOException e) {
            Log.e(TAG, "Error playing sound", e);
            // Fallback to notification sound if alarm sound fails
            if (uri != null && !uri.equals(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))) {
                playDefaultSound();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setSound(null, null); // Sound handled by MediaPlayer
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Alarm Service destroyed");
        if (volumeRampHelper != null) {
            volumeRampHelper.stop();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
