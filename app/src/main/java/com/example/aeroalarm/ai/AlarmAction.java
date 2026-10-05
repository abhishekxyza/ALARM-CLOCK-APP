package com.example.aeroalarm.ai;

import java.util.List;

public class AlarmAction {
    public enum ActionType {
        SET_ALARM,
        SNOOZE,
        DELETE_ALARM,
        LIST_ALARMS,
        SWITCH_TAB,
        SET_WALLPAPER,
        GET_SLEEP_STATS,
        UNKNOWN
    }

    private ActionType actionType;
    private int hour;
    private int minute;
    private int second;
    private String ampm; // "AM" or "PM"
    private String label;
    private List<Integer> repeatDays; // 0=Sun, 1=Mon... 6=Sat
    private String targetTab; // "alarm", "world_clock", "timer", "stopwatch"
    private String wallpaperTheme; // "black", "navy", "purple", "emerald"

    public AlarmAction(ActionType actionType, int hour, int minute, int second, String ampm, String label, List<Integer> repeatDays, String targetTab, String wallpaperTheme) {
        this.actionType = actionType;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.ampm = ampm;
        this.label = label;
        this.repeatDays = repeatDays;
        this.targetTab = targetTab;
        this.wallpaperTheme = wallpaperTheme;
    }

    public ActionType getActionType() { return actionType; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public int getSecond() { return second; }
    public String getAmpm() { return ampm; }
    public String getLabel() { return label; }
    public List<Integer> getRepeatDays() { return repeatDays; }
    public String getTargetTab() { return targetTab; }
    public String getWallpaperTheme() { return wallpaperTheme; }
}
