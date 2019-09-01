package com.tilmanification.quicklearn.sensors;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

class FeatureSensorSms extends FeatureSensor  {

    // ========================================================================
    // Fields
    // ========================================================================



    public final String			TAG				= FeatureSensorSms.class.getSimpleName();
    private static final String	SMS_SENT		= "sent";
    private static final String	SMS_RECEIVED	= "received";
    private static final String	SMS_READ	    = "read";

    private static Handler		handle			= new Handler() {};
    private ContentResolver		contentResolver;
    private ContentObserver		contentObserver;
    private Sms					sms;

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeatureSensorSms(Context context, Features features) {
        super(context, features);
    }

    public void onStart() {
        contentObserver = new ContentObserver(handle) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                onSmsEvent(context);
            }
        };
        contentResolver = context.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, contentObserver);
    }

    public void onStop() {
        contentResolver.unregisterContentObserver(contentObserver);
    }

    // ========================================================================
    // Method
    // ========================================================================

    private void onSmsEvent(Context context) {

        synchronized (TAG) {

            Sms sms = Sms.parseFrom(context);

            // first SMS --> sent or received
            if (this.sms == null) {
                Log.d(TAG, "first SMS --> sent or received");
                this.sms = sms;
                onSmsSentOrReceived(sms);

                // new SMS ID --> sent or received
            } else if (this.sms.id != sms.id) {
                Log.d(TAG, "new SMS ID --> sent or received");
                this.sms = sms;
                onSmsSentOrReceived(sms);

                // read state changed --> probably read received SMS
            } else {
                Log.d(TAG, "Other SMS event --> will be ignored");
//                features.onSmsRead();
                //careful, this might not only be read
                QLearnJsonLog.logSensorSnapshot(QlearnKeys.SMS_EVENT, SMS_READ);
            }
        }
    }

    private void onSmsSentOrReceived(Sms sms) {

        if (SMS_RECEIVED.equals(sms.event)) {
//            features.onSmsReceived();
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.SMS_EVENT, SMS_RECEIVED);
        } else if (SMS_SENT.equals(sms.event)) {
//            features.onSmsSent();
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.SMS_EVENT, SMS_SENT);
        }
    }
}
