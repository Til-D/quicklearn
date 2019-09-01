package com.tilmanification.quicklearn.log;

import android.content.Context;
import android.util.Log;

import com.tilmanification.quicklearn.QuickLearnPrefs;
import com.tilmanification.quicklearn.sensors.AbstractSensor;
import com.tilmanification.quicklearn.sensors.QLearnNotificationListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

//import org.pielot.borpred.BoredomNotificationListenerService;

public class QLearnJsonLog implements com.tilmanification.quicklearn.log.QlearnKeys {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String			TAG			= QLearnJsonLog.class.getSimpleName();
    public static final String			EMPTY		= "";
    public static final String			DELIMITER	= ",";

    // ========================================================================
    // Fields
    // ========================================================================

    private static JsonLogTransmitter	logger;

    private static String				pid;

    // ========================================================================
    // Methods
    // ========================================================================

    public static synchronized void startLogger(Context context) {

        if (logger == null) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "startLogger()");
            }

            pid = com.tilmanification.quicklearn.log.UuidGen.getPid6(context);

            logger = new JsonLogTransmitter(context, QuickLearnPrefs.APP_NAME, ServerComm.SERVER);
            logger.host = com.tilmanification.quicklearn.log.ServerComm.LOG_SERVER_URL;
            logger.allowFlushVia3G = QuickLearnPrefs.LOG_USE_3G;
            logger.checkFlushFrequency = QuickLearnPrefs.LOG_FLUSH_FREQ;
            logger.includeEmptyValues = QuickLearnPrefs.LOG_INCLUDE_EMPTY_VALUES;
            logger.deleteWhenBroken = QuickLearnPrefs.LOG_DELETE_WHEN_BROKEN;
            logger.onStart();

        } else {

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "startLogger() -- logger already initialized. ignoring call");
            }
        }
    }

//    public static synchronized void logFirstStart(Context context) {
//        if (logger != null) {
//            LogDevice.logDeviceDetailsIfFirstStart(context, logger, INST);
//        }
//    }

    public static synchronized void onStop0() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onStop()");
        }
        if (logger != null) {
            logger.onStop();
        }
        logger = null;
    }

    private static synchronized JSONObject prepareJSONSnapshot() {

        JSONObject json = new JSONObject();
        try {
            json.put(LogKeys.KEY_T, LogKeys.EMPTY);
            json.put(LogKeys.KEY_UPTIME_SINCE_START_S, LogKeys.EMPTY);
            // json.put(LogKeys.KEY_VC, LogKeys.EMPTY);
            json.put(LogKeys.KEY_DATE, LogKeys.EMPTY);
            json.put(KEY_PID, pid);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR creating json object!");
            }
            e.printStackTrace();
        }
        return json;
    }

    public static synchronized void log(AbstractSensor sensor) {
        logSensorSnapshot(sensor.jsonify());
    }

    public static synchronized void logSensorSnapshot(JSONObject sensor) {
        JSONObject json = prepareJSONSnapshot();
        try {
            Iterator<?> keys = sensor.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                json.put(key, sensor.getString(key));
            }
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR adding sensor log data to json object");
            }
            e.printStackTrace();
        }
        logJSONSnapshot(json);
    }

    public static synchronized void logSensorSnapshot(String key, String value) {
//        Log.i(TAG, "logSnapshot: key=" + key + ", value=" + value);
        JSONObject json = new JSONObject();
        try {
            json.put(com.tilmanification.quicklearn.log.QlearnKeys.SENSOR_ID, key);
            json.put(com.tilmanification.quicklearn.log.QlearnKeys.SENSOR_VALUE, value);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR adding notification clicked data to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);
    }

    public static synchronized void logSnapshot(String key, String value) {
//        Log.i(TAG, "logSnapshot: key=" + key + ", value=" + value);
        JSONObject json = prepareJSONSnapshot();
        try {
            json.put(key, value);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR adding sensor log data to json object");
            }
            e.printStackTrace();
        }
        logJSONSnapshot(json);
    }

    public static synchronized void logJSONSnapshot(JSONObject json) {
        try {
            if (logger != null && logger.isRunning()) {
                logger.log(pid, json);
            } else {
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "logger not available or not running");
                }
            }

        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.w(TAG, e + " in createSnapshot()", e);
            }
        }
    }

    public static synchronized void tryUploadData() {
        if (logger != null && logger.isRunning()) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "forceFlush()");
            }
            logger.startUploadFiles(false);
        }
    }

    // ========================================================================
    // Accessibility
    // ========================================================================

    public static synchronized void onAppChanged(String packageName, boolean started) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            if (started) {
                Log.i(TAG, packageName + " OPENED");
            } else {
                Log.d(TAG, packageName + " CLOSED");
            }
        }

        logSnapshot(PACKAGE, packageName);
    }

    public static synchronized void onRingerModeChanged(int ringerMode) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Ringer mode changed to " + ringerMode);
        }

        logSnapshot(RINGER_MODE, String.valueOf(ringerMode));
    }

    public static synchronized void onClearAllNotifications() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Cleared all notifications");
        }

        JSONObject json = prepareJSONSnapshot();
        try {
            Iterator<?> keys = json.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (key.indexOf('.') > 0) {
                    json.put(key, EMPTY);
                }
            }
            json.put(NOTIFS_CLEARED, TRUE);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR adding sensor log data to json object");
            }
            e.printStackTrace();
        }
        logJSONSnapshot(json);

    }

    public static synchronized void onClearSingleNotification() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Cleared single notification.");
        }

        logSnapshot(CLEAR_NOTIFICATION, TRUE);
    }

    // ========================================================================
    // API 18
    // ========================================================================

    public static synchronized void onOpenNotificationBar() {

        boolean pendingNotifications = QLearnNotificationListenerService.getPendingNotificationCount() > 0;
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "openend notification bar, pending notifications == " + pendingNotifications);
        }

        logSnapshot(OPEN_NOTIFICATION_CENTER, pendingNotifications ? TRUE : FALSE);
    }

    public static synchronized void onSurveyFilledIn(String socialContext,
                                                     String location,
                                                     String timingOpportune,
                                                     int condition) {

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onSurveyFilledIn for condition: " + condition);
        }

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(QLEARN_CONDITION, condition);
            values.put(SURVEY_SOCIAL_CONTEXT, socialContext);
            values.put(SURVEY_LOCATION, location);
            values.put(SURVEY_TIMING_OPPORTUNE, timingOpportune);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_SURVEY_RESPONSE);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR putting survey data into json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);
    }

    public static synchronized void onNotificationInteraction(int condition,
                                                              long notifPostedMillis) {
//                                                              String classifier,
//                                                              boolean prediction,
//                                                              String probability) {


        String notifPostedDate = LogUtil.getTimeStringMySql(notifPostedMillis);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "QLearn notification clicked: posted=" + notifPostedMillis + ", " + notifPostedDate + ", condition: " + condition);
        }
        long clickedAfterMillis = System.currentTimeMillis() - notifPostedMillis;

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(QLEARN_CONDITION, condition);
            values.put(QLEARN_NOTIF_POSTED_MS, "" + notifPostedMillis);
            values.put(QLEARN_NOTIF_POSTED_DATE, notifPostedDate);
            values.put(QLEARN_NOTIFICATION_CLICKED_AFTER_SEC, clickedAfterMillis);
//            values.put(BOREDOM_CLASSIFIER, classifier);
//            values.put(BOREDOM_PREDICTION, prediction);
//            values.put(BOREDOM_PROBABILITY, probability);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_NOTIFICATION_INTERACTION);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR adding notification clicked data to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);
    }

    public static synchronized void onSessionFinished(int mode, int condition, int set_count, int word_count) {

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onSessionFinished: mode=" + mode + ", condition=" + condition + ", set_count: " + set_count + ", word_count: " + word_count);
        }

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(QLEARN_MODE, "" + mode);
            values.put(QLEARN_CONDITION, condition);
            values.put(QLEARN_NUMBER_OF_WORDS_DONE, word_count);
            values.put(QLEARN_NUMBER_OF_SETS_DONE, set_count);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_SESSION_FINISHED);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR session finished log data to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);
    }

    public static synchronized void onWordReviewed(String word_key, String word_src_lang, String word_target_lang, int mode, int condition, boolean correct, boolean seen_before) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onWordReviewed: word=" + word_key);
        }

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();
            values.put(QLEARN_MODE, "" + mode);
            values.put(QLEARN_CONDITION, condition);
            values.put(WORD_KEY, word_key);
            values.put(WORD_SRC_LANGUAGE, word_src_lang);
            values.put(WORD_TARGET_LANGUAGE, word_target_lang);
            values.put(WORD_GUESSED_CORRECTLY, correct);
            values.put(WORD_SEEN_BEFORE, seen_before);
            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_WORD_REVIEWED);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR word reviewed log data to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);
    }

    public static synchronized void onQLearnNotificationIgnored(int condition,
                                                                long notifPostedMillis,
                                                                String packageName) {
//                                                                String classifier,
//                                                                boolean prediction,
//                                                                String probability) {

        String notifPostedDate = LogUtil.getTimeStringMySql(notifPostedMillis);
        Log.i(TAG, "notification ignored: posted=" + notifPostedMillis + ", " + notifPostedDate + " (" + packageName + ")");
        long cancelledAfterMs = System.currentTimeMillis() - notifPostedMillis;

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();
            values.put(PACKAGE, packageName);
            values.put(QLEARN_CONDITION, condition);
            values.put(QLEARN_NOTIF_POSTED_MS, "" + notifPostedMillis);
            values.put(QLEARN_NOTIF_POSTED_DATE, notifPostedDate);
            values.put(QLEARN_NOTIF_CANCELLED_AFTER_MS, cancelledAfterMs);
//            values.put(BOREDOM_CLASSIFIER, classifier);
//            values.put(BOREDOM_PREDICTION, prediction);
//            values.put(BOREDOM_PROBABILITY, probability);
            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_IGNORED);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR notification ignored log data to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);

    }

    public static synchronized void onQLearnNotificationDismissed(int condition,
                                                                long notifPostedMillis,
                                                                  String packageName,
//                                                                String classifier,
//                                                                boolean prediction,
//                                                                String probability,
                                                                int pendingCount,
                                                                boolean firstInteraction) {

        String notifPostedDate = LogUtil.getTimeStringMySql(notifPostedMillis);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "notification ignored: posted=" + notifPostedMillis + ", " + notifPostedDate + " (" + packageName + ")");
        }
        long dismissedAfterMs = System.currentTimeMillis() - notifPostedMillis;

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();
            values.put(PACKAGE, packageName);
            values.put(QLEARN_CONDITION, condition);
            values.put(QLEARN_NOTIF_POSTED_MS, "" + notifPostedMillis);
            values.put(QLEARN_NOTIF_POSTED_DATE, notifPostedDate);
            values.put(QLEARN_NOTIF_DISMISSED_AFTER_MS, dismissedAfterMs);
            values.put(QLEARN_NOTIF_DISMISSED_PENDING_COUNT, pendingCount);
            values.put(QLEARN_NOTIF_FIRST_INTERACTION, firstInteraction);
//            values.put(QlearnKeys.BOREDOM_CLASSIFIER, classifier);
//            values.put(QlearnKeys.BOREDOM_PREDICTION, prediction);
//            values.put(QlearnKeys.BOREDOM_PROBABILITY, probability);
            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_DISMISSED);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR notification dismissed log data to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);

    }

    public static void onQLearnConditionSwitch(int old_condition, int new_condition, long days_passed) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onQLearnConditionSwitch( old condition: "
                    + old_condition
                    + ", new condition: "
                    + new_condition
                    + ", days_passed: "
                    + days_passed);
        }

        JSONObject json = prepareJSONSnapshot();
        JSONObject values = new JSONObject();
        try {

            values.put(QlearnKeys.QLEARN_CONDITION_OLD_COND, old_condition);
            values.put(QlearnKeys.QLEARN_CONDITION_NEW_COND, new_condition);
            values.put(QlearnKeys.QLEARN_CONDITION_DAYS_PASSED, days_passed);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_CONDITION_SWITCH);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging condition switch to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);

    }

    /**
     * is called when all words have been viewed in one single condition, this resets the conditional word lists
     */
    public static void onQLearnConditionWordsReset(int condition) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onQLearnConditionWordsReset: condition: " + condition);
        }

        JSONObject json = prepareJSONSnapshot();
        JSONObject values = new JSONObject();
        try {

            values.put(QlearnKeys.QLEARN_CONDITION, condition);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_CONDITIONAL_WORD_LIST_RESET);
            json.put(QlearnKeys.SENSOR_VALUE, values);

        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging condition reset to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);
    }

    public static void onESMTriggered () {

        JSONObject json = prepareJSONSnapshot();
        try {

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "onESMTriggered()");
            }

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_SURVEY_TRIGGERED);
            json.put(QlearnKeys.SENSOR_VALUE, true);

        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging QLearn status to json object");
            }
            e.printStackTrace();
        }
        logSensorSnapshot(json);
    }

    public static void onESMDismissed () {

        JSONObject json = prepareJSONSnapshot();
        try {

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "onESMDismissed()");
            }

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_SURVEY_DISMISSED);
            json.put(QlearnKeys.SENSOR_VALUE, true);

        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging QLearn status to json object");
            }
            e.printStackTrace();
        }
        logSensorSnapshot(json);
    }

    public static void onQLearnOpened (int condition,
                                      int mode) { //notification click or app_launch
//                                      String classifier,
//                                      boolean prediction,
//                                      String probability) {

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onQLearnOpened( condition: "
                    + condition
                    + ", mode: "
                    + mode + " )");
//                    + ", classifier: "
//                    + classifier
//                    + ", prediction: "
//                    + prediction
//                    + ", probability: "
//                    + probability + " )");
        }

        JSONObject json = prepareJSONSnapshot();
        JSONObject values = new JSONObject();
        try {

            values.put(QlearnKeys.QLEARN_MODE, mode);
//            values.put(QlearnKeys.BOREDOM_CLASSIFIER, classifier);
//            values.put(QlearnKeys.BOREDOM_PREDICTION, prediction);
//            values.put(QlearnKeys.BOREDOM_PROBABILITY, probability);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_APP_LAUNCH);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging QLearn status to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);

    }

    public static void onVocabularyExtensionTriggered(int previousSize, int newSize) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onVocabularyExtensionTriggered( previousSize: "
                    + previousSize
                    + ", newSize: "
                    + newSize + " )");
        }

        JSONObject json = prepareJSONSnapshot();
        JSONObject values = new JSONObject();
        try {

            values.put(QlearnKeys.VOCAB_EXTENSION_PREV_SIZE, previousSize);
            values.put(QlearnKeys.VOCAB_EXTENSION_NEW_SIZE, newSize);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.QLEARN_VOCAB_EXTENSION_TRIGGERED);
            json.put(QlearnKeys.SENSOR_VALUE, values);

        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging QLearn status to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);

    }

    public static void onBatteryDrainage(Double prevBatteryPct, Double currentBatteryPct) {

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onBatteryDrainage( prevBatteryPct: "
                    + prevBatteryPct
                    + ", currentBatteryPct: "
                    + currentBatteryPct + " )");
        }

        JSONObject json = prepareJSONSnapshot();
        JSONObject values = new JSONObject();
        try {

            values.put(QlearnKeys.BATTERY_PREV_LEVEL, prevBatteryPct);
            values.put(QlearnKeys.BATTERY_PREV_CURRENT_LEVEL, currentBatteryPct);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.BATTERY_PERCENTAGE);
            json.put(QlearnKeys.SENSOR_VALUE, values);

        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging QLearn status to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);
    }

    public static void onUpdateDataUsage(long logImpulse, String bytesReceived, String bytesTransmitted) {

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onUpdateDataUsage( logImpulse: "
                    + logImpulse
                    + ", bytesReceived: "
                    + bytesReceived
                    + ", bytesTransmitted: "
                    + bytesTransmitted + " )");
        }

        JSONObject json = prepareJSONSnapshot();
        JSONObject values = new JSONObject();
        try {

            values.put(QlearnKeys.FEATURE_LOG_IMPULSE_IN_MS, logImpulse);
            values.put(QlearnKeys.FEATURE_BYTES_RECEIVED, bytesReceived);
            values.put(QlearnKeys.FEATURE_BYTES_TRANSMITTED, bytesTransmitted);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.FEATURE_DATA_USAGE);
            json.put(QlearnKeys.SENSOR_VALUE, values);

        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging QLearn status to json object");
            }
            e.printStackTrace();
        }

        logSensorSnapshot(json);

    }
}
