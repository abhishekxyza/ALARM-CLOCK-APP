package com.example.aeroalarm;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // Header & Navigation
    private TextView tvHeaderTitle;
    private ImageButton btnAddTop, btnMoreTop;
    private ImageView ivMainBackground;
    private View vMainBackgroundDim;
    private LinearLayout tabAlarm, tabWorldClock, tabTimer, tabStopwatch;
    private ImageView ivTabAlarm, ivTabWorldClock, ivTabTimer, ivTabStopwatch;
    private TextView tvTabAlarm, tvTabWorldClock, tvTabTimer, tvTabStopwatch;
    private View viewAlarm, viewWorldClock, viewTimer, viewStopwatch;
    private int currentTab = 0; // 0: Alarm, 1: World Clock, 2: Timer, 3: Stopwatch

    // Alarm Tab
    private RecyclerView recyclerViewAlarms;
    private View emptyState;
    private AlarmAdapter alarmAdapter;
    private List<AlarmModel> alarmList;
    private String selectedMusicUriString = null;
    private TextView tvDialogSelectedMusic;

    // World Clock Tab
    private TextView tvLocalTime, tvLocalAmPm, tvLocalDate, tvLocalCityName;
    private RecyclerView rvWorldClock;
    private WorldClockAdapter worldClockAdapter;
    private List<WorldCityModel> worldCityList;

    // Timer Tab
    private NumberPicker npTimerHour, npTimerMinute, npTimerSecond;
    private TextView tvTimerCountdown;
    private LinearLayout timerInputLayout;
    private ImageButton btnTimerCancel, btnTimerPlayPause, btnTimerSound;
    private RecyclerView rvPresetTimers;
    private PresetTimerAdapter presetTimerAdapter;
    private List<TimerModel> presetTimerList;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long timerTotalTimeMs = 0;
    private long timerRemainingTimeMs = 0;
    private boolean isTimerRunning = false;

    // Stopwatch Tab
    private TextView tvStopwatchDisplay;
    private Button btnStopwatchStartPause, btnStopwatchLapReset;
    private RecyclerView rvStopwatchLaps;
    private StopwatchLapAdapter stopwatchLapAdapter;
    private List<StopwatchLapAdapter.LapItem> lapList = new ArrayList<>();
    private Handler stopwatchHandler = new Handler(Looper.getMainLooper());
    private Runnable stopwatchRunnable;
    private long stopwatchStartTimeMs = 0;
    private long stopwatchElapsedTimeMs = 0;
    private long lastLapTimeMs = 0;
    private boolean isStopwatchRunning = false;

    // Clock Handler
    private Handler clockHandler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    // Camera Executor
    private ExecutorService cameraExecutor;

    // Activity Result Launchers
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

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    showBarcodeSettingsDialog();
                } else {
                    Toast.makeText(this, "Camera permission is required for barcode task", Toast.LENGTH_LONG).show();
                }
            }
    );

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications are required for alarms", Toast.LENGTH_LONG).show();
                }
            }
    );

    private String selectedWallpaperTypeTemp = "default_black";
    private String selectedWallpaperUriTemp = null;
    private TextView tvDialogWallpaperStatusTemp = null;

    private final ActivityResultLauncher<Intent> wallpaperPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            Log.e("MainActivity", "Persistable permission error", e);
                        }
                        selectedWallpaperTypeTemp = "custom_uri";
                        selectedWallpaperUriTemp = uri.toString();
                        if (tvDialogWallpaperStatusTemp != null) {
                            tvDialogWallpaperStatusTemp.setText("Selected Photo: " + uri.getLastPathSegment());
                            tvDialogWallpaperStatusTemp.setTextColor(Color.parseColor("#FF9500"));
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        requestNotificationPermission();

        setupAlarmTab();
        setupWorldClockTab();
        setupTimerTab();
        setupStopwatchTab();
        setupNavigationAndHeader();
        applyMainWallpaper();

        cameraExecutor = Executors.newSingleThreadExecutor();
        startClock();
    }

    private void initViews() {
        // Header & Nav
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnAddTop = findViewById(R.id.btnAddTop);
        btnMoreTop = findViewById(R.id.btnMoreTop);
        ivMainBackground = findViewById(R.id.ivMainBackground);
        vMainBackgroundDim = findViewById(R.id.vMainBackgroundDim);

        tabAlarm = findViewById(R.id.tabAlarm);
        tabWorldClock = findViewById(R.id.tabWorldClock);
        tabTimer = findViewById(R.id.tabTimer);
        tabStopwatch = findViewById(R.id.tabStopwatch);

        ivTabAlarm = findViewById(R.id.ivTabAlarm);
        ivTabWorldClock = findViewById(R.id.ivTabWorldClock);
        ivTabTimer = findViewById(R.id.ivTabTimer);
        ivTabStopwatch = findViewById(R.id.ivTabStopwatch);

        tvTabAlarm = findViewById(R.id.tvTabAlarm);
        tvTabWorldClock = findViewById(R.id.tvTabWorldClock);
        tvTabTimer = findViewById(R.id.tvTabTimer);
        tvTabStopwatch = findViewById(R.id.tvTabStopwatch);

        viewAlarm = findViewById(R.id.viewAlarm);
        viewWorldClock = findViewById(R.id.viewWorldClock);
        viewTimer = findViewById(R.id.viewTimer);
        viewStopwatch = findViewById(R.id.viewStopwatch);

        // Alarm
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);
        emptyState = findViewById(R.id.emptyState);

        // World Clock
        tvLocalCityName = findViewById(R.id.tvLocalCityName);
        tvLocalTime = findViewById(R.id.tvLocalTime);
        tvLocalAmPm = findViewById(R.id.tvLocalAmPm);
        tvLocalDate = findViewById(R.id.tvLocalDate);
        rvWorldClock = findViewById(R.id.rvWorldClock);

        // Timer
        npTimerHour = findViewById(R.id.npTimerHour);
        npTimerMinute = findViewById(R.id.npTimerMinute);
        npTimerSecond = findViewById(R.id.npTimerSecond);
        tvTimerCountdown = findViewById(R.id.tvTimerCountdown);
        timerInputLayout = findViewById(R.id.timerInputLayout);
        rvPresetTimers = findViewById(R.id.rvPresetTimers);
        btnTimerCancel = findViewById(R.id.btnTimerCancel);
        btnTimerPlayPause = findViewById(R.id.btnTimerPlayPause);
        btnTimerSound = findViewById(R.id.btnTimerSound);

        // Stopwatch
        tvStopwatchDisplay = findViewById(R.id.tvStopwatchDisplay);
        btnStopwatchStartPause = findViewById(R.id.btnStopwatchStartPause);
        btnStopwatchLapReset = findViewById(R.id.btnStopwatchLapReset);
        rvStopwatchLaps = findViewById(R.id.rvStopwatchLaps);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupNavigationAndHeader() {
        tabAlarm.setOnClickListener(v -> switchTab(0));
        tabWorldClock.setOnClickListener(v -> switchTab(1));
        tabTimer.setOnClickListener(v -> switchTab(2));
        tabStopwatch.setOnClickListener(v -> switchTab(3));

        btnAddTop.setOnClickListener(v -> {
            if (currentTab == 0) {
                showAlarmDialog(null);
            } else if (currentTab == 1) {
                showAddCityDialog();
            } else if (currentTab == 2) {
                showAddTimerDialog();
            } else if (currentTab == 3) {
                resetStopwatch();
            }
        });

        btnMoreTop.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("🎨 Anime Oshi Settings");
        popup.getMenu().add("📷 Barcode Task Settings");
        popup.getMenu().add("🖼️ Wallpaper Settings");
        popup.getMenu().add("⚙️ Settings");
        popup.getMenu().add("📊 Sleep Stats");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.contains("Anime Oshi")) {
                showOshiSettingsDialog();
                return true;
            } else if (title.contains("Barcode")) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    showBarcodeSettingsDialog();
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
                return true;
            } else if (title.contains("Wallpaper")) {
                showWallpaperSettingsDialog();
                return true;
            } else if (title.contains("Settings")) {
                showSettingsDialog();
                return true;
            } else if (title.contains("Sleep Stats")) {
                showSleepStatsDialog();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void switchTab(int tabIndex) {
        currentTab = tabIndex;

        // Reset tab styling
        resetTabStyles();

        // Hide all views
        viewAlarm.setVisibility(View.GONE);
        viewWorldClock.setVisibility(View.GONE);
        viewTimer.setVisibility(View.GONE);
        viewStopwatch.setVisibility(View.GONE);

        int activeColor = Color.parseColor("#FF9500");
        int inactiveColor = Color.parseColor("#9E9E9E");

        switch (tabIndex) {
            case 0: // Alarm
                tvHeaderTitle.setText("Alarm");
                viewAlarm.setVisibility(View.VISIBLE);
                tabAlarm.setBackgroundResource(R.drawable.bg_tab_active_pill);
                ivTabAlarm.setColorFilter(activeColor);
                tvTabAlarm.setTextColor(activeColor);
                break;
            case 1: // World Clock
                tvHeaderTitle.setText("World Clock");
                viewWorldClock.setVisibility(View.VISIBLE);
                tabWorldClock.setBackgroundResource(R.drawable.bg_tab_active_pill);
                ivTabWorldClock.setColorFilter(activeColor);
                tvTabWorldClock.setTextColor(activeColor);
                updateWorldClockDisplay();
                break;
            case 2: // Timer
                tvHeaderTitle.setText("Timer");
                viewTimer.setVisibility(View.VISIBLE);
                tabTimer.setBackgroundResource(R.drawable.bg_tab_active_pill);
                ivTabTimer.setColorFilter(activeColor);
                tvTabTimer.setTextColor(activeColor);
                break;
            case 3: // Stopwatch
                tvHeaderTitle.setText("Stopwatch");
                viewStopwatch.setVisibility(View.VISIBLE);
                tabStopwatch.setBackgroundResource(R.drawable.bg_tab_active_pill);
                ivTabStopwatch.setColorFilter(activeColor);
                tvTabStopwatch.setTextColor(activeColor);
                break;
        }
    }

    private void resetTabStyles() {
        int inactiveColor = Color.parseColor("#9E9E9E");

        tabAlarm.setBackgroundResource(0);
        tabWorldClock.setBackgroundResource(0);
        tabTimer.setBackgroundResource(0);
        tabStopwatch.setBackgroundResource(0);

        ivTabAlarm.setColorFilter(inactiveColor);
        ivTabWorldClock.setColorFilter(inactiveColor);
        ivTabTimer.setColorFilter(inactiveColor);
        ivTabStopwatch.setColorFilter(inactiveColor);

        tvTabAlarm.setTextColor(inactiveColor);
        tvTabWorldClock.setTextColor(inactiveColor);
        tvTabTimer.setTextColor(inactiveColor);
        tvTabStopwatch.setTextColor(inactiveColor);
    }

    // ================= ALARM TAB =================
    private void setupAlarmTab() {
        alarmList = StorageHelper.getAlarms(this);
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
        updateEmptyState();
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

    // ================= WORLD CLOCK TAB =================
    private void setupWorldClockTab() {
        worldCityList = StorageHelper.getWorldCities(this);
        worldClockAdapter = new WorldClockAdapter(worldCityList, new WorldClockAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(WorldCityModel city) {
            }

            @Override
            public void onCityDelete(WorldCityModel city) {
                worldCityList.remove(city);
                StorageHelper.saveWorldCities(MainActivity.this, worldCityList);
                worldClockAdapter.updateData(worldCityList);
            }
        });
        rvWorldClock.setLayoutManager(new LinearLayoutManager(this));
        rvWorldClock.setAdapter(worldClockAdapter);
    }

    private void updateWorldClockDisplay() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.US);
        SimpleDateFormat ampmFormat = new SimpleDateFormat("a", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.US);

        tvLocalTime.setText(timeFormat.format(now));
        tvLocalAmPm.setText(ampmFormat.format(now));
        tvLocalDate.setText(dateFormat.format(now));

        String localCity = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        tvLocalCityName.setText("Local Time (" + localCity + ")");

        if (worldClockAdapter != null) {
            worldClockAdapter.notifyDataSetChanged();
        }
    }

    private void showAddCityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_city, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etSearchCity = view.findViewById(R.id.etSearchCity);
        RecyclerView rvCityList = view.findViewById(R.id.rvCityList);
        Button btnClose = view.findViewById(R.id.btnCloseCityDialog);

        List<WorldCityModel> availableCities = getPresetCities();
        WorldClockAdapter availableAdapter = new WorldClockAdapter(availableCities, new WorldClockAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(WorldCityModel city) {
                boolean exists = false;
                for (WorldCityModel c : worldCityList) {
                    if (c.getCityName().equalsIgnoreCase(city.getCityName())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    worldCityList.add(city);
                    StorageHelper.saveWorldCities(MainActivity.this, worldCityList);
                    worldClockAdapter.updateData(worldCityList);
                    Toast.makeText(MainActivity.this, "Added " + city.getCityName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, city.getCityName() + " is already added", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }

            @Override
            public void onCityDelete(WorldCityModel city) {}
        });

        rvCityList.setLayoutManager(new LinearLayoutManager(this));
        rvCityList.setAdapter(availableAdapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private List<WorldCityModel> getPresetCities() {
        List<WorldCityModel> list = new ArrayList<>();
        list.add(new WorldCityModel("Tokyo", "Japan", "Asia/Tokyo"));
        list.add(new WorldCityModel("London", "United Kingdom", "Europe/London"));
        list.add(new WorldCityModel("New York", "United States", "America/New_York"));
        list.add(new WorldCityModel("Paris", "France", "Europe/Paris"));
        list.add(new WorldCityModel("Sydney", "Australia", "Australia/Sydney"));
        list.add(new WorldCityModel("Dubai", "UAE", "Asia/Dubai"));
        list.add(new WorldCityModel("San Francisco", "United States", "America/Los_Angeles"));
        list.add(new WorldCityModel("Singapore", "Singapore", "Asia/Singapore"));
        list.add(new WorldCityModel("Toronto", "Canada", "America/Toronto"));
        list.add(new WorldCityModel("Seoul", "South Korea", "Asia/Seoul"));
        list.add(new WorldCityModel("Berlin", "Germany", "Europe/Berlin"));
        return list;
    }

    // ================= TIMER TAB =================
    private void setupTimerTab() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            npTimerHour.setTextColor(Color.WHITE);
            npTimerMinute.setTextColor(Color.WHITE);
            npTimerSecond.setTextColor(Color.WHITE);
        }

        npTimerHour.setMinValue(0);
        npTimerHour.setMaxValue(23);
        npTimerMinute.setMinValue(0);
        npTimerMinute.setMaxValue(59);
        npTimerSecond.setMinValue(0);
        npTimerSecond.setMaxValue(59);

        NumberPicker.Formatter formatter = i -> String.format(Locale.US, "%02d", i);
        npTimerHour.setFormatter(formatter);
        npTimerMinute.setFormatter(formatter);
        npTimerSecond.setFormatter(formatter);

        // Preset Timers RecyclerView
        presetTimerList = StorageHelper.getPresetTimers(this);
        presetTimerAdapter = new PresetTimerAdapter(presetTimerList, timer -> {
            long secs = timer.getDurationSeconds();
            int h = (int) (secs / 3600);
            int m = (int) ((secs % 3600) / 60);
            int s = (int) (secs % 60);
            npTimerHour.setValue(h);
            npTimerMinute.setValue(m);
            npTimerSecond.setValue(s);
            Toast.makeText(this, "Loaded " + timer.getName(), Toast.LENGTH_SHORT).show();
        });
        rvPresetTimers.setLayoutManager(new LinearLayoutManager(this));
        rvPresetTimers.setAdapter(presetTimerAdapter);

        // Circular Action Buttons (Image 1)
        btnTimerPlayPause.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnTimerCancel.setOnClickListener(v -> resetTimer());

        btnTimerSound.setOnClickListener(v -> {
            Toast.makeText(this, "Timer Sound: Default Tone", Toast.LENGTH_SHORT).show();
        });
    }

    private void startTimer() {
        if (timerRemainingTimeMs == 0) {
            int h = npTimerHour.getValue();
            int m = npTimerMinute.getValue();
            int s = npTimerSecond.getValue();
            timerTotalTimeMs = ((h * 3600L) + (m * 60L) + s) * 1000L;
            timerRemainingTimeMs = timerTotalTimeMs;
        }

        if (timerRemainingTimeMs <= 0) {
            Toast.makeText(this, "Set a time first", Toast.LENGTH_SHORT).show();
            return;
        }

        isTimerRunning = true;
        timerInputLayout.setVisibility(View.GONE);
        tvTimerCountdown.setVisibility(View.VISIBLE);
        btnTimerPlayPause.setImageResource(R.drawable.ic_close);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerRemainingTimeMs > 0) {
                    timerRemainingTimeMs -= 1000;
                    updateTimerCountdownDisplay();
                    timerHandler.postDelayed(this, 1000);
                } else {
                    onTimerFinished();
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void pauseTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        btnTimerPlayPause.setImageResource(R.drawable.ic_play_arrow);
    }

    private void resetTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        timerRemainingTimeMs = 0;
        timerTotalTimeMs = 0;

        tvTimerCountdown.setVisibility(View.GONE);
        timerInputLayout.setVisibility(View.VISIBLE);
        btnTimerPlayPause.setImageResource(R.drawable.ic_play_arrow);
    }

    private void showAddTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_timer, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageButton btnClose = view.findViewById(R.id.btnCloseAddTimer);
        NumberPicker npHour = view.findViewById(R.id.npAddTimerHour);
        NumberPicker npMinute = view.findViewById(R.id.npAddTimerMinute);
        NumberPicker npSecond = view.findViewById(R.id.npAddTimerSecond);

        EditText etTimerName = view.findViewById(R.id.etTimerName);
        androidx.appcompat.widget.SwitchCompat swShowOnLockScreen = view.findViewById(R.id.swShowOnLockScreen);
        Button btnSave = view.findViewById(R.id.btnSaveAddTimer);

        npHour.setMinValue(0);
        npHour.setMaxValue(23);
        npMinute.setMinValue(0);
        npMinute.setMaxValue(59);
        npSecond.setMinValue(0);
        npSecond.setMaxValue(59);

        NumberPicker.Formatter formatter = i -> String.format(Locale.US, "%02d", i);
        npHour.setFormatter(formatter);
        npMinute.setFormatter(formatter);
        npSecond.setFormatter(formatter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            npHour.setTextColor(Color.WHITE);
            npMinute.setTextColor(Color.WHITE);
            npSecond.setTextColor(Color.WHITE);
        }

        // Set default to 00h 31m 00s like Image 2
        npHour.setValue(0);
        npMinute.setValue(31);
        npSecond.setValue(0);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int h = npHour.getValue();
            int m = npMinute.getValue();
            int s = npSecond.getValue();
            long totalSecs = (h * 3600L) + (m * 60L) + s;

            if (totalSecs <= 0) {
                Toast.makeText(this, "Set a valid duration", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = etTimerName.getText().toString().trim();
            if (name.isEmpty()) name = "Timer";

            boolean lockScreen = swShowOnLockScreen.isChecked();

            TimerModel newTimer = new TimerModel(System.currentTimeMillis(), name, totalSecs, lockScreen);
            presetTimerList.add(newTimer);
            StorageHelper.savePresetTimers(this, presetTimerList);
            presetTimerAdapter.updateData(presetTimerList);

            // Populate pickers with new timer
            npTimerHour.setValue(h);
            npTimerMinute.setValue(m);
            npTimerSecond.setValue(s);

            Toast.makeText(this, "Timer '" + name + "' added", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private interface OnHolidayUpdatedListener {
        void onUpdated();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        ImageButton btnBack = view.findViewById(R.id.btnBackSettings);
        androidx.appcompat.widget.SwitchCompat swTimerSounds = view.findViewById(R.id.swTimerRunningSounds);
        androidx.appcompat.widget.SwitchCompat swStopwatchSounds = view.findViewById(R.id.swStopwatchRunningSounds);

        TextView tvHolidayCountry = view.findViewById(R.id.tvHolidayCountrySubtext);
        if (tvHolidayCountry != null) {
            tvHolidayCountry.setText(StorageHelper.getHolidayCountry(this));
        }

        swTimerSounds.setChecked(StorageHelper.isTimerSoundEnabled(this));
        swStopwatchSounds.setChecked(StorageHelper.isStopwatchSoundEnabled(this));

        swTimerSounds.setOnCheckedChangeListener((bv, isChecked) -> {
            StorageHelper.setTimerSoundEnabled(this, isChecked);
        });

        swStopwatchSounds.setOnCheckedChangeListener((bv, isChecked) -> {
            StorageHelper.setStopwatchSoundEnabled(this, isChecked);
        });

        View cardDualClock = view.findViewById(R.id.cardDualClock);
        if (cardDualClock != null) {
            cardDualClock.setOnClickListener(v -> showDateTimeSettingsDialog());
        }

        View cardHolidays = view.findViewById(R.id.cardHolidays);
        if (cardHolidays != null) {
            cardHolidays.setOnClickListener(v -> showHolidaysSettingsDialog(() -> {
                if (tvHolidayCountry != null) {
                    tvHolidayCountry.setText(StorageHelper.getHolidayCountry(this));
                }
            }));
        }

        btnBack.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDateTimeSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_date_time_settings, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        ImageButton btnBack = view.findViewById(R.id.btnBackDateTime);
        androidx.appcompat.widget.SwitchCompat swAutoSetTime = view.findViewById(R.id.swAutoSetTime);
        androidx.appcompat.widget.SwitchCompat swUse24HourFormat = view.findViewById(R.id.swUse24HourFormat);
        androidx.appcompat.widget.SwitchCompat swAutoTimeZone = view.findViewById(R.id.swAutoTimeZone);
        androidx.appcompat.widget.SwitchCompat swSystemDualClock = view.findViewById(R.id.swSystemDualClock);

        TextView tvDateValue = view.findViewById(R.id.tvDateValue);
        TextView tvTimeValue = view.findViewById(R.id.tvTimeValue);
        TextView tvTimeZoneValue = view.findViewById(R.id.tvTimeZoneValue);
        TextView tvHomeCityValue = view.findViewById(R.id.tvHomeCityValue);

        swAutoSetTime.setChecked(StorageHelper.isAutoSetTime(this));
        swUse24HourFormat.setChecked(StorageHelper.isUse24HourFormat(this));
        swAutoTimeZone.setChecked(StorageHelper.isAutoTimeZone(this));
        swSystemDualClock.setChecked(StorageHelper.isSystemDualClock(this));

        Date now = new Date();
        tvDateValue.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(now));
        tvTimeValue.setText(new SimpleDateFormat("h:mm a", Locale.US).format(now));
        tvTimeZoneValue.setText("GMT" + new SimpleDateFormat("Z", Locale.US).format(now) + " " + TimeZone.getDefault().getDisplayName());
        tvHomeCityValue.setText(StorageHelper.getHomeCity(this));

        swAutoSetTime.setOnCheckedChangeListener((bv, isChecked) -> StorageHelper.setAutoSetTime(this, isChecked));
        swUse24HourFormat.setOnCheckedChangeListener((bv, isChecked) -> StorageHelper.setUse24HourFormat(this, isChecked));
        swAutoTimeZone.setOnCheckedChangeListener((bv, isChecked) -> StorageHelper.setAutoTimeZone(this, isChecked));
        swSystemDualClock.setOnCheckedChangeListener((bv, isChecked) -> StorageHelper.setSystemDualClock(this, isChecked));

        btnBack.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showHolidaysSettingsDialog(OnHolidayUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_holidays_settings, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        ImageButton btnBack = view.findViewById(R.id.btnBackHolidays);
        RecyclerView rvCountries = view.findViewById(R.id.rvCountryHolidays);
        EditText etSearch = view.findViewById(R.id.etSearchCountry);

        List<String> countries = getCountryList();
        String current = StorageHelper.getHolidayCountry(this);

        HolidayCountryAdapter adapter = new HolidayCountryAdapter(countries, current, selectedCountry -> {
            StorageHelper.setHolidayCountry(this, selectedCountry);
            Toast.makeText(this, "Holiday Region set to " + selectedCountry, Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onUpdated();
            dialog.dismiss();
        });

        rvCountries.setLayoutManager(new LinearLayoutManager(this));
        rvCountries.setAdapter(adapter);

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        btnBack.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private List<String> getCountryList() {
        List<String> list = new ArrayList<>();
        list.add("Afghanistan");
        list.add("Albania");
        list.add("Algeria");
        list.add("American Samoa");
        list.add("Andorra");
        list.add("Angola");
        list.add("Anguilla");
        list.add("Antigua & Barbuda");
        list.add("Argentina");
        list.add("Armenia");
        list.add("Aruba");
        list.add("Australia");
        list.add("Austria");
        list.add("Azerbaijan");
        list.add("Bahamas");
        list.add("Bahrain");
        list.add("Bangladesh");
        list.add("Barbados");
        list.add("Belarus");
        list.add("Belgium");
        list.add("Belize");
        list.add("India");
        list.add("United Kingdom");
        list.add("United States");
        java.util.Collections.sort(list);
        return list;
    }

    private void updateTimerCountdownDisplay() {
        long seconds = (timerRemainingTimeMs / 1000) % 60;
        long minutes = (timerRemainingTimeMs / (1000 * 60)) % 60;
        long hours = (timerRemainingTimeMs / (1000 * 60 * 60));
        tvTimerCountdown.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void onTimerFinished() {
        resetTimer();
        Toast.makeText(this, "Timer Finished!", Toast.LENGTH_LONG).show();
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alert);
            if (r != null) r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= STOPWATCH TAB =================
    private void setupStopwatchTab() {
        stopwatchLapAdapter = new StopwatchLapAdapter(lapList);
        rvStopwatchLaps.setLayoutManager(new LinearLayoutManager(this));
        rvStopwatchLaps.setAdapter(stopwatchLapAdapter);

        btnStopwatchStartPause.setOnClickListener(v -> {
            if (isStopwatchRunning) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnStopwatchLapReset.setOnClickListener(v -> {
            if (isStopwatchRunning) {
                recordLap();
            } else {
                resetStopwatch();
            }
        });
    }

    private void startStopwatch() {
        isStopwatchRunning = true;
        stopwatchStartTimeMs = System.currentTimeMillis() - stopwatchElapsedTimeMs;
        btnStopwatchStartPause.setText("Pause");
        btnStopwatchLapReset.setText("Lap");
        btnStopwatchLapReset.setEnabled(true);

        stopwatchRunnable = new Runnable() {
            @Override
            public void run() {
                stopwatchElapsedTimeMs = System.currentTimeMillis() - stopwatchStartTimeMs;
                updateStopwatchDisplay(stopwatchElapsedTimeMs);
                stopwatchHandler.postDelayed(this, 10);
            }
        };
        stopwatchHandler.post(stopwatchRunnable);
    }

    private void pauseStopwatch() {
        isStopwatchRunning = false;
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
        btnStopwatchStartPause.setText("Resume");
        btnStopwatchLapReset.setText("Reset");
    }

    private void resetStopwatch() {
        isStopwatchRunning = false;
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
        stopwatchElapsedTimeMs = 0;
        lastLapTimeMs = 0;
        lapList.clear();
        stopwatchLapAdapter.updateData(lapList);

        updateStopwatchDisplay(0);
        btnStopwatchStartPause.setText("Start");
        btnStopwatchLapReset.setText("Lap");
        btnStopwatchLapReset.setEnabled(false);
    }

    private void recordLap() {
        long currentTotal = stopwatchElapsedTimeMs;
        long lapDuration = currentTotal - lastLapTimeMs;
        lastLapTimeMs = currentTotal;

        int lapNum = lapList.size() + 1;
        lapList.add(0, new StopwatchLapAdapter.LapItem(lapNum, lapDuration, currentTotal));
        stopwatchLapAdapter.updateData(lapList);
    }

    private void updateStopwatchDisplay(long ms) {
        long minutes = (ms / 1000) / 60;
        long seconds = (ms / 1000) % 60;
        long hundredths = (ms % 1000) / 10;
        tvStopwatchDisplay.setText(String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths));
    }

    // ================= DIALOGS =================
    private void showSleepStatsDialog() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(MainActivity.this);
            double avgDelayMs = db.sleepStatDao().getAverageWakeUpDelay(System.currentTimeMillis() - (7L * 24 * 3600 * 1000));
            double avgSecs = avgDelayMs / 1000.0;
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
                builder.setTitle("📊 Sleep Analytics")
                       .setMessage(String.format(Locale.US, "7-Day Avg Wake-Up Speed: %.1f seconds", avgSecs))
                       .setPositiveButton("OK", (d, w) -> d.dismiss())
                       .show();
            });
        });
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

        NumberPicker.Formatter formatter = i -> String.format(Locale.US, "%02d", i);
        npHour.setFormatter(formatter);
        npMinute.setFormatter(formatter);
        npSecond.setFormatter(formatter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            npHour.setTextColor(Color.WHITE);
            npMinute.setTextColor(Color.WHITE);
            npSecond.setTextColor(Color.WHITE);
            npAmPm.setTextColor(Color.WHITE);
        }

        CheckBox[] dayChecks = {
                view.findViewById(R.id.cbSun),
                view.findViewById(R.id.cbMon),
                view.findViewById(R.id.cbTue),
                view.findViewById(R.id.cbWed),
                view.findViewById(R.id.cbThu),
                view.findViewById(R.id.cbFri),
                view.findViewById(R.id.cbSat)
        };

        Button btnPresetMonFri = view.findViewById(R.id.btnPresetMonFri);
        Button btnPresetSatSun = view.findViewById(R.id.btnPresetSatSun);
        Button btnPresetSunday = view.findViewById(R.id.btnPresetSunday);
        Button btnPresetEveryDay = view.findViewById(R.id.btnPresetEveryDay);

        btnPresetMonFri.setOnClickListener(v -> {
            dayChecks[0].setChecked(false); // Sun
            dayChecks[1].setChecked(true);  // Mon
            dayChecks[2].setChecked(true);  // Tue
            dayChecks[3].setChecked(true);  // Wed
            dayChecks[4].setChecked(true);  // Thu
            dayChecks[5].setChecked(true);  // Fri
            dayChecks[6].setChecked(false); // Sat
        });

        btnPresetSatSun.setOnClickListener(v -> {
            dayChecks[0].setChecked(true);  // Sun
            dayChecks[1].setChecked(false); // Mon
            dayChecks[2].setChecked(false); // Tue
            dayChecks[3].setChecked(false); // Wed
            dayChecks[4].setChecked(false); // Thu
            dayChecks[5].setChecked(false); // Fri
            dayChecks[6].setChecked(true);  // Sat
        });

        btnPresetSunday.setOnClickListener(v -> {
            dayChecks[0].setChecked(true);  // Sun
            dayChecks[1].setChecked(false); // Mon
            dayChecks[2].setChecked(false); // Tue
            dayChecks[3].setChecked(false); // Wed
            dayChecks[4].setChecked(false); // Thu
            dayChecks[5].setChecked(false); // Fri
            dayChecks[6].setChecked(false); // Sat
        });

        btnPresetEveryDay.setOnClickListener(v -> {
            for (CheckBox cb : dayChecks) {
                cb.setChecked(true);
            }
        });

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
            selectedMusicUriString = existingAlarm.getCustomMusicUri();

            if (existingAlarm.getTone() != null) {
                for (int i = 0; i < tones.length; i++) {
                    if (tones[i].equals(existingAlarm.getTone())) {
                        spinnerTone.setSelection(i);
                        break;
                    }
                }
            }
            
            if (selectedMusicUriString != null) {
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

    private void showOshiSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_oshi_settings, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etOshiUrl = view.findViewById(R.id.etOshiUrl);
        CheckBox cbLockOshi = view.findViewById(R.id.cbLockOshi);
        Button btnSave = view.findViewById(R.id.btnSaveOshi);
        Button btnCancel = view.findViewById(R.id.btnCancelOshi);

        etOshiUrl.setText(StorageHelper.getOshiUrl(this));
        cbLockOshi.setChecked(StorageHelper.isOshiLocked(this));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String url = etOshiUrl.getText().toString().trim();
            boolean locked = cbLockOshi.isChecked();
            StorageHelper.setOshiCharacter(this, url, locked);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyMainWallpaper() {
        String type = StorageHelper.getWallpaperType(this);
        String uriStr = StorageHelper.getWallpaperUri(this);

        View root = findViewById(R.id.headerLayout);
        if (root != null && root.getParent() instanceof View) {
            root = (View) root.getParent();
        }

        if ("custom_uri".equals(type) && uriStr != null && !uriStr.isEmpty()) {
            if (ivMainBackground != null && vMainBackgroundDim != null) {
                ivMainBackground.setVisibility(View.VISIBLE);
                vMainBackgroundDim.setVisibility(View.VISIBLE);
                try {
                    Glide.with(this).load(Uri.parse(uriStr)).into(ivMainBackground);
                } catch (Exception e) {
                    Log.e("MainActivity", "Error loading background image", e);
                }
            }
        } else {
            if (ivMainBackground != null) ivMainBackground.setVisibility(View.GONE);
            if (vMainBackgroundDim != null) vMainBackgroundDim.setVisibility(View.GONE);

            int bgColor = Color.parseColor("#000000");
            switch (type) {
                case "preset_navy":
                    bgColor = Color.parseColor("#0f172a");
                    break;
                case "preset_purple":
                    bgColor = Color.parseColor("#1e1b4b");
                    break;
                case "preset_emerald":
                    bgColor = Color.parseColor("#064e3b");
                    break;
            }

            if (root != null) root.setBackgroundColor(bgColor);
            getWindow().getDecorView().setBackgroundColor(bgColor);
        }
    }

    private void showWallpaperSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_wallpaper_settings, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnPresetBlack = view.findViewById(R.id.btnPresetBlack);
        Button btnPresetNavy = view.findViewById(R.id.btnPresetNavy);
        Button btnPresetPurple = view.findViewById(R.id.btnPresetPurple);
        Button btnPresetEmerald = view.findViewById(R.id.btnPresetEmerald);
        Button btnPickGallery = view.findViewById(R.id.btnPickGalleryWallpaper);
        tvDialogWallpaperStatusTemp = view.findViewById(R.id.tvSelectedWallpaperStatus);
        Button btnSave = view.findViewById(R.id.btnSaveWallpaper);
        Button btnCancel = view.findViewById(R.id.btnCancelWallpaper);

        selectedWallpaperTypeTemp = StorageHelper.getWallpaperType(this);
        selectedWallpaperUriTemp = StorageHelper.getWallpaperUri(this);

        updateWallpaperStatusText();

        btnPresetBlack.setOnClickListener(v -> {
            selectedWallpaperTypeTemp = "default_black";
            selectedWallpaperUriTemp = null;
            updateWallpaperStatusText();
        });

        btnPresetNavy.setOnClickListener(v -> {
            selectedWallpaperTypeTemp = "preset_navy";
            selectedWallpaperUriTemp = null;
            updateWallpaperStatusText();
        });

        btnPresetPurple.setOnClickListener(v -> {
            selectedWallpaperTypeTemp = "preset_purple";
            selectedWallpaperUriTemp = null;
            updateWallpaperStatusText();
        });

        btnPresetEmerald.setOnClickListener(v -> {
            selectedWallpaperTypeTemp = "preset_emerald";
            selectedWallpaperUriTemp = null;
            updateWallpaperStatusText();
        });

        btnPickGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            wallpaperPickerLauncher.launch(intent);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            StorageHelper.saveWallpaper(this, selectedWallpaperTypeTemp, selectedWallpaperUriTemp);
            applyMainWallpaper();
            Toast.makeText(this, "Wallpaper updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateWallpaperStatusText() {
        if (tvDialogWallpaperStatusTemp == null) return;
        tvDialogWallpaperStatusTemp.setTextColor(Color.parseColor("#FF9500"));

        if ("custom_uri".equals(selectedWallpaperTypeTemp)) {
            tvDialogWallpaperStatusTemp.setText("Selected: Custom Gallery Photo");
        } else if ("preset_navy".equals(selectedWallpaperTypeTemp)) {
            tvDialogWallpaperStatusTemp.setText("Selected: Midnight Navy");
        } else if ("preset_purple".equals(selectedWallpaperTypeTemp)) {
            tvDialogWallpaperStatusTemp.setText("Selected: Dark Purple");
        } else if ("preset_emerald".equals(selectedWallpaperTypeTemp)) {
            tvDialogWallpaperStatusTemp.setText("Selected: Dark Emerald");
        } else {
            tvDialogWallpaperStatusTemp.setText("Selected: Pitch Black (Default)");
        }
    }

    private void showBarcodeSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_barcode_settings, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvCurrentBarcode = view.findViewById(R.id.tvCurrentBarcode);
        PreviewView previewView = view.findViewById(R.id.settingsCameraPreview);
        Button btnDone = view.findViewById(R.id.btnCloseBarcodeSettings);
        Button btnReset = view.findViewById(R.id.btnResetBarcode);

        String saved = StorageHelper.getSavedBarcode(this);
        tvCurrentBarcode.setText(saved != null ? saved : "None");

        startCamera(previewView, barcode -> {
            StorageHelper.saveBarcode(this, barcode);
            runOnUiThread(() -> tvCurrentBarcode.setText(barcode));
        });

        btnReset.setOnClickListener(v -> {
            StorageHelper.saveBarcode(this, null);
            tvCurrentBarcode.setText("None");
            Toast.makeText(this, "Barcode removed", Toast.LENGTH_SHORT).show();
        });

        btnDone.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void startClock() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentTab == 1) {
                    updateWorldClockDisplay();
                }
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    private void startCamera(PreviewView previewView, OnBarcodeScannedListener listener) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                BarcodeScanner scanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build());

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    processImageProxy(scanner, image, listener);
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(BarcodeScanner scanner, ImageProxy imageProxy, OnBarcodeScannedListener listener) {
        if (imageProxy.getImage() == null) return;

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null) {
                            listener.onScanned(rawValue);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Barcode scan failed", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private interface OnBarcodeScannedListener {
        void onScanned(String barcode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockRunnable);
        timerHandler.removeCallbacks(timerRunnable);
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
