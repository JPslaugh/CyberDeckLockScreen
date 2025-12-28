package com.cyberdeck.lockscreen;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LockScreenActivity extends Activity {

    private TextView timeText, dateText, batteryText, wifiText, uptimeText, statusText;
    private GestureDetector gestureDetector;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup window flags to show over lock screen
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        setContentView(R.layout.activity_lockscreen);

        // Initialize views
        timeText = findViewById(R.id.timeText);
        dateText = findViewById(R.id.dateText);
        batteryText = findViewById(R.id.batteryText);
        wifiText = findViewById(R.id.wifiText);
        uptimeText = findViewById(R.id.uptimeText);
        statusText = findViewById(R.id.statusText);

        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        // Setup gesture detector for swipe to unlock
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        updateSystemInfo();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void updateSystemInfo() {
        // Update time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        timeText.setText(sdf.format(new Date()));

        // Update date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateText.setText("[" + dateFormat.format(new Date()) + "]");

        // Update battery
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int)((level / (float)scale) * 100);

            String bars = "";
            int numBars = batteryPct / 10;
            for (int i = 0; i < 10; i++) {
                bars += i < numBars ? "█" : "░";
            }
            batteryText.setText("Battery: [" + batteryPct + "%] " + bars);
        }

        // Update WiFi
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiText.setText("WiFi: [Connected]");
        } else {
            wifiText.setText("WiFi: [Disconnected]");
        }

        // Update Uptime
        updateUptime();

        // Update every second
        timeText.postDelayed(this::updateSystemInfo, 1000);
    }

    private void updateUptime() {
        try {
            long uptimeMillis = SystemClock.elapsedRealtime();
            long days = uptimeMillis / (1000 * 60 * 60 * 24);
            long hours = (uptimeMillis / (1000 * 60 * 60)) % 24;
            long minutes = (uptimeMillis / (1000 * 60)) % 60;

            uptimeText.setText(String.format(Locale.US, "Uptime: [%dd %dh %dm]", days, hours, minutes));
        } catch (Exception e) {
            uptimeText.setText("Uptime: [N/A]");
        }
    }

    private void unlockScreen() {
        statusText.setText("[SYSTEM_UNLOCKED]");
        statusText.setTextColor(0xFF00FF00);

        // Finish activity to dismiss lock screen
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from dismissing lock screen
    }

    // Gesture listener for swipe to unlock
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffY) > Math.abs(diffX)) {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {
                        // Swipe up detected
                        unlockScreen();
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
