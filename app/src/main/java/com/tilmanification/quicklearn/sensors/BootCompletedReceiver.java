package com.tilmanification.quicklearn.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tilmanification.quicklearn.NotificationTriggerService;
import com.tilmanification.quicklearn.QuickLearnPrefs;
import com.tilmanification.quicklearn.Util;
import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String	TAG	= BootCompletedReceiver.class.getSimpleName();

    // ========================================================================
    // Methods
    // ========================================================================

    @Override
    public void onReceive(Context context, Intent intent) {

        if (QuickLearnPrefs.RESTART_SERVICE_AFTER_REBOOT) {

            // Start the notification trigger service
            if(Util.getBool(context, QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {

                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "Restarting NotificationTriggerService after boot-up");
                }

                Intent intentNotificationTriggerService = new Intent(context, NotificationTriggerService.class);
                context.startService(intentNotificationTriggerService);

            } else {
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "Restarting BoredomPredictionService after boot-up -- no consent yet");
                }
            }

            QLearnJsonLog.startLogger(context.getApplicationContext());
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.PHONE_REBOOT, Boolean.toString(true));

        }

    }
}
