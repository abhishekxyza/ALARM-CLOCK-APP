package com.example.aeroalarm;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

public class VolumeRampHelper {
    private static final float START_VOLUME = 0.1f;
    private static final float MAX_VOLUME = 1.0f;
    private static final float VOLUME_INCREMENT = 0.1f;
    private static final long RAMP_INTERVAL_MS = 20000; // 20 seconds

    private MediaPlayer mediaPlayer;
    private float currentVolume;
    private Handler handler;
    private Runnable rampRunnable;

    public VolumeRampHelper(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        this.currentVolume = START_VOLUME;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startRamping() {
        if (mediaPlayer == null) return;

        // Start at initial volume
        mediaPlayer.setVolume(currentVolume, currentVolume);
        
        rampRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && currentVolume < MAX_VOLUME) {
                    currentVolume += VOLUME_INCREMENT;
                    if (currentVolume > MAX_VOLUME) currentVolume = MAX_VOLUME;
                    
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                    
                    if (currentVolume < MAX_VOLUME) {
                        handler.postDelayed(this, RAMP_INTERVAL_MS);
                    }
                }
            }
        };
        
        handler.postDelayed(rampRunnable, RAMP_INTERVAL_MS);
    }

    public void stop() {
        if (handler != null && rampRunnable != null) {
            handler.removeCallbacks(rampRunnable);
        }
        rampRunnable = null;
    }
}
