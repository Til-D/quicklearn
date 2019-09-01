package com.tilmanification.quicklearn.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

class FeatureSensorCharging extends FeatureSensor {

    // ========================================================================
    // Fields
    // ========================================================================

    public final String				TAG	= FeatureSensorCharging.class.getSimpleName();
    private PowerConnectionReceiver	powerConnectionReceiver;
    private int						charging;

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeatureSensorCharging(Context context, Features features) {
        super(context, features);
    }

    public void onStart() {
        this.powerConnectionReceiver = new PowerConnectionReceiver();

        IntentFilter powerConnectionReceiverFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        powerConnectionReceiverFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        context.registerReceiver(powerConnectionReceiver, powerConnectionReceiverFilter);

        updateBatteryStatus();
    }

    public void onStop() {
        context.unregisterReceiver(this.powerConnectionReceiver);
    }

    // ========================================================================
    // Method
    // ========================================================================

    private void updateBatteryStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        boolean plugged = usbCharge || acCharge;

        int charging = plugged ? 1 : 0;

        Log.d(TAG, "plugged: "+plugged+", charging: "+charging);

        if (this.charging != charging) {
            this.charging = charging;
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.CHARGING_CHANGED, Integer.toString(charging));
        }

    }

    // battery status
    class PowerConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryStatus();
        }
    }
}