package com.example.aeroalarm;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SleepStatRepository {
    private SleepStatDao sleepStatDao;
    private ExecutorService executorService;

    public SleepStatRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        sleepStatDao = db.sleepStatDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(SleepStatEntity stat) {
        executorService.execute(() -> sleepStatDao.insert(stat));
    }
}
