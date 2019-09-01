package com.tilmanification.quicklearn.sensors;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.tilmanification.quicklearn.QuickLearnPrefs;
import com.tilmanification.quicklearn.log.QLearnJsonLog;

public class SensorService extends Service implements QuickLearnPrefs {

    // TODO make sure this service is always running (add startService to everything)

    private static final String TAG = SensorService.class.getSimpleName();

    private static SensorService instance;

    private BroadcastReceiver screenOnReceiver;

    private FeatureSensorManager							featureSensorManager;
    public  Features										features;

    public SensorService() {
        instance = this;
    }

    public static SensorService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onBind()");
        }
        return null;
    }

    @Override
    public void onDestroy() {

        this.featureSensorManager.onStop();

        unregisterReceiver(screenOnReceiver);

        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onCreate()");
        }

        this.featureSensorManager = new FeatureSensorManager(this, features);
        this.featureSensorManager.onStart();

        QLearnJsonLog.startLogger(getApplicationContext());
    }

}