package com.tilmanification.quicklearn.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tilmanification.quicklearn.QuickLearnPrefs;
import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Map;

public class Features {

    // ========================================================================
    // Constants
    // ========================================================================

    public static final String MOST_USED_APP_NONE = "None";

    private static final String TAG = Features.class.getSimpleName();

    public static final double TIME_WINDOW_MIN = 5;
    public static final long TIME_WINDOW_MS = (long) (1000 * 60 * TIME_WINDOW_MIN);

    public static final String SEM_LOC_UNKNOWN = "Unknown";
    public static final String SEM_LOC_HOME = "home";
    public static final String SEM_LOC_WORK = "work";
    public static final String SEM_LOC_OTHER = "other";

    public static final String CAT_COMMUNICATION = "Comunicacion";
    public static final String CAT_PRODUCTIVITY = "Productividad";
    public static final String CAT_SOCIETY = "Sociedad";
    public static final String CAT_OTHER = "Other";

    private static final String APP_OTHER = "Other";
    private static final String PACKAGE_LAUNCHER = APP_OTHER;

    public static String RINGER_MODE_SILENT = "Slnt";
    public static String RINGER_MODE_VIBRATE = "Vbrt";
    public static String RINGER_MODE_NORMAL = "Nrml";

    // ========================================================================
    // Fields Package filtering
    // ========================================================================

    private static final String[] WHITELIST_NOTIFICATIONS = {
            "com.google.android.gm", "com.whatsapp", "com.android.settings",
            "com.android.phone", "com.facebook.orca", "com.android.email",
            "com.android.mms", "com.android.vending", "org.telegram.messenger",
            "com.facebook.katana", };

    private static final String[] APP_IN_FOCUS_WHITELIST = {
            "com.android.settings", "com.whatsapp", "com.google.android.gm",
            "com.facebook.katana", "com.facebook.orca", "com.android.chrome",
            "com.android.email", "com.android.mms", "com.android.browser",
            "com.instagram.android", };

    private static final String[] MOST_USED_APP_WHITELIST = {
            "com.android.phone", "com.whatsapp", "com.google.android.gm",
            "com.facebook.katana", "com.facebook.orca", "com.android.chrome",
            "com.android.email", "com.android.mms", "com.android.browser",
            "com.instagram.android", };

    static Map<String, String> packageMapping;

    static String launcher;

    // ========================================================================
    // Features from CHI '15 paper
    // ========================================================================

    // public int age; // age a integer
    // public int gender; // 0=female, 1=male
    // public int isAtHome; // Boolean 0 or 1
    // public int isAtWork; // Boolean 0 or 1
    // public String mostUsedAppCategory = "Comunicacion";
    // public int proximity; // Boolean 0 or 1
    // public int charging; // Boolean 0 or 1
    // public int screenOrientChanges; // Boolean 0 or 1
    // public double timeSinceLastNotif; // time in seconds
    // public double timeSinceLastSmsReceived; // time in seconds
    // public double timeSinceLastSmsSent; // time in seconds
    // public double timeSinceLastIncomingCall; // time in seconds
    // public double timeSinceLastOutgoingCall; // time in seconds

    // ========================================================================
    // Features for UbiComp '15
    // ========================================================================

    public int age;
    public String app_category_in_focus = CAT_OTHER;
    public String app_in_focus = APP_OTHER;
    public double apps_per_min;;
    public double battery_drain;
    public double battery_level;
    public double bytes_received; // kb
    public double bytes_transmitted; // kb
    // Boolean 0 or 1
    public int charging;
    // 0=Mon,6=Sun
    public int day_of_week;
    // 0 = female, 1 = male
    public int gender;
    // 24-hour format
    public int hour_of_day;
    public String last_notif = APP_OTHER;
    public String last_notif_category = CAT_OTHER;
    public double light;
    public String most_used_app = APP_OTHER;
    public String most_used_app_category = CAT_OTHER;
    public int num_apps;
    public int num_notifs;
    public int num_unlocks;
    public String prev_app_in_focus = APP_OTHER;
    // Boolean 0 or 1
    public int proneness;
    public int proximity;
    public String ringer_mode;
    public int screen_orientation_changes;
    public String semantic_location = SEM_LOC_UNKNOWN;
    public double time_last_SMS_read; // min
    public double time_last_SMS_received; // min
    public double time_last_SMS_sent; // min
    public double time_last_incoming_phonecall; // min
    public long time_last_notif; // sec
    public long time_last_notification_center_access; // sec
    public String time_spent_in_communication_apps = "None";
    public double time_last_outgoing_phonecall; // min
    public double time_last_unlock; // min
    // first decimal of age, e.g. 35 --> 3
    public int cat_age = 3;

    // ========================================================================
    // Support variables
    // ========================================================================

    private static int DAY_MS = 1000 * 60 * 60 * 24;
    private static long START_TIME = System.currentTimeMillis() - DAY_MS;
    private long timeLastNotif = START_TIME;
    private long timeLastNotifCenterAccess = START_TIME;
    private long timeLastSmsRead = START_TIME;
    private long timeLastSmsReceived = START_TIME;
    private long timeLastSmsSent = START_TIME;
    private long timeLastIncomingCall = START_TIME;
    private long timeLastOutgoingCall = START_TIME;
    private long timeLastUnlock = START_TIME;

    private TimeWindowedEvents appEvents = new TimeWindowedEvents();
    private TimeWindowedEvents appCatEvents = new TimeWindowedEvents();
    private TimeWindowedEvents battEvents = new TimeWindowedEvents();
    private TimeWindowedEvents notifEvents = new TimeWindowedEvents();
    private TimeWindowedEvents unlockEvents = new TimeWindowedEvents();
    private TimeWindowedEvents screenOrientEvents = new TimeWindowedEvents();

    // ========================================================================
    // Constructor
    // ========================================================================

    public Features() {
    }

    public Features(Features features) {

        // TODO important: keep this constructor complete!!

        this.age = features.age;
        this.app_category_in_focus = features.app_category_in_focus;
        this.app_in_focus = features.app_in_focus;
        this.apps_per_min = features.apps_per_min;
        this.battery_drain = features.battery_drain;
        this.battery_level = features.battery_level;
        this.bytes_received = features.bytes_received;
        this.bytes_transmitted = features.bytes_transmitted;
        this.charging = features.charging;
        this.day_of_week = features.day_of_week;
        this.gender = features.gender;
        this.hour_of_day = features.hour_of_day;
        this.last_notif = features.last_notif;
        this.last_notif_category = features.last_notif_category;
        this.light = features.light;
        this.most_used_app = features.most_used_app;
        this.most_used_app_category = features.most_used_app_category;
        this.num_apps = features.num_apps;
        this.num_notifs = features.num_notifs;
        this.num_unlocks = features.num_unlocks;
        this.prev_app_in_focus = features.prev_app_in_focus;
        this.proneness = features.proneness;
        this.proximity = features.proximity;
        this.ringer_mode = features.ringer_mode;
        this.screen_orientation_changes = features.screen_orientation_changes;
        this.semantic_location = features.semantic_location;
        this.time_last_SMS_read = features.time_last_SMS_read;
        this.time_last_SMS_received = features.time_last_SMS_received;
        this.time_last_SMS_sent = features.time_last_SMS_sent;
        this.time_last_incoming_phonecall = features.time_last_incoming_phonecall;
        this.time_last_notif = features.time_last_notif;
        this.time_last_notification_center_access = features.time_last_notification_center_access;
        this.time_last_outgoing_phonecall = features.time_last_outgoing_phonecall;
        this.time_last_unlock = features.time_last_unlock;
        this.time_spent_in_communication_apps = features.time_spent_in_communication_apps;
        this.cat_age = features.cat_age;

        this.timeLastNotif = features.timeLastNotif;
        this.timeLastNotifCenterAccess = features.timeLastNotifCenterAccess;
        this.time_last_SMS_read = features.time_last_SMS_read;
        this.timeLastSmsReceived = features.timeLastSmsReceived;
        this.timeLastSmsSent = features.timeLastSmsSent;
        this.timeLastIncomingCall = features.timeLastIncomingCall;
        this.timeLastOutgoingCall = features.timeLastOutgoingCall;
        this.timeLastUnlock = features.timeLastUnlock;

        this.appEvents = new TimeWindowedEvents(features.appEvents);
        this.appCatEvents = new TimeWindowedEvents(features.appCatEvents);
        this.battEvents = new TimeWindowedEvents(features.battEvents);
        this.notifEvents = new TimeWindowedEvents(features.notifEvents);
        this.notifEvents = new TimeWindowedEvents(features.notifEvents);
        this.screenOrientEvents = new TimeWindowedEvents(
                features.screenOrientEvents);

    }

    // ========================================================================
    // Feature-related events
    // ========================================================================

    public void onBatteryChanged(double batteryPct) {
        this.battery_drain = batteryPct;
        this.battEvents.addEvent("" + batteryPct);
        this.battEvents.endAllOngoingEvents();
        TimeWindowedEvent e = battEvents.getLastEvent();
        Log.i(TAG, "battery: " + (100 * batteryPct) + "% -- # events: "
                + battEvents.getNumEventsInTimeWindow() + ", event: " + e);
    }

    // @attribute app_category_in_focus
    // {Other,Comunicacion,Sociedad,Productividad}
    // @attribute app_in_focus
    // @attribute prev_app_in_focus
    // {Other,com.android.settings,com.whatsapp,com.google.android.gm,com.facebook.katana,com.facebook.orca,com.android.chrome,com.android.email,com.android.mms,com.android.browser,com.instagram.android}
    public void onForegroundAppChanged(String packageName, boolean isLauncher) {

        String filteredCategory = filterCategory(packageName);
        String filteredPackage = filterAppInFocus(packageName);

        if (isLauncher) {
            Log.i(TAG, "onForegroundAppChanged( " + packageName
                    + " )  --> launcher");
            filteredCategory = CAT_OTHER;
            filteredPackage = PACKAGE_LAUNCHER;

            this.appEvents.endAllOngoingEvents();
            this.appCatEvents.endAllOngoingEvents();

        } else {
            Log.i(TAG, "onForegroundAppChanged( " + packageName + " ) --> "
                    + filteredPackage + ", category: " + filteredCategory);
            this.appEvents.addEvent(packageName);
            this.appCatEvents.addEvent(filteredCategory);
        }

        this.prev_app_in_focus = app_in_focus;
        this.app_category_in_focus = filteredCategory;
        this.app_in_focus = filteredPackage;
    }

    // @attribute bytes_received numeric
    // @attribute bytes_transmitted numeric
    public void onDataUsageUpdate(long received, long transmitted) {
        this.bytes_received = (received / 1000.0);
        this.bytes_transmitted = (transmitted / 1000.0);
        Log.i(TAG, "bytes_received: " + bytes_received
                + ", bytes_transmitted: " + bytes_transmitted);

    }

    // @attribute charging numeric
    /** Boolean 0 or 1 */
    public void onChargingChanged(int charging) {
        Log.i(TAG, "charging: " + charging);
        this.charging = charging;
    }

    public void onLightChanged(int light) {
        Log.i(TAG, "light: " + light);
        this.light = light;
    }

    // @attribute last_notif
    // {com.google.android.gm,Other,com.whatsapp,com.android.settings,com.android.phone,com.facebook.orca,com.android.email,com.android.mms,None,com.android.vending,org.telegram.messenger,com.facebook.katana}
    // @attribute last_notif_category
    // {Comunicacion,Other,Productividad,Sociedad}
    // @attribute time_last_notif numeric
    public void onNotificationPosted(String packageName) {
        String filteredCategory = filterCategory(packageName);
        String filteredPackageName = filterNotification(packageName);
        Log.i(TAG, "notification posted: " + packageName + " --> "
                + filteredPackageName + ", category: " + filteredCategory);

        this.last_notif = filteredPackageName;
        this.last_notif_category = filteredCategory;
        this.timeLastNotif = System.currentTimeMillis();
        this.notifEvents.addEvent(last_notif);

        updateTimeRelatedVariables();

    }

    public void onNumAppsChanges(int numApps) {
        Log.i(TAG, "num_apps: " + numApps);
        this.num_apps = numApps;
    }

    // @attribute proximity numeric
    /** Boolean 0 or 1 */
    public void onProximityChanged(int proximity) {
        Log.i(TAG, "proximity: " + proximity);
        this.proximity = proximity;
    }

    public void onRingerModeChanged(String ringerMode) {
        Log.i(TAG, "ringer mode: " + ringerMode);
        this.ringer_mode = ringerMode;
    }

    public void onScreenUnlocked() {
        Log.i(TAG, "screen unlocked - num_unlocks: " + num_unlocks);
        this.unlockEvents.addEvent("unlock");
        this.timeLastUnlock = System.currentTimeMillis();

//		logSnapshot();
    }

    public void onScreenOff() {
        Log.i(TAG, "screen off");
        this.unlockEvents.endAllOngoingEvents();
    }

    // @attribute screen_orientation_changes numeric
    public void onScreenOrientationChanged(String orientation) {
        Log.i(TAG, "screen orientation changed to " + orientation);
        screenOrientEvents.addEvent(orientation);
    }

    // @attribute semantic_location {home,other,work,Unknown}
    /** home, work, other, or Unknown */
    public void onSemanticLocationChanged(String semanticLocation) {
        Log.i(TAG, "semantic location changed: " + semanticLocation);
        this.semantic_location = semanticLocation;
    }

    // @attribute time_last_SMS_read numeric
    public void onSmsRead() {
        Log.i(TAG, "SMS onSmsRead");
        timeLastSmsRead = System.currentTimeMillis();
        updateTimeRelatedVariables();
    }

    // @attribute time_last_SMS_received numeric
    public void onSmsReceived() {
        Log.i(TAG, "SMS received");
        timeLastSmsReceived = System.currentTimeMillis();
        updateTimeRelatedVariables();
    }

    // @attribute time_last_SMS_sent numeric
    public void onSmsSent() {
        Log.i(TAG, "SMS sent");
        timeLastSmsSent = System.currentTimeMillis();
        updateTimeRelatedVariables();
    }

    // @attribute time_last_incoming_phonecall numeric
    public void onIncomingCall() {
        Log.i(TAG, "call incoming");
        timeLastIncomingCall = System.currentTimeMillis();
        updateTimeRelatedVariables();
    }

    // @attribute time_last_notification_center_access numeric
    public void onNotificationDrawerAccessed() {
        Log.i(TAG, "notification center accessed");
        this.timeLastNotifCenterAccess = System.currentTimeMillis();
    }

    // @attribute time_last_outgoing_phonecall numeric
    public void onOutgoingCall() {
        Log.i(TAG, "call outgoing");
        timeLastOutgoingCall = System.currentTimeMillis();
        updateTimeRelatedVariables();
    }

    // TODO @attribute time_spent_in_communication_apps {None,Micro,Full}

    // @attribute cat_age numeric
    // @attribute gender numeric
    public void updateDemographics(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        age = prefs.getInt(QuickLearnPrefs.PREF_AGE, 0);
        cat_age = age / 10;
        gender = prefs.getInt(QuickLearnPrefs.PREF_GENDER_POS, 0);

        Log.i(TAG, "cat_age: " + cat_age + ", gender: " + gender);

    }

    // ========================================================================
    // Update
    // ========================================================================

    // @attribute day_of_week numeric
    // @attribute hour_of_day numeric
    public void updateTimeRelatedVariables() {

        final long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);

        // day needs to be corrected, because Java returns 2 for Monday, whereas
        // Python returns 0
        day_of_week = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        hour_of_day = c.get(Calendar.HOUR_OF_DAY);

        appEvents.removeOldEvents();
        appCatEvents.removeOldEvents();
        battEvents.removeOldEvents();
        notifEvents.removeOldEvents();
        unlockEvents.removeOldEvents();
        screenOrientEvents.removeOldEvents();

        apps_per_min = appEvents.getNumEventsInTimeWindow() / TIME_WINDOW_MIN;

        battery_level = computeBatteryLevel();

        most_used_app = filterMostUsedApp(appEvents
                .mostCommonEventInTimeWindow(APP_OTHER));
        most_used_app_category = appCatEvents
                .mostCommonEventInTimeWindow(CAT_OTHER);
        num_notifs = notifEvents.getNumEventsInTimeWindow();
        num_unlocks = unlockEvents.getNumEventsInTimeWindow();
        screen_orientation_changes = Math.min(
                screenOrientEvents.getNumEventsInTimeWindow(), 1); // use 0 and
        // 1

        Log.d(TAG, "apps_per_min: " + apps_per_min + ", most_used_app: "
                + most_used_app + ", most_used_app_category: "
                + most_used_app_category + ", num_notifs: " + num_notifs
                + ", num_unlocks: " + num_unlocks
                + ", screen_orientation_changes: " + screen_orientation_changes

        );

        // Log.e(TAG, "num_unlocks: " + num_unlocks);

        time_last_notif = updateSec(timeLastNotif, now);
        time_last_notification_center_access = updateSec(
                timeLastNotifCenterAccess, now);
        time_last_SMS_read = updateMin(timeLastSmsRead, now);
        time_last_SMS_received = updateMin(timeLastSmsReceived, now);
        time_last_SMS_sent = updateMin(timeLastSmsSent, now);
        time_last_incoming_phonecall = updateMin(timeLastIncomingCall, now);
        time_last_outgoing_phonecall = updateMin(timeLastOutgoingCall, now);
        time_last_unlock = updateMin(timeLastUnlock, now);

        Log.d(TAG, "time (min) since last notif: " + time_last_notif
                + ", last notif-center access: "
                + time_last_notification_center_access + ", unlock: "
                + time_last_unlock + ", time (sec) SMS read: "
                + time_last_SMS_read + ", SMS received: "
                + time_last_SMS_received + ", SMS sent: " + time_last_SMS_sent
                + ", incoming call: " + time_last_incoming_phonecall
                + ", outgoing call: " + time_last_outgoing_phonecall

        );
    }

    private double computeBatteryLevel() {

        int eventCount = battEvents.getNumEventsInTimeWindow();
        TimeWindowedEvent oldestBatteryEvent = battEvents.getFirstEvent();
        TimeWindowedEvent latestBatteryEvent = battEvents.getLastEvent();

        if (oldestBatteryEvent == null || latestBatteryEvent == null) {
            Log.e(TAG, "computeBatteryLevel() - not enough events yet "
                    + oldestBatteryEvent + ", " + latestBatteryEvent);
            return 0;
        }

        double oldestBatteryLevel = Double.parseDouble(oldestBatteryEvent.name);
        double latestBatteryLevel = Double.parseDouble(latestBatteryEvent.name);
        double batteryLevelDiff = latestBatteryLevel - oldestBatteryLevel;
        long timeDiffMs = latestBatteryEvent.started
                - oldestBatteryEvent.started;
        double timeDiffMin = timeDiffMs / 60000.0;

        double batteryLevelChangePerMin = 0;
        if (timeDiffMs > 0) {
            batteryLevelChangePerMin = 60000.0 * batteryLevelDiff / timeDiffMs;
        }

        Log.i(TAG, "battery levels - events: " + eventCount + ", oldest: "
                + oldestBatteryLevel + ", latest: " + latestBatteryLevel
                + ", diff: " + batteryLevelDiff + " | time diff (ms): "
                + timeDiffMs + ", time diff (min): " + timeDiffMin
                + " | battery change per minute: " + batteryLevelChangePerMin);

        return batteryLevelChangePerMin;
    }

    public double updateMin(long timeWhenEventOccurred, long now) {
        return Math.abs(now - timeWhenEventOccurred) / 60000.0;
    }

    public long updateSec(long timeWhenEventOccurred, long now) {
        return Math.abs(now - timeWhenEventOccurred) / 1000;
    }

    @SuppressWarnings("unused")
//    public void populate(Instance instance) {
//
//        int i = 0;
//        try {
//
//            updateTimeRelatedVariables();
//
//            @SuppressWarnings("unchecked")
//            Enumeration<Attribute> attributes = instance.enumerateAttributes();
//            while (attributes.hasMoreElements()) {
//                Attribute nextElement = attributes.nextElement();
//                // Log.e(TAG, "" + nextElement);
//            }
//
//            // Features selected after cleaning with Jose
//            // instance.setValue(i++, age);
//            setSafe(instance, i++, app_category_in_focus);
//            setSafe(instance, i++, app_in_focus);
//            // TODO add feature apps_per_min: setSafe(instance, i++,
//            // apps_per_min);
//            // instance.setMissing(i++); // instance.setValue(i++,
//            // battery_drain);
//            // instance.setMissing(i++); // instance.setValue(i++,
//            // battery_level);
//            setSafe(instance, i++, bytes_received);
//            setSafe(instance, i++, bytes_transmitted);
//            setSafe(instance, i++, charging);
//            setSafe(instance, i++, day_of_week);
//            setSafe(instance, i++, gender);
//            setSafe(instance, i++, hour_of_day);
//            setSafe(instance, i++, last_notif);
//            setSafe(instance, i++, last_notif_category);
//            // TODO add feature light: setSafe(instance, i++, light);
//            setSafe(instance, i++, most_used_app);
//            setSafe(instance, i++, most_used_app_category);
//            setSafe(instance, i++, num_apps);
//            setSafe(instance, i++, num_notifs);
//            setSafe(instance, i++, num_unlocks);
//            // Log.i(TAG,
//            // "prev_app_in_focus: "+prev_app_in_focus+", (has crashed a few times)");
//            // instance.setMissing(i++); // instance.setValue(i++,
//            // prev_app_in_focus); // crashes because label lists
//            // are
//            // different
//            // instance.setMissing(i++); // instance.setValue(i++, proneness);
//            setSafe(instance, i++, proximity);
//            setSafe(instance, i++, ringer_mode);
//            setSafe(instance, i++, screen_orientation_changes);
//            setSafe(instance, i++, semantic_location);
//
//            setSafe(instance, i++, time_last_SMS_read);
//            setSafe(instance, i++, time_last_SMS_received);
//            setSafe(instance, i++, time_last_SMS_sent);
//            setSafe(instance, i++, time_last_incoming_phonecall);
//            setSafe(instance, i++, time_last_notif);
//            setSafe(instance, i++, time_last_notification_center_access);
//            setSafe(instance, i++, time_last_outgoing_phonecall);
//            // TODO add feature time_last_unlock: setSafe(instance, i++,
//            // time_last_unlock);
//
//            // instance.setMissing(i++); // instance.setValue(i++,
//            // time_spent_in_communication_apps);
//            setSafe(instance, i++, cat_age);
//
//        } catch (Exception e) {
//            Log.e(TAG, e + " in populate(Instance)", e);
//        }
//
//        Log.d(TAG, "Num features: " + i + ", Instance: " + instance);
//    }

//    private void setSafe(Instance instance, int i, String value) {
//        try {
//            instance.setValue(i, value);
//        } catch (Exception e) {
//            Log.e(TAG, e + " when setting " + instance.attribute(i)
//                    + " to string " + value);
//            instance.setMissing(i);
//        }
//    }
//
//    private void setSafe(Instance instance, int i, double value) {
//        try {
//            instance.setValue(i, value);
//        } catch (Exception e) {
//            Log.e(TAG, e + " when setting " + instance.attribute(i)
//                    + " to double " + value);
//            instance.setMissing(i);
//        }
//    }
//
//    private void setSafe(Instance instance, int i, int value) {
//        try {
//            instance.setValue(i, value);
//        } catch (Exception e) {
//            Log.e(TAG, e + " when setting " + instance.attribute(i)
//                    + " to int " + value);
//            instance.setMissing(i);
//        }
//    }

    // ========================================================================
    // Package filtering
    // ========================================================================

    private String filterCategory(String packageName) {
        if (packageMapping.containsKey(packageName)) {
            return packageMapping.get(packageName);
        }
        return CAT_OTHER;
    }

    private String filterNotification(String packageName) {
        for (String p : WHITELIST_NOTIFICATIONS) {
            if (p.equals(packageName)) {
                return p;
            }
        }
        return APP_OTHER;
    }

    private String filterAppInFocus(String packageName) {
        for (String p : APP_IN_FOCUS_WHITELIST) {
            if (p.equals(packageName)) {
                return p;
            }
        }
        return APP_OTHER;
    }

    private String filterMostUsedApp(String packageName) {
        for (String p : MOST_USED_APP_WHITELIST) {
            if (p.equals(packageName)) {
                return p;
            }
        }
        return APP_OTHER;
    }

    /**
     *  takes a snapshot of all variables and sends them to the log
     */
    public void logSnapshot() {


        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(QlearnKeys.FEATURE_APP_IN_FOCUS, "" + app_in_focus);
            values.put(QlearnKeys.FEATURE_APP_CAT_IN_FOCUS, "" + app_category_in_focus);
            values.put(QlearnKeys.FEATURE_BYTES_RECEIVED, "" + bytes_received);
            values.put(QlearnKeys.FEATURE_BYTES_TRANSMITTED, "" + bytes_transmitted);

            values.put(QlearnKeys.FEATURE_CHARGING, "" + charging);
            values.put(QlearnKeys.FEATURE_DAY_OF_WEEK, "" + day_of_week);
            values.put(QlearnKeys.FEATURE_HOUR_OF_DAY, "" + hour_of_day);
            values.put(QlearnKeys.FEATURE_LAST_NOTIF, "" + last_notif);
            values.put(QlearnKeys.FEATURE_LAST_NOTIF_CAT, "" + last_notif_category);
            values.put(QlearnKeys.FEATURE_MOST_USED_APP, "" + most_used_app);
            values.put(QlearnKeys.FEATURE_MOST_USED_APP_CAT, "" + most_used_app_category);
            values.put(QlearnKeys.FEATURE_NUM_APPS, "" + num_apps);
            values.put(QlearnKeys.FEATURE_NUM_NOTIFS, "" + num_notifs);
            values.put(QlearnKeys.FEATURE_NUM_UNLOCKS, "" + num_unlocks);
            values.put(QlearnKeys.FEATURE_PROXIMITY, "" + proximity);
            values.put(QlearnKeys.FEATURE_RINGER_MODE, "" + ringer_mode);
            values.put(QlearnKeys.FEATURE_SCREEN_ORIENTATION_CHANGES, "" + screen_orientation_changes);
            values.put(QlearnKeys.FEATURE_SEMANTIC_LOCATION, "" + semantic_location);
            values.put(QlearnKeys.FEATURE_TIME_LAST_SMS_READ, "" + time_last_SMS_read);
            values.put(QlearnKeys.FEATURE_TIME_LAST_SMS_SENT, "" + time_last_SMS_sent);
            values.put(QlearnKeys.FEATURE_TIME_LAST_SMS_RECEIVED, "" + time_last_SMS_received);
            values.put(QlearnKeys.FEATURE_TIME_LAST_INCOMING_PHONECALL, "" + time_last_incoming_phonecall);
            values.put(QlearnKeys.FEATURE_TIME_LAST_NOTIF, "" + time_last_notif);
            values.put(QlearnKeys.FEATURE_TIME_LAST_NOTIF_CENTER_ACCESS, "" + time_last_notification_center_access);
            values.put(QlearnKeys.FEATURE_TIME_LAST_OUTGOING_PHONECALL, "" + time_last_outgoing_phonecall);
            values.put(QlearnKeys.FEATURE_TIME_SPENT_IN_COMMUNICATION_APPS, "" + time_spent_in_communication_apps);

            json.put(QlearnKeys.SENSOR_ID, QlearnKeys.FEATURES_SNAPSHOT);
            json.put(QlearnKeys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            Log.e(TAG, "ERROR session finished log data to json object");
            e.printStackTrace();
        }

        QLearnJsonLog.logSensorSnapshot(json);

    }

}
