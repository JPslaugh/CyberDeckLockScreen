package com.cyberdeck.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {

    private static final String TAG = "CyberDeck.ScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        Log.d(TAG, "Received action: " + intent.getAction());

        switch (intent.getAction()) {
            case Intent.ACTION_SCREEN_ON:
                // Screen turned on, show lock screen
                Intent lockIntent = new Intent(context, LockScreenActivity.class);
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                  Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                  Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(lockIntent);
                break;

            case Intent.ACTION_SCREEN_OFF:
                // Screen turned off
                Log.d(TAG, "Screen off");
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                // Device booted, start the service
                Intent serviceIntent = new Intent(context, LockScreenService.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }

                // Also show lock screen immediately
                Intent lockIntent = new Intent(context, LockScreenActivity.class);
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                  Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                  Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(lockIntent);
                break;

            case Intent.ACTION_USER_PRESENT:
                // User unlocked the device
                Log.d(TAG, "User present");
                break;
        }
    }
}
