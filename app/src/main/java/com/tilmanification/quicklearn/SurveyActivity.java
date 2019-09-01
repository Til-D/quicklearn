package com.tilmanification.quicklearn;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.tilmanification.quicklearn.log.QLearnJsonLog;

//import org.hcilab.projects.log.QLearnJsonLog;

public class SurveyActivity extends Activity {

    private final String TAG = SurveyActivity.class.getSimpleName();

    private Spinner socialContextSelectionSpinner;
    private Spinner locationSelectionSpinner;
    private Spinner timingOpportuneSelectionSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_survey);

        Button buttonSubmit             = (Button) findViewById(R.id.survey_send);

        // social context
        socialContextSelectionSpinner  = (Spinner) findViewById(R.id.survey_social_context);
        ArrayAdapter<CharSequence> socialContextSelectionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.survey_social_context,
                android.R.layout.simple_spinner_item);
        socialContextSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialContextSelectionSpinner.setAdapter(socialContextSelectionAdapter);

        // location
        locationSelectionSpinner  = (Spinner) findViewById(R.id.survey_location);
        ArrayAdapter<CharSequence> locationSelectionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.survey_location,
                android.R.layout.simple_spinner_item);
        locationSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSelectionSpinner.setAdapter(locationSelectionAdapter);

        // timing opportune?
        timingOpportuneSelectionSpinner  = (Spinner) findViewById(R.id.survey_timing_opportune);
        ArrayAdapter<CharSequence> timingOpportuneSelectionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.survey_timing_opportune,
                android.R.layout.simple_spinner_item);
        timingOpportuneSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timingOpportuneSelectionSpinner.setAdapter(timingOpportuneSelectionAdapter);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String socialContext = (String) socialContextSelectionSpinner.getSelectedItem();
                String location = (String) locationSelectionSpinner.getSelectedItem();
                String timingOpportune = (String) timingOpportuneSelectionSpinner.getSelectedItem();

                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "social context: " + socialContext);
                    Log.i(TAG, "location: " + location);
                    Log.i(TAG, "timing oppotune: " + timingOpportune);
                }

                if(socialContextSelectionSpinner.getSelectedItemPosition() == 0) {
                    showErrorDialog(R.string.survey_error_message_1);
                    return;
                }

                if(locationSelectionSpinner.getSelectedItemPosition() == 0) {
                    showErrorDialog(R.string.survey_error_message_2);
                    return;
                }

                if(timingOpportuneSelectionSpinner.getSelectedItemPosition() == 0) {
                    showErrorDialog(R.string.survey_error_message_3);
                    return;
                }

                //increase survey_counts
                int survey_count = Util.getInt(getApplicationContext(), QuickLearnPrefs.PREF_SURVEY_COUNT, 0);
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.e(TAG, "increase survey_count: " + survey_count);
                }
//                Util.putInt(getApplicationContext(), QuickLearnPrefs.PREF_SURVEY_ATTEMPT, 0);
                Util.putInt(getApplicationContext(), QuickLearnPrefs.PREF_SURVEY_COUNT, ++survey_count);

                //update last survey date
                Util.putLong(getApplicationContext(), QuickLearnPrefs.PREF_DATE_LAST_SURVEY_MS, System.currentTimeMillis());

                int condition = StudyManager.getInstance(getApplicationContext()).current_condition;
                QLearnJsonLog.onSurveyFilledIn(socialContext, location, timingOpportune, condition);

                finish();

                Toast.makeText(SurveyActivity.this, R.string.survey_thanks, Toast.LENGTH_SHORT).show();
            }
        });
        QLearnJsonLog.onESMTriggered();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "back button pressed > survey dismissed");
//                int app_opened_count = Util.getInt(this, QuickLearnPrefs.PREF_NUMBER_OF_TIMES_APP_OPENED, 0);
//                Util.putInt(this, QuickLearnPrefs.PREF_NUMBER_OF_TIMES_APP_OPENED, --app_opened_count );
            }
            // log dismissal
            QLearnJsonLog.onESMDismissed();
        }
        return super.onKeyUp(keyCode, event);
    }

    private void showErrorDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.survey_error_title);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.survey_error_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

}