package com.example.aeroalarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private String selectedMusicUriString = null;
    private TextView tvDialogSelectedMusic;

    private final ActivityResultLauncher<Intent> musicPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        selectedMusicUriString = uri.toString();
                        if (tvDialogSelectedMusic != null) {
                            tvDialogSelectedMusic.setText("Selected: " + uri.getLastPathSegment());
                            tvDialogSelectedMusic.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
    );

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
        NumberPicker npHour = view.findViewById(R.id.npHour);
        NumberPicker npMinute = view.findViewById(R.id.npMinute);
        NumberPicker npSecond = view.findViewById(R.id.npSecond);
        NumberPicker npAmPm = view.findViewById(R.id.npAmPm);
        
        EditText etLabel = view.findViewById(R.id.etLabel);
        Spinner spinnerTone = view.findViewById(R.id.spinnerTone);
        Button btnSelectMusic = view.findViewById(R.id.btnSelectMusic);
        tvDialogSelectedMusic = view.findViewById(R.id.tvSelectedMusic);
        
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        npHour.setMinValue(1);
        npHour.setMaxValue(12);
        npMinute.setMinValue(0);
        npMinute.setMaxValue(59);
        npSecond.setMinValue(0);
        npSecond.setMaxValue(59);
        npAmPm.setMinValue(0);
        npAmPm.setMaxValue(1);
        npAmPm.setDisplayedValues(new String[]{"AM", "PM"});

        // Padding for single digits
        NumberPicker.Formatter formatter = i -> String.format(Locale.US, "%02d", i);
        npHour.setFormatter(formatter);
        npMinute.setFormatter(formatter);
        npSecond.setFormatter(formatter);

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

        selectedMusicUriString = null;

        if (existingAlarm != null) {
            tvTitle.setText("Edit Alarm");
            btnDelete.setVisibility(View.VISIBLE);
            
            npHour.setValue(existingAlarm.getHour());
            npMinute.setValue(existingAlarm.getMinute());
            npSecond.setValue(existingAlarm.getSecond());
            npAmPm.setValue(existingAlarm.getAmpm().equals("AM") ? 0 : 1);
            
            etLabel.setText(existingAlarm.getLabel());

            // Select the tone in spinner
            if (existingAlarm.getTone() != null) {
                for (int i = 0; i < tones.length; i++) {
                    if (tones[i].equals(existingAlarm.getTone())) {
                        spinnerTone.setSelection(i);
                        break;
                    }
                }
            }
            
            if (existingAlarm.getCustomMusicUri() != null) {
                selectedMusicUriString = existingAlarm.getCustomMusicUri();
                Uri uri = Uri.parse(selectedMusicUriString);
                tvDialogSelectedMusic.setText("Selected: " + uri.getLastPathSegment());
                tvDialogSelectedMusic.setVisibility(View.VISIBLE);
            }

            if (existingAlarm.getDays() != null) {
                for (int day : existingAlarm.getDays()) {
                    if (day >= 0 && day < 7) dayChecks[day].setChecked(true);
                }
            }
        }

        btnSelectMusic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            musicPickerLauncher.launch(intent);
        });

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
            int h = npHour.getValue();
            int m = npMinute.getValue();
            int s = npSecond.getValue();
            String ampm = npAmPm.getValue() == 0 ? "AM" : "PM";
            
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

            alarm.setHour(h);
            alarm.setMinute(m);
            alarm.setSecond(s);
            alarm.setAmpm(ampm);
            alarm.setLabel(label);
            alarm.setTone(tone);
            alarm.setCustomMusicUri(selectedMusicUriString);
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
