package com.tilmanification.quicklearn.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.tilmanification.quicklearn.log.QLearnJsonLog;

class FeatureSensorBattery extends FeatureSensor {

    // ========================================================================
    // Fields
    // ========================================================================

    protected static final String TAG = FeatureSensorBattery.class
            .getSimpleName();
    private double batteryPct;
    private BatteryDrainReceiver batteryDrainReceiver;

    // ========================================================================
    // Receiver
    // ========================================================================

    class BatteryDrainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            double pct = getBatteryPct();
            if (pct != batteryPct) {
                batteryPct = pct;
//                features.onBatteryChanged(batteryPct);
            }
        }

    }

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeatureSensorBattery(Context context, Features features) {
        super(context, features);

    }

    public void onStart() {
        this.batteryDrainReceiver = new BatteryDrainReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.context.registerReceiver(this.batteryDrainReceiver, filter);
        this.batteryPct = getBatteryPct();
    }

    public void onStop() {
        this.context.unregisterReceiver(batteryDrainReceiver);
    }

    /** Called when screen is turned on */
    public void onScreenOn() {
        this.batteryPct = getBatteryPct();
    }

    /** Called when screen is turned off */
    public void onScreenOff() {
        Double prevBatteryPct = this.batteryPct;
        Double currentBatteryPct = getBatteryPct();
        QLearnJsonLog.onBatteryDrainage(prevBatteryPct, currentBatteryPct);
    }

    // ========================================================================
    // Method
    // ========================================================================

    private double getBatteryPct() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        Double currentBatteryPct = level / (double) scale;
        return currentBatteryPct;
    }

}
