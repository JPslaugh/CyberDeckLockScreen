package com.cyberdeck.lockscreen;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class VolumeOverlay {

    private Context context;
    private WindowManager windowManager;
    private View overlayView;
    private TextView volumeText;
    private TextView volumeBar;
    private Handler handler;
    private Runnable hideRunnable;
    private boolean isShowing = false;

    public VolumeOverlay(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.handler = new Handler();

        // Inflate the overlay layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        overlayView = inflater.inflate(R.layout.volume_overlay, null);

        volumeText = overlayView.findViewById(R.id.volumeText);
        volumeBar = overlayView.findViewById(R.id.volumeBar);

        hideRunnable = this::hide;
    }

    public void show(int volumePercent) {
        // Update volume first
        updateVolume(volumePercent);

        if (!isShowing) {
            try {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    200,
                    400,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                );

                // Cover entire screen
                params.gravity = Gravity.TOP | Gravity.RIGHT;
                params.y = 0;
                params.x = 0;

                windowManager.addView(overlayView, params);
                isShowing = true;
            } catch (Exception e) {
                e.printStackTrace();
                isShowing = false;
                return;
            }
        }

        // Remove existing hide callback and schedule new one
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, 2000);
    }

    private void updateVolume(int volumePercent) {
        volumeText.setText(String.format("[VOLUME: %d%%]", volumePercent));

        // Create ASCII bar (20 characters)
        StringBuilder bar = new StringBuilder();
        int filledBars = (volumePercent * 20) / 100;

        for (int i = 0; i < 20; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }

        volumeBar.setText(bar.toString());
    }

    private void hide() {
        if (isShowing) {
            try {
                windowManager.removeView(overlayView);
                isShowing = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy() {
        handler.removeCallbacks(hideRunnable);
        hide();
    }
}
