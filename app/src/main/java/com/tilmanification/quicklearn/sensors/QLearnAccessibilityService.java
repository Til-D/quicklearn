package com.tilmanification.quicklearn.sensors;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.List;

public class QLearnAccessibilityService extends AccessibilityService {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String					TAG								= QLearnAccessibilityService.class
            .getSimpleName();

    private static final Object					LISTENER_MUTEX					= new Object();

    // ========================================================================
    // Fields
    // ========================================================================

    public static QLearnAccessibilityService	instance;

    private List<QLearnAccessibilityServiceListener>	accessibilitySensors			= new ArrayList<QLearnAccessibilityServiceListener>();

    // ========================================================================
    // Methods
    // ========================================================================

    // ------------------------------------------------------------------------
    // Listener Management
    // ------------------------------------------------------------------------

    public void register(QLearnAccessibilityServiceListener sensor) {
        synchronized (LISTENER_MUTEX) {
            accessibilitySensors.add(sensor);
        }
    }

    public void unregister(QLearnAccessibilityServiceListener sensor) {
        synchronized (LISTENER_MUTEX) {
            accessibilitySensors.remove(sensor);
        }
    }

    private void notifySensors(AccessibilityEvent ae) {
        synchronized (LISTENER_MUTEX) {
            for (QLearnAccessibilityServiceListener sensor : accessibilitySensors) {
                sensor.onAccessibilityEvent(ae);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of 'AccessibilityService'
    // ------------------------------------------------------------------------

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "onServiceConnected()");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);

        instance = this;

        //BoredomPredictionService.startService(this);

    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt()");

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent ae) {
        notifySensors(ae);
    }
}
