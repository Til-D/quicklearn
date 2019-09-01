package com.tilmanification.quicklearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;
import java.util.Locale;

import static com.tilmanification.quicklearn.QuickLearnPrefs.PREF_CONSENT_DATA_SHARING;

public class ConsentActivity extends Activity {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String	TAG	= ConsentActivity.class.getSimpleName();
    private SharedPreferences	prefs;
    private Button				acceptButton;

    // ========================================================================
    // Methods
    // ========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        acceptButton = (Button) findViewById(R.id.con_btn_give_consent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onStart()");
        }

//        updateAcceptButton();

        if (MainActivity.finishing || Util.getBool(this, QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "setup finished");
            }
            finish();

        } else if (Util.getBool(this, MainActivity.PREF_CONSENT_GIVEN, false)) {
            startNextActivity();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            MainActivity.finishing = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startNextActivity() {
//        final Intent intent = new Intent(this, EnableAccessibilityActivity.class);
        final Intent intent = new Intent(this, EnableNotificationActivity.class);
        startActivity(intent);
        finish();
    }

    public void onGiveConsent(final View view) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "onGiveConsent()");
            }
        }
        Util.put(getApplicationContext(), MainActivity.PREF_CONSENT_GIVEN, true);
        Util.put(getApplicationContext(), PREF_CONSENT_DATA_SHARING, true);
//        MainActivity.consentShown = true;
        startNextActivity();
    }

    public void onDenyConsent(final View view) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "onDenyConsent()");
            }
        }
//        MainActivity.finishing = true;
//        MainActivity.consentShown = true;
        finishAffinity();
    }

    private void showRegistrationFailedDialog() {
        Resources res = getResources();

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(R.drawable.quicklearnnotificon);
        alertDialog.setTitle(R.string.error);
        alertDialog.setMessage(res.getString(R.string.con_could_not_register));
        alertDialog.setCancelable(false);
        alertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL,
                res.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(QuickLearnPrefs.DEBUG_MODE) {
                            Log.i(TAG, "Dialog confirmed");
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void updateAcceptButton() {
        final int dbId = prefs.getInt(QuickLearnPrefs.PREF_DATABASE_ID, 0);
        final boolean enabled = dbId > 0;
        acceptButton.setEnabled(enabled);
    }

    private static String getTimezone() {
        return "" + Calendar.getInstance().get(Calendar.ZONE_OFFSET)
                / (60 * 60 * 1000);
    }

    private static String getLocale() {
        return "" + Locale.getDefault().toString();
    }
}
