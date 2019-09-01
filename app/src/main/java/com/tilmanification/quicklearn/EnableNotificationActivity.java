package com.tilmanification.quicklearn;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.tilmanification.quicklearn.sensors.QLearnNotificationListenerService;

//import org.pielot.borpred.BoredomNotificationListenerService;

public class EnableNotificationActivity extends Activity {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String	TAG	= EnableNotificationActivity.class.getSimpleName();

    // ========================================================================
    // Method
    // ========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_notification);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onCreate()");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onResume()");
        }

        if (MainActivity.finishing || Util.getBool(this, QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {
            finish();
        } else if (Build.VERSION.SDK_INT < 18) {
            final Intent intent;
            if(QuickLearnPrefs.NETWORKLOCATIONENFORCED) {
                intent = new Intent(this, EnableNetworkLocationActivity.class);
            } else {
                intent = new Intent(this, EnterEmailActivity.class);
            }
            startActivity(intent);

        } else if (isNotificationAccessEnabled()) {

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification service enabled - going to next activity");
            }

            final Activity activity = this;

            Resources res = getResources();

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setIcon(R.drawable.quicklearnnotificon);
            alertDialog.setTitle(R.string.success);
            alertDialog.setMessage(res.getString(R.string.not_success));
            alertDialog.setCancelable(false);
            alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL,
                    res.getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(QuickLearnPrefs.DEBUG_MODE) {
                                Log.i(TAG, "Dialog confirmed");
                            }
                            final Intent intent;
                            if(QuickLearnPrefs.NETWORKLOCATIONENFORCED) {
                                intent = new Intent(activity, EnableNetworkLocationActivity.class);
                            } else {
                                intent = new Intent(activity, EnterEmailActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        }
                    });
            alertDialog.show();
        }

    }

    @TargetApi(18)
    public static boolean isNotificationAccessEnabled() {
        return QLearnNotificationListenerService.notificationAccessEnabled;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "back button pressed");
            }
            MainActivity.finishing = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onOpenNotificationSettings(final View view) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onOpenNotificationSettings()");
        }

        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivityForResult(intent, 0);
    }

}
