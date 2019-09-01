package com.tilmanification.quicklearn.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

class FeaturePhoneSensorRinger extends FeatureSensor {

    // ========================================================================
    // Fields
    // ========================================================================

    public final String				TAG	= FeaturePhoneSensorRinger.class.getSimpleName();

    private AudioManager			audioManager;

    private RingerModeReceiver		ringerModeReceiver;
    private VolumeChangedObserver	volumeChangedReceiver;
    private String ringerMode;

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeaturePhoneSensorRinger(Context context, Features features) {
        super(context, features);

        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.updateRingerMode();
        this.ringerMode = "";
    }

    public void onStart() {

        this.ringerModeReceiver = new RingerModeReceiver();
        IntentFilter ringerModeReceiverFilter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        this.context.registerReceiver(ringerModeReceiver, ringerModeReceiverFilter);

        this.volumeChangedReceiver = new VolumeChangedObserver(this.context, new Handler());
        this.context
                .getApplicationContext()
                .getContentResolver()
                .registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, volumeChangedReceiver);
    }

    public void onStop() {

        if (ringerModeReceiver != null) {
            this.context.unregisterReceiver(ringerModeReceiver);
            this.ringerModeReceiver = null;
        }

        if (volumeChangedReceiver != null) {
            this.context.getApplicationContext().getContentResolver().unregisterContentObserver(volumeChangedReceiver);
            this.volumeChangedReceiver = null;
        }

    }

    // ========================================================================
    // Method
    // ========================================================================

    public void updateRingerMode() {
        int ringerModeInt = this.audioManager.getRingerMode();
        String mode = convertFrom(ringerModeInt);
        if(mode!=null && !mode.equals("") && !mode.equals(ringerMode)) {
            ringerMode = mode;
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.RINGER_MODE, ringerMode);
        }
//        features.onRingerModeChanged(ringerMode);

    }

    private static final String convertFrom(int ringerMode) {
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) return Features.RINGER_MODE_SILENT;
        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) return Features.RINGER_MODE_VIBRATE;
        return Features.RINGER_MODE_NORMAL;
    }

    class RingerModeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            updateRingerMode();
        }
    }

    class VolumeChangedObserver extends ContentObserver {

        public VolumeChangedObserver(Context c, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateRingerMode();
        }
    }

}
