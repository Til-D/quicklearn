package com.tilmanification.quicklearn.sensors;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

class FeatureSensorPhone extends FeatureSensor {

    // ========================================================================
    // Fields
    // ========================================================================

    public final String			TAG				= FeatureSensorPhone.class.getSimpleName();

    private TelephonyManager	telephonyManager;
    private PhoneCallReceiver	phoneCallReceiver;

    private boolean				call			= false;
    private boolean				incomingCall	= false;
    private boolean				callPicked		= false;

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeatureSensorPhone(Context context, Features features) {
        super(context, features);
    }

    public void onStart() {
        this.phoneCallReceiver = new PhoneCallReceiver();
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.telephonyManager.listen(this.phoneCallReceiver, PhoneStateListener.LISTEN_CALL_STATE);

    }

    public void onStop() {
        this.telephonyManager.listen(this.phoneCallReceiver, PhoneStateListener.LISTEN_NONE);
    }

    // ========================================================================
    // Method
    // ========================================================================

    private class PhoneCallReceiver extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            onPhoneCallStateChanged(state, incomingNumber);
        }
    }

    public void onPhoneCallStateChanged(int state, String incomingNumber) {

        //Log.e(TAG, "phone call state: " + state + ", number: " + incomingNumber);

        if (TelephonyManager.CALL_STATE_RINGING == state) {
            call = true;
//            if (incomingNumber.length() > 0) {
                incomingCall = true;
//            }
        }

        if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
            call = true;
            if (incomingCall) {
                callPicked = true;
            }
        }

        if (TelephonyManager.CALL_STATE_IDLE == state) {
            if (!call) return;
            onCallEnded();
        }
    }

    protected void onCallEnded() {
        if (incomingCall) {
            Log.d(TAG, "The last phone call was incoming, it was " + (callPicked ? "successful" : "not successful"));
//            features.onIncomingCall();
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.PHONE_CALL_INCOMING, Boolean.toString(callPicked));
        } else {
            Log.d(TAG, "The last phone call was outgoing");
//            features.onOutgoingCall();
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.PHONE_CALL_OUTGOING, Boolean.toString(true));
        }


        call = false;
        incomingCall = false;
        callPicked = false;
    }

}
