package com.example.aeroalarm;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import androidx.annotation.Nullable;
import android.graphics.drawable.Drawable;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RingingActivity extends AppCompatActivity {
    private long alarmId;
    private ImageView ivAnimeCharacter;
    private PreviewView cameraPreview;
    private TextView tvTaskInstructions;
    private ExecutorService cameraExecutor;
    private String savedBarcode;
    private boolean isTaskCompleted = false;
    private long ringStartTime;
    private String characterName = "Random API";
    private SleepStatRepository sleepStatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowFlags();
        setContentView(R.layout.activity_ringing);

        ringStartTime = System.currentTimeMillis();
        sleepStatRepository = new SleepStatRepository(this);

        alarmId = getIntent().getLongExtra("ALARM_ID", -1);
        savedBarcode = StorageHelper.getSavedBarcode(this);

        TextView tvTime = findViewById(R.id.tvRingingTime);
        TextView tvLabel = findViewById(R.id.tvRingingLabel);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnSnooze = findViewById(R.id.btnSnooze);
        ivAnimeCharacter = findViewById(R.id.ivAnimeCharacter);
        cameraPreview = findViewById(R.id.ringingCameraPreview);
        tvTaskInstructions = findViewById(R.id.tvTaskInstructions);

        loadAnimeCharacter();

        List<AlarmModel> alarms = StorageHelper.getAlarms(this);
        AlarmModel currentAlarm = null;
        for (AlarmModel a : alarms) {
            if (a.getId() == alarmId) {
                currentAlarm = a;
                break;
            }
        }

        if (currentAlarm != null) {
            String time = String.format(Locale.US, "%02d:%02d:%02d %s", currentAlarm.getHour(), currentAlarm.getMinute(), currentAlarm.getSecond(), currentAlarm.getAmpm());
            tvTime.setText(time);
            tvLabel.setText(currentAlarm.getLabel() == null || currentAlarm.getLabel().isEmpty() ? "Wake Up!" : currentAlarm.getLabel());
        }

        if (savedBarcode != null && !savedBarcode.isEmpty()) {
            btnDismiss.setEnabled(false);
            btnDismiss.setText("Scan Barcode to Dismiss");
            tvTaskInstructions.setVisibility(View.VISIBLE);
            cameraPreview.setVisibility(View.VISIBLE);
            cameraExecutor = Executors.newSingleThreadExecutor();
            checkCameraPermissionAndStart();
        }

        btnDismiss.setOnClickListener(v -> dismissAlarm());
        btnSnooze.setOnClickListener(v -> snoozeAlarm());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(RingingActivity.this, "Complete the task to dismiss!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission needed for barcode task!", Toast.LENGTH_LONG).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                BarcodeScanner scanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build());

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    processImageProxy(scanner, image);
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("RingingActivity", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void processImageProxy(BarcodeScanner scanner, ImageProxy imageProxy) {
        if (isTaskCompleted || imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String value = barcode.getRawValue();
                        if (value != null && value.equals(savedBarcode)) {
                            isTaskCompleted = true;
                            runOnUiThread(this::onTaskCompleted);
                            break;
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void onTaskCompleted() {
        Toast.makeText(this, "Barcode Matched! You can dismiss now.", Toast.LENGTH_LONG).show();
        findViewById(R.id.btnDismiss).setEnabled(true);
        ((Button)findViewById(R.id.btnDismiss)).setText("Dismiss");
        tvTaskInstructions.setText("Task Completed!");
        cameraPreview.setVisibility(View.GONE);
        
        Log.d("AlarmWakeUp", "Alarm dismissed via Barcode at: " + 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
    }

    private void setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void loadAnimeCharacter() {
        if (StorageHelper.isOshiLocked(this)) {
            String oshiUrl = StorageHelper.getOshiUrl(this);
            if (oshiUrl != null && !oshiUrl.isEmpty()) {
                // Strip query parameters if they exist (sometimes causes issues with cookie-based URLs)
                if (oshiUrl.contains("?")) {
                    oshiUrl = oshiUrl.substring(0, oshiUrl.indexOf("?"));
                }
                
                characterName = "Your Oshi";
                Log.d("RingingActivity", "Loading Oshi URL: " + oshiUrl);
                
                Glide.with(this)
                    .load(oshiUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("RingingActivity", "Glide Oshi Load Failed", e);
                            // Fallback to random if Oshi fails
                            fetchRandomCharacter();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("RingingActivity", "Oshi Image Loaded Successfully");
                            return false;
                        }
                    })
                    .into(ivAnimeCharacter);
                return;
            }
        }

        fetchRandomCharacter();
    }

    private void fetchRandomCharacter() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.jikan.moe/v4/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JikanService service = retrofit.create(JikanService.class);
        service.getRandomCharacter().enqueue(new Callback<JikanResponse>() {
            @Override
            public void onResponse(Call<JikanResponse> call, Response<JikanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    characterName = response.body().getData().getName();
                    String imageUrl = response.body().getData().getImages().getJpg().getImageUrl();
                    Glide.with(RingingActivity.this).load(imageUrl).into(ivAnimeCharacter);
                }
            }

            @Override
            public void onFailure(Call<JikanResponse> call, Throwable t) {
                Log.e("RingingActivity", "Random API Fail", t);
            }
        });
    }

    private void dismissAlarm() {
        long dismissTime = System.currentTimeMillis();
        
        // Log to database
        SleepStatEntity stat = new SleepStatEntity(
                alarmId, // using alarmId as surrogate for setTime if not available
                ringStartTime,
                dismissTime,
                characterName
        );
        sleepStatRepository.insert(stat);

        stopService(new Intent(this, AlarmService.class));
        finish();
    }

    private void snoozeAlarm() {
        stopService(new Intent(this, AlarmService.class));
        List<AlarmModel> alarms = StorageHelper.getAlarms(this);
        AlarmModel snooze = new AlarmModel();
        snooze.setId(System.currentTimeMillis());
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MINUTE, 5);
        int h = cal.get(java.util.Calendar.HOUR);
        snooze.setHour(h == 0 ? 12 : h);
        snooze.setMinute(cal.get(java.util.Calendar.MINUTE));
        snooze.setSecond(cal.get(java.util.Calendar.SECOND));
        snooze.setAmpm(cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM ? "AM" : "PM");
        snooze.setLabel("Snooze");
        snooze.setActive(true);
        snooze.setDays(new java.util.ArrayList<>());
        alarms.add(snooze);
        StorageHelper.saveAlarms(this, alarms);
        AlarmManagerHelper.scheduleAlarm(this, snooze);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
