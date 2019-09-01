package com.tilmanification.quicklearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class EnableNetworkLocationActivity extends Activity {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String	TAG	= EnableNetworkLocationActivity.class.getSimpleName();

    // ========================================================================
    // Method
    // ========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_network_location);
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

        boolean networkEnabled = Util.isNetworkLocationEnabled(this);

        if (MainActivity.finishing || Util.getBool(this, QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {
            finish();

        } else if (networkEnabled) {

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "network location service enabled - going to next activity");
            }

            final Activity activity = this;

            Resources res = getResources();

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setIcon(R.drawable.quicklearnnotificon);
            alertDialog.setTitle(R.string.success);
            alertDialog.setMessage(res.getString(R.string.nl_success));
            alertDialog.setCancelable(false);
            alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL,
                    res.getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(QuickLearnPrefs.DEBUG_MODE) {
                                Log.i(TAG, "Dialog confirmed");
                            }
                            final Intent intent = new Intent(activity, EnterEmailActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
            alertDialog.show();

        }
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

    public void onOpenLocationSettings(final View view) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onOpenLocationSettings()");
        }

        Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(viewIntent);

    }

}
