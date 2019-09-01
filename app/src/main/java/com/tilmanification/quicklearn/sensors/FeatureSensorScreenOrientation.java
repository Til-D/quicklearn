package com.tilmanification.quicklearn.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

class FeatureSensorScreenOrientation extends FeatureSensor {

    // ========================================================================
    // Fields
    // ========================================================================

    public final String	TAG	= FeatureSensorScreenOrientation.class.getSimpleName();

    public enum Orientation {
        LANDSCAPE, PORTRAIT, UNDEFINED;
    }

    private Orientation			orientation;
    private BroadcastReceiver	orientationListener;

    public FeatureSensorScreenOrientation(Context context, Features features) {
        super(context, features);
        this.orientation = Orientation.UNDEFINED;
    }

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public void onStart() {
        this.orientation = resolveOrientation(context.getResources().getConfiguration().orientation);
        this.orientationListener = new OrientationReceiver();
        IntentFilter orientationReceiverFilter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        context.registerReceiver(this.orientationListener, orientationReceiverFilter);
    }

    public void onStop() {
        context.unregisterReceiver(this.orientationListener);
    }

    // ========================================================================
    // Method
    // ========================================================================

    public void updateOrientation(Orientation orientation) {

        // if orientation changed
        if (!this.orientation.equals(orientation)) {
//            features.onScreenOrientationChanged(orientation.toString());
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.SCREEN_ORIENTATION, orientation.toString());
        }
        this.orientation = orientation;
    }

    private class OrientationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                updateOrientation(resolveOrientation(context.getResources().getConfiguration().orientation));
            }
        }
    }

    private static Orientation resolveOrientation(int orientation) {
        Orientation result;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                result = Orientation.LANDSCAPE;
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                result = Orientation.PORTRAIT;
                break;
            default:
                result = Orientation.UNDEFINED;
        }
        return result;
    }

}
