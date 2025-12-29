package com.cyberdeck.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class VolumeChangeReceiver extends BroadcastReceiver {

    private VolumeOverlay volumeOverlay;

    public VolumeChangeReceiver(VolumeOverlay volumeOverlay) {
        this.volumeOverlay = volumeOverlay;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
            (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION") ||
             intent.getAction().equals("android.media.STREAM_MUTE_CHANGED_ACTION"))) {

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            int volumePercent = (currentVolume * 100) / maxVolume;

            volumeOverlay.show(volumePercent);
        }
    }
}
