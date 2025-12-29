package com.cyberdeck.lockscreen;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class LockScreenService extends Service {

    private static final String TAG = "CyberDeck.LockService";
    private ScreenReceiver screenReceiver;
    private VolumeChangeReceiver volumeChangeReceiver;
    private VolumeOverlay volumeOverlay;
    private KeyguardManager.KeyguardLock keyguardLock;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        // Disable system lock screen
        disableSystemLockScreen();

        // Setup volume overlay
        volumeOverlay = new VolumeOverlay(this);

        // Register screen receiver
        screenReceiver = new ScreenReceiver();
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, screenFilter);

        // Register volume change receiver
        volumeChangeReceiver = new VolumeChangeReceiver(volumeOverlay);
        IntentFilter volumeFilter = new IntentFilter();
        volumeFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        volumeFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
        registerReceiver(volumeChangeReceiver, volumeFilter);

        Log.d(TAG, "Volume overlay initialized");
    }

    private void disableSystemLockScreen() {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardLock = keyguardManager.newKeyguardLock("CyberDeckLock");
            keyguardLock.disableKeyguard();
            Log.d(TAG, "System lock screen disabled");
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable system lock screen", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY; // Restart service if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        if (screenReceiver != null) {
            unregisterReceiver(screenReceiver);
        }
        if (volumeChangeReceiver != null) {
            unregisterReceiver(volumeChangeReceiver);
        }
        if (volumeOverlay != null) {
            volumeOverlay.destroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
