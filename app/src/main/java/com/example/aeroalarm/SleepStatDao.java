package com.example.aeroalarm;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface SleepStatDao {
    @Insert
    void insert(SleepStatEntity stat);

    @Query("SELECT AVG(dismissTime - alarmRingTime) FROM sleep_stats WHERE alarmRingTime > :since")
    double getAverageWakeUpDelay(long since);
}
