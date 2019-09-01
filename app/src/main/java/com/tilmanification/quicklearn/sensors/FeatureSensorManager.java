package com.tilmanification.quicklearn.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class FeatureSensorManager {

    private static final String	TAG			= FeatureSensorManager.class.getSimpleName();

    public static String		launcher	= "";

    private Context				context;
    private List<FeatureSensor>	sensors;
    private ScreenReceiver		screenReceiver;

    public FeatureSensorManager(Context context, Features features) {
        this.context = context;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context
                .getPackageManager()
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        launcher = resolveInfo.activityInfo.packageName;
        Log.i(TAG, "launcher package: " + launcher);

//        Features.packageMapping = loadPackageMapping();
        Features.launcher = launcher;

        initSensors(features);
    }

    private void initSensors(Features features) {
        if (sensors == null) {

            sensors = new ArrayList<FeatureSensor>();
            sensors.add(new FeatureSensorScreen(context, features));

//            sensors.add(new FeatureSensorApp(context, features));
            sensors.add(new FeatureSensorBattery(context, features));
            sensors.add(new FeatureSensorCharging(context, features));
            sensors.add(new FeatureSensorDataUsage(context, features));
//			sensors.add(new FeatureSensorLight(context, features));                 > drains too much battery
//            sensors.add(new FeatureSensorNotificationDrawer(context, features));  > requires accessibility service
            sensors.add(new FeatureSensorPhone(context, features));
//			sensors.add(new FeatureSensorProximity(context, features));             > drains too much battery
            sensors.add(new FeaturePhoneSensorRinger(context, features));
            sensors.add(new FeatureSensorScreenOrientation(context, features));
//            sensors.add(new FeatureSensorSemanticLocation(context, features));    > requires extra permission
//            sensors.add(new FeatureSensorSms(context, features));                 > crashes app
        }
    }

    public void onStart() {
        String sensorName = null;
        try {
            Log.i(TAG, "starting " + sensors.size() + " sensors");
            for (FeatureSensor sensor : sensors) {
                sensorName = sensor.getClass().getSimpleName();
                sensor.onStart();
            }

            this.screenReceiver = new ScreenReceiver();
            IntentFilter screenReceiverFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            screenReceiverFilter.addAction(Intent.ACTION_SCREEN_OFF);
            screenReceiverFilter.addAction(Intent.ACTION_USER_PRESENT);
            this.context.registerReceiver(screenReceiver, screenReceiverFilter);

        } catch (Exception e) {
            Log.e(TAG, e + " in onStart() for sensor " + sensorName, e);
        }
    }

    public void onStop() {
        Log.i(TAG, "stopping " + sensors.size() + " sensors");
        try {
            this.context.unregisterReceiver(screenReceiver);
        } catch (Exception e) {
            Log.w(TAG, "onStop(): screenReceiver was not registered");
        }
        for (FeatureSensor sensor : sensors) {
            sensor.onStop();
        }
    }

    private class ScreenReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.i(TAG, "screen on");
                onScreenOn();

            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                Log.i(TAG, "screen unlocked");
                onScreenUnlocked();

            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.i(TAG, "screen off");
                onScreenOff();

            }
        }
    }

    private void onScreenOn() {
        for (FeatureSensor sensor : sensors) {
            sensor.onScreenOn();
        }
    }

    private void onScreenUnlocked() {
        for (FeatureSensor sensor : sensors) {
            sensor.onScreenUnlocked();
        }

    }

    private void onScreenOff() {
        for (FeatureSensor sensor : sensors) {
            sensor.onScreenOff();
        }
    }

//    private Map<String, String> loadPackageMapping() {
//        Map<String, String> packageMapping = new HashMap<String, String>();
//
//        try {
//            String path = Util.getFilePath(context, "packages_categories_es.txt");
//            BufferedReader reader = new BufferedReader(new FileReader(path));
//            String line = null;
//
//            while ((line = reader.readLine()) != null) {
//                String[] elems = line.split(";");
//                String key = elems[0];
//                String value = flattenToAscii(elems[1]);
//
//                if (Features.CAT_COMMUNICATION.equals(value)
//                        || Features.CAT_PRODUCTIVITY.equals(value)
//                        || Features.CAT_SOCIETY.equals(value)) {
//                    packageMapping.put(key, value);
//                    Log.v(TAG, key + ": " + value);
//                }
//            }
//
//            reader.close();
//
//            Log.i(TAG, "read " + packageMapping.size() + " package mappings from " + path);
//
//        } catch (IOException e) {
//            Log.e(TAG, e + " when loading package categories", e);
//        }
//
//        return packageMapping;
//    }

    private static String flattenToAscii(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }
        return sb.toString();
    }

}
