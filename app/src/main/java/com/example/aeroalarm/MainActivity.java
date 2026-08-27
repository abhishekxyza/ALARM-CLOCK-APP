package com.example.aeroalarm;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTime, tvAmPm, tvDate;
    private RecyclerView recyclerViewAlarms;
    private View emptyState;
    private AlarmAdapter alarmAdapter;
    private List<AlarmModel> alarmList;
    private Handler clockHandler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tvTime);
        tvAmPm = findViewById(R.id.tvAmPm);
        tvDate = findViewById(R.id.tvDate);
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);
        emptyState = findViewById(R.id.emptyState);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddAlarm);

        alarmList = StorageHelper.getAlarms(this);
        setupRecyclerView();
        updateEmptyState();

        fabAdd.setOnClickListener(v -> showAlarmDialog(null));

        startClock();
    }

    private void setupRecyclerView() {
        alarmAdapter = new AlarmAdapter(alarmList, new AlarmAdapter.OnAlarmClickListener() {
            @Override
            public void onAlarmClick(AlarmModel alarm) {
                showAlarmDialog(alarm);
            }

            @Override
            public void onAlarmToggle(AlarmModel alarm, boolean isChecked) {
                alarm.setActive(isChecked);
                StorageHelper.saveAlarms(MainActivity.this, alarmList);
                if (isChecked) {
                    AlarmManagerHelper.scheduleAlarm(MainActivity.this, alarm);
                } else {
                    AlarmManagerHelper.cancelAlarm(MainActivity.this, alarm);
                }
            }
        });
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAlarms.setAdapter(alarmAdapter);
    }

    private void updateEmptyState() {
        if (alarmList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerViewAlarms.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerViewAlarms.setVisibility(View.VISIBLE);
        }
    }

    private void startClock() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    private void updateTime() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.US);
        SimpleDateFormat ampmFormat = new SimpleDateFormat("a", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);

        tvTime.setText(timeFormat.format(now));
        tvAmPm.setText(ampmFormat.format(now));
        tvDate.setText(dateFormat.format(now));
    }

    private void showAlarmDialog(AlarmModel existingAlarm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_alarm, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        EditText etLabel = view.findViewById(R.id.etLabel);
        Spinner spinnerTone = view.findViewById(R.id.spinnerTone);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        CheckBox[] dayChecks = {
                view.findViewById(R.id.cbSun),
                view.findViewById(R.id.cbMon),
                view.findViewById(R.id.cbTue),
                view.findViewById(R.id.cbWed),
                view.findViewById(R.id.cbThu),
                view.findViewById(R.id.cbFri),
                view.findViewById(R.id.cbSat)
        };

        String[] tones = {"Classic Beep", "Digital Retro", "Calm Chimes", "Sci-Fi Pulse"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTone.setAdapter(adapter);

        if (existingAlarm != null) {
            tvTitle.setText("Edit Alarm");
            btnDelete.setVisibility(View.VISIBLE);
            
            // Handle 24h format for picker mapping
            int h = existingAlarm.getHour();
            if (existingAlarm.getAmpm().equals("PM") && h < 12) h += 12;
            if (existingAlarm.getAmpm().equals("AM") && h == 12) h = 0;
            
            timePicker.setHour(h);
            timePicker.setMinute(existingAlarm.getMinute());
            etLabel.setText(existingAlarm.getLabel());
            
            if (existingAlarm.getDays() != null) {
                for (int day : existingAlarm.getDays()) {
                    if (day >= 0 && day < 7) dayChecks[day].setChecked(true);
                }
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            AlarmManagerHelper.cancelAlarm(this, existingAlarm);
            alarmList.remove(existingAlarm);
            StorageHelper.saveAlarms(this, alarmList);
            alarmAdapter.notifyDataSetChanged();
            updateEmptyState();
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            int h24 = timePicker.getHour();
            int m = timePicker.getMinute();
            int h12 = h24 % 12;
            h12 = h12 == 0 ? 12 : h12;
            String ampm = h24 >= 12 ? "PM" : "AM";
            String label = etLabel.getText().toString().trim();
            String tone = spinnerTone.getSelectedItem().toString();

            List<Integer> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (dayChecks[i].isChecked()) days.add(i);
            }

            AlarmModel alarm = existingAlarm != null ? existingAlarm : new AlarmModel();
            if (existingAlarm == null) {
                alarm.setId(System.currentTimeMillis());
                alarmList.add(alarm);
            }

            alarm.setHour(h12);
            alarm.setMinute(m);
            alarm.setAmpm(ampm);
            alarm.setLabel(label);
            alarm.setTone(tone);
            alarm.setDays(days);
            alarm.setActive(true);

            StorageHelper.saveAlarms(this, alarmList);
            alarmAdapter.notifyDataSetChanged();
            updateEmptyState();
            
            AlarmManagerHelper.scheduleAlarm(this, alarm);

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockRunnable);
    }
}
