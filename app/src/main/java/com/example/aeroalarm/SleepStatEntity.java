package com.example.aeroalarm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_stats")
public class SleepStatEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private long alarmSetTime;
    private long alarmRingTime;
    private long dismissTime;
    private String characterName;

    public SleepStatEntity(long alarmSetTime, long alarmRingTime, long dismissTime, String characterName) {
        this.alarmSetTime = alarmSetTime;
        this.alarmRingTime = alarmRingTime;
        this.dismissTime = dismissTime;
        this.characterName = characterName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getAlarmSetTime() { return alarmSetTime; }
    public void setAlarmSetTime(long alarmSetTime) { this.alarmSetTime = alarmSetTime; }

    public long getAlarmRingTime() { return alarmRingTime; }
    public void setAlarmRingTime(long alarmRingTime) { this.alarmRingTime = alarmRingTime; }

    public long getDismissTime() { return dismissTime; }
    public void setDismissTime(long dismissTime) { this.dismissTime = dismissTime; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
}
