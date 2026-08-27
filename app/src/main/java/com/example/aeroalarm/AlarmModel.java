package com.example.aeroalarm;

import java.util.List;

public class AlarmModel {
    private long id;
    private int hour;
    private int minute;
    private int second;
    private String ampm;
    private String label;
    private String tone;
    private String customMusicUri; // URI for custom music file
    private List<Integer> days; // 0=Sun, 1=Mon, ..., 6=Sat
    private boolean active;

    public AlarmModel() {
    }

    public AlarmModel(long id, int hour, int minute, int second, String ampm, String label, String tone, List<Integer> days, boolean active) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.ampm = ampm;
        this.label = label;
        this.tone = tone;
        this.days = days;
        this.active = active;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public int getSecond() { return second; }
    public void setSecond(int second) { this.second = second; }

    public String getAmpm() { return ampm; }
    public void setAmpm(String ampm) { this.ampm = ampm; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public String getCustomMusicUri() { return customMusicUri; }
    public void setCustomMusicUri(String customMusicUri) { this.customMusicUri = customMusicUri; }

    public List<Integer> getDays() { return days; }
    public void setDays(List<Integer> days) { this.days = days; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
