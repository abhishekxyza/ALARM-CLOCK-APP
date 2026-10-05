package com.example.aeroalarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.TimeZone;

public class AnalogClockView extends View {

    private Paint dialPaint;
    private Paint borderPaint;
    private Paint tickPaint;
    private Paint numberPaint;
    private Paint hourHandPaint;
    private Paint minuteHandPaint;
    private Paint secondHandPaint;
    private Paint centerPivotPaint;

    private int hour = 12;
    private int minute = 0;
    private int second = 0;
    private TimeZone timeZone = TimeZone.getDefault();

    public AnalogClockView(Context context) {
        super(context);
        init();
    }

    public AnalogClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalogClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dialPaint.setColor(Color.parseColor("#1c1c1e"));
        dialPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#2c2c2e"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#757575"));
        tickPaint.setStrokeWidth(3f);

        numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setColor(Color.WHITE);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTextSize(36f);

        hourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourHandPaint.setColor(Color.WHITE);
        hourHandPaint.setStyle(Paint.Style.STROKE);
        hourHandPaint.setStrokeWidth(12f);
        hourHandPaint.setStrokeCap(Paint.Cap.ROUND);

        minuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minuteHandPaint.setColor(Color.WHITE);
        minuteHandPaint.setStyle(Paint.Style.STROKE);
        minuteHandPaint.setStrokeWidth(8f);
        minuteHandPaint.setStrokeCap(Paint.Cap.ROUND);

        secondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondHandPaint.setColor(Color.parseColor("#FF9500"));
        secondHandPaint.setStyle(Paint.Style.STROKE);
        secondHandPaint.setStrokeWidth(4f);
        secondHandPaint.setStrokeCap(Paint.Cap.ROUND);

        centerPivotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPivotPaint.setColor(Color.parseColor("#FF9500"));
        centerPivotPaint.setStyle(Paint.Style.FILL);
    }

    public void setTimeZone(TimeZone tz) {
        if (tz != null) {
            this.timeZone = tz;
            updateTime();
        }
    }

    public void updateTime() {
        Calendar cal = Calendar.getInstance(timeZone);
        this.hour = cal.get(Calendar.HOUR);
        this.minute = cal.get(Calendar.MINUTE);
        this.second = cal.get(Calendar.SECOND);
        invalidate();
    }

    public void setTime(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(centerX, centerY) - 16;

        if (radius <= 0) return;

        // Dial & Border
        canvas.drawCircle(centerX, centerY, radius, dialPaint);
        canvas.drawCircle(centerX, centerY, radius, borderPaint);

        // Ticks & Numbers
        float numberRadius = radius * 0.76f;
        Rect textBounds = new Rect();

        for (int i = 1; i <= 12; i++) {
            double angle = Math.toRadians((i * 30) - 90);

            // Draw Number
            String numStr = String.valueOf(i);
            numberPaint.getTextBounds(numStr, 0, numStr.length(), textBounds);
            float numX = (float) (centerX + numberRadius * Math.cos(angle));
            float numY = (float) (centerY + numberRadius * Math.sin(angle)) + (textBounds.height() / 2f);
            canvas.drawText(numStr, numX, numY, numberPaint);

            // Ticks
            float outerX = (float) (centerX + radius * Math.cos(angle));
            float outerY = (float) (centerY + radius * Math.sin(angle));
            float innerX = (float) (centerX + (radius - 16) * Math.cos(angle));
            float innerY = (float) (centerY + (radius - 16) * Math.sin(angle));
            tickPaint.setStrokeWidth(6f);
            canvas.drawLine(innerX, innerY, outerX, outerY, tickPaint);
        }

        // Minute Ticks
        tickPaint.setStrokeWidth(2f);
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) continue;
            double angle = Math.toRadians((i * 6) - 90);
            float outerX = (float) (centerX + radius * Math.cos(angle));
            float outerY = (float) (centerY + radius * Math.sin(angle));
            float innerX = (float) (centerX + (radius - 8) * Math.cos(angle));
            float innerY = (float) (centerY + (radius - 8) * Math.sin(angle));
            canvas.drawLine(innerX, innerY, outerX, outerY, tickPaint);
        }

        // Hour Hand
        double hourAngle = Math.toRadians(((hour % 12) + minute / 60.0) * 30 - 90);
        float hourHandLen = radius * 0.45f;
        float hourX = (float) (centerX + hourHandLen * Math.cos(hourAngle));
        float hourY = (float) (centerY + hourHandLen * Math.sin(hourAngle));
        canvas.drawLine(centerX, centerY, hourX, hourY, hourHandPaint);

        // Minute Hand
        double minAngle = Math.toRadians((minute + second / 60.0) * 6 - 90);
        float minHandLen = radius * 0.65f;
        float minX = (float) (centerX + minHandLen * Math.cos(minAngle));
        float minY = (float) (centerY + minHandLen * Math.sin(minAngle));
        canvas.drawLine(centerX, centerY, minX, minY, minuteHandPaint);

        // Second Hand (Orange)
        double secAngle = Math.toRadians(second * 6 - 90);
        float secHandLen = radius * 0.82f;
        float secX = (float) (centerX + secHandLen * Math.cos(secAngle));
        float secY = (float) (centerY + secHandLen * Math.sin(secAngle));

        // Tail extending backward
        float tailX = (float) (centerX - (radius * 0.15f) * Math.cos(secAngle));
        float tailY = (float) (centerY - (radius * 0.15f) * Math.sin(secAngle));
        canvas.drawLine(tailX, tailY, secX, secY, secondHandPaint);

        // Center Pivot Circle
        canvas.drawCircle(centerX, centerY, 8f, centerPivotPaint);
    }
}
