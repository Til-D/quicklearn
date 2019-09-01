package com.tilmanification.quicklearn.sensors;

import android.content.Context;
import android.util.Log;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

class FeatureSensorNotifications extends FeatureSensor implements NotificationListener {

    // ========================================================================
    // Fields
    // ========================================================================

    public final String							TAG	= FeatureSensorNotifications.class.getSimpleName();

    public static FeatureSensorNotifications	instance;

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeatureSensorNotifications(Context context, Features features) {
        super(context, features);
        instance = this;
    }

    public void onStart() {
        QLearnNotificationListenerService.addNotificationListener(this);
    }

    public void onStop() {
        QLearnNotificationListenerService.removeNotificationListener(this);
    }

    // ========================================================================
    // Method
    // ========================================================================

    public void onNotificationPosted(String packageName) {
        Log.i(TAG, "onNotificationPosted( " + packageName + " )");
//        features.onNotificationPosted(packageName);
        QLearnJsonLog.logSensorSnapshot(QlearnKeys.NOTIF_POSTED_PACKAGE, packageName);
    }

    @Override
    public void onNotificationRemoved(String packageName) {
        Log.i(TAG, "onNotificationRemoved( " + packageName + " )");
        QLearnJsonLog.logSensorSnapshot(QlearnKeys.NOTIF_REMOVED_PACKAGE, packageName);

    }

}
