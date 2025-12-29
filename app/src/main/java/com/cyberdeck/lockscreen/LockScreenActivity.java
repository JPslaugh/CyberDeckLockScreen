package com.cyberdeck.lockscreen;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LockScreenActivity extends Activity {

    private static final String CORRECT_PIN = "757175";
    private TextView timeText, dateText, batteryText, wifiText, uptimeText, statusText, errorText;
    private EditText pinInput;
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
        errorText = findViewById(R.id.errorText);
        pinInput = findViewById(R.id.pinInput);

        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        // Setup PIN input
        setupPinInput();

        updateSystemInfo();

        // Show keyboard automatically
        pinInput.postDelayed(() -> {
            pinInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(pinInput, InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }

    private void setupPinInput() {
        pinInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorText.setText("");

                if (s.length() == 6) {
                    checkPin(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkPin(String enteredPin) {
        if (CORRECT_PIN.equals(enteredPin)) {
            unlockScreen();
        } else {
            errorText.setText("[ACCESS DENIED]");
            pinInput.setText("");
            pinInput.postDelayed(() -> errorText.setText(""), 2000);
        }
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
}
