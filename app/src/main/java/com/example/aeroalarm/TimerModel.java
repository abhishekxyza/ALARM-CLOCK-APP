package com.example.aeroalarm;

public class TimerModel {
    private long id;
    private String name;
    private long durationSeconds;
    private boolean showOnLockScreen;

    public TimerModel() {
    }

    public TimerModel(long id, String name, long durationSeconds, boolean showOnLockScreen) {
        this.id = id;
        this.name = name;
        this.durationSeconds = durationSeconds;
        this.showOnLockScreen = showOnLockScreen;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isShowOnLockScreen() {
        return showOnLockScreen;
    }

    public void setShowOnLockScreen(boolean showOnLockScreen) {
        this.showOnLockScreen = showOnLockScreen;
    }

    public String getFormattedDuration() {
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        return String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
