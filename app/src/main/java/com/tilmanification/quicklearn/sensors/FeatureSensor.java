package com.tilmanification.quicklearn.sensors;

import android.content.Context;

abstract class FeatureSensor {

    protected Context	context;
    protected Features	features;

    protected FeatureSensor(Context context, Features features) {
        this.context = context;
        this.features = features;
        // Log.i(this.getClass().getSimpleName(), "()");
    }

    /** Called to start the sensor */
    public abstract void onStart();

    /** Called to stop the sensor */
    public abstract void onStop();

    /** Called when screen is turned on */
    public void onScreenOn() {}

    /** Called when screen is unlocked*/
    public void onScreenUnlocked() {}

    /** Called when screen is turned off */
    public void onScreenOff() {}

}
