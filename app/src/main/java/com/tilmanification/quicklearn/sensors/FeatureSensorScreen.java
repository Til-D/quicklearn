package com.tilmanification.quicklearn.sensors;

import android.content.Context;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;


class FeatureSensorScreen extends FeatureSensor {

    protected FeatureSensorScreen(Context context, Features features) {
        super(context, features);
    }

    /** Called to start the sensor */
    public void onStart() {}

    /** Called to stop the sensor */
    public void onStop() {}

    /** Called when screen is turned on */
    public void onScreenOn() {
        QLearnJsonLog.logSensorSnapshot(QlearnKeys.SCREEN_ON, Boolean.toString(true));
    }

    /** Called when screen is unlocked */
    public void onScreenUnlocked() {
        QLearnJsonLog.logSensorSnapshot(QlearnKeys.PHONE_UNLOCKED, Boolean.toString(true));
    }

    /** Called when screen is turned off */
    public void onScreenOff() {
        QLearnJsonLog.logSensorSnapshot(QlearnKeys.SCREEN_ON, Boolean.toString(false));
        QLearnJsonLog.tryUploadData();
    }

}
