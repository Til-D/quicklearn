package com.tilmanification.quicklearn.sensors;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.tilmanification.quicklearn.NotificationTriggerService;
import com.tilmanification.quicklearn.QuickLearnPrefs;
import com.tilmanification.quicklearn.StudyManager;
import com.tilmanification.quicklearn.Util;
import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

import java.util.ArrayList;
import java.util.Date;

@TargetApi(18)
public class QLearnNotificationListenerService extends NotificationListenerService {

    private static final String					TAG			= QLearnNotificationListenerService.class.getSimpleName();

    public static QLearnNotificationListenerService	instance;

    public static boolean						notificationAccessEnabled;

    private static ArrayList<NotificationListener>		listeners	= new ArrayList<NotificationListener>();

    public static void addNotificationListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public static boolean removeNotificationListener(NotificationListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public IBinder onBind(Intent mIntent) {
        Log.i(TAG, "onBind( .. )");
        IBinder mIBinder = super.onBind(mIntent);
        notificationAccessEnabled = true;
        instance = this;

        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        Log.i(TAG, "onUnbind( .. )");
        boolean mOnUnbind = super.onUnbind(mIntent);
        notificationAccessEnabled = false;
        return mOnUnbind;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        int pendingCount = getPendingNotificationCount(this);

        shortSleep();

        if (sbn.isClearable() && !sbn.isOngoing()) {
            for (NotificationListener listener : listeners) {
                listener.onNotificationPosted(sbn.getPackageName());
            }

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.d(TAG, "onNotificationPosted( " + str(sbn) + " ) - pending: " + pendingCount);
            }

            QLearnJsonLog.logSensorSnapshot(QlearnKeys.NOTIF_POSTED, sbn.getPackageName());

            // Start the notification trigger service
            if(Util.getBool(this, QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {
                Intent intentNotificationTriggerService = new Intent(this, NotificationTriggerService.class);
                startService(intentNotificationTriggerService);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        if (sbn.isClearable() && !sbn.isOngoing()) {
            for (NotificationListener listener : listeners) {
                listener.onNotificationRemoved(sbn.getPackageName());
            }

            int pendingCount = getPendingNotificationCount(this);

            boolean firstInteraction = Util.getBool(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
            boolean ignored = Util.getBool(getApplicationContext(), QuickLearnPrefs.NOTIF_IGNORED, false);
            if(firstInteraction && !ignored) {
                int condition = StudyManager.getInstance(getApplicationContext()).current_condition;
                long notifPostedMillis = Util.getLong(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, 0);
//            String classifier = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED, "");
//            boolean prediction = Util.getBool(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_CLASSIFIER, false);
//            String probability = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_PROBABILITY, "");

                QLearnJsonLog.onQLearnNotificationDismissed(condition, notifPostedMillis, sbn.getPackageName(), pendingCount, firstInteraction);
                Util.put(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
                StudyManager.getInstance(getApplicationContext()).resetWordCount();
            }
            Util.put(getApplicationContext(), QuickLearnPrefs.NOTIF_IGNORED, false);

            shortSleep();

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.d(TAG, "onNotificationRemoved( " + str(sbn) + " ) - pending: " + pendingCount);
            }

            //log other notifications
            if(!getPackageName().equals(sbn.getPackageName())) {
                QLearnJsonLog.logSensorSnapshot(QlearnKeys.QLEARN_DISMISSED, sbn.getPackageName());
            }

        }
    }

    protected void shortSleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ie) {}
    }

    public static int getPendingNotificationCount() {
        if (instance != null) { return getPendingNotificationCount(instance); }
        return 0;
    }

    private static int getPendingNotificationCount(NotificationListenerService nls) {
        StatusBarNotification[] activeNotifications = nls.getActiveNotifications();

        int pendingNotifications = 0;
        for (StatusBarNotification sbn : activeNotifications) {
            if (sbn.isClearable() && !sbn.isOngoing()) {
                pendingNotifications++;
            }
        }
        return pendingNotifications;
    }

    protected void log(StatusBarNotification sbn) {

        Notification n = sbn.getNotification();

        Log.d(TAG, "--- Notification ------------------");

        Log.d(TAG, "ID: " + sbn.getId());
        Log.d(TAG, "package: " + sbn.getPackageName());
        Log.d(TAG, "ticker: " + n.tickerText);

        Log.d(TAG, "post time: " + new Date(sbn.getPostTime()));
        Log.d(TAG, "clearable: " + sbn.isClearable());
        Log.d(TAG, "ongoing: " + sbn.isOngoing());

        Log.d(TAG, "sound: " + (n.sound != null));
        Log.d(TAG, "vibration: " + (n.vibrate != null));

        Log.d(TAG, "active notifications: " + this.getActiveNotifications().length);

        Log.d(TAG, "-----------------------------------");

    }

    private static String str(StatusBarNotification n) {
        return n.getPackageName();
    }
}
