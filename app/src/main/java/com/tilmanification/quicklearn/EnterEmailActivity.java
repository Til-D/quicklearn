package com.tilmanification.quicklearn;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import static com.tilmanification.quicklearn.QuickLearnPrefs.AGE_SPECIFICATION_MANDATORY;
import static com.tilmanification.quicklearn.QuickLearnPrefs.GENDER_SPECIFICATION_MANDATORY;
import static com.tilmanification.quicklearn.log.QlearnKeys.QLEARN_DATE_SETUP_COMPLETED;

//import org.pielot.borpred.BoredomPredictionService;

public class EnterEmailActivity extends Activity {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String			TAG					= EnterEmailActivity.class.getSimpleName();

    private SharedPreferences			prefs;
    boolean								demographicsVisible	= false;

    private EditText					emailText;
    private EditText					ageText;

    private Spinner						genderSelectionSpinner;
    private ArrayAdapter<CharSequence>	genderSelectionAdapter;

    private Spinner						usageSelectionSpinner;
    private ArrayAdapter<CharSequence>	usageSelectionAdapter;

    private Spinner                     languageSelectionSource;
    private Spinner                     languageSelectionTarget;
    private Spinner                     languageProficiency;

    public int							genderPos;
    public int                          usagePos;

    // ========================================================================
    // Method
    // ========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_email);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onCreate()");
        }
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.demographicsVisible = prefs.getBoolean(QuickLearnPrefs.PREF_ASK_DEMOGRAPHICS, false);

        this.emailText = (EditText) findViewById(R.id.em_user_email);
        this.ageText = (EditText) findViewById(R.id.em_age);

        genderSelectionSpinner = (Spinner) findViewById(R.id.em_gender);
        genderSelectionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item);
        genderSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSelectionSpinner.setAdapter(genderSelectionAdapter);
        genderSelectionSpinner.setOnItemSelectedListener(new GenderSelectionListener() {
        });

        usageSelectionSpinner = (Spinner) findViewById(R.id.em_usage);
        usageSelectionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.usage_array,
                android.R.layout.simple_spinner_item);
        usageSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usageSelectionSpinner.setAdapter(usageSelectionAdapter);
        usageSelectionSpinner.setOnItemSelectedListener(new UsageSelectionListener() {
        });

        languageSelectionSource = (Spinner) findViewById(R.id.spinner_language_source);
        languageSelectionTarget = (Spinner) findViewById(R.id.spinner_language_target);
        languageProficiency     = (Spinner) findViewById(R.id.spinner_language_proficiency);

        LanguageDictionary languageDictionary = new LanguageDictionary(this);

        String[] languages = new String[languageDictionary.languages.length + 1];
        languages[0] = "-";
        for (int i = 0; i < languageDictionary.languages.length; i++) {
            languages[i + 1] = languageDictionary.languages[i];
        }

        ArrayAdapter<String> adapterLanguageSource = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        ArrayAdapter<String> adapterLanguageTarget = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, languages);

        languageSelectionSource.setAdapter(adapterLanguageSource);
        languageSelectionTarget.setAdapter(adapterLanguageTarget);

        languageProficiency.setAdapter(ArrayAdapter.createFromResource(
                this,
                R.array.language_proficiency,
                android.R.layout.simple_spinner_dropdown_item
        ));

        languageSelectionSource.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // View ageLayout = findViewById(R.id.em_age_layout);
        // ageLayout.setVisibility(demographicsVisible ? View.VISIBLE : View.GONE);
        // View genderLayout = findViewById(R.id.em_gender_layout);
        // genderLayout.setVisibility(demographicsVisible ? View.VISIBLE : View.GONE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onStart()");
            Log.i(TAG, "BorappMainActivity.finishing == " + MainActivity.finishing);
        }

        if (MainActivity.finishing || Util.getBool(this, QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {
            finish();

        } else {

            int genderPos = prefs.getInt(QuickLearnPrefs.PREF_GENDER_POS, 0);
            genderSelectionSpinner.setSelection(genderPos);

            int usagePos = prefs.getInt(QuickLearnPrefs.PREF_USAGE_POS, 0);
            usageSelectionSpinner.setSelection(usagePos);
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

    public void onDonePressed(final View view) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onDonePressed()");
        }

        int age = getAge();

        if(AGE_SPECIFICATION_MANDATORY && age == 0) {
            showErrorDialog(R.string.em_error_message_5);
            ageText.requestFocus();
            return;
        }

        if(GENDER_SPECIFICATION_MANDATORY && genderSelectionSpinner.getSelectedItemPosition() == 0) {
            showErrorDialog(R.string.em_error_message_6);
            genderSelectionSpinner.requestFocus();
            return;
        }

        if(languageSelectionSource.getSelectedItemPosition() == 0) {
            showErrorDialog(R.string.em_error_message_1);
            languageSelectionSource.requestFocus();
            return;
        }

        if(languageSelectionTarget.getSelectedItemPosition() == 0) {
            showErrorDialog(R.string.em_error_message_2);
            languageSelectionTarget.requestFocus();
            return;
        }

        if(languageProficiency.getSelectedItemPosition() == 0) {
            showErrorDialog(R.string.em_error_message_3);
            languageProficiency.requestFocus();
            return;
        }

        if(languageSelectionSource.getSelectedItemId() == languageSelectionTarget.getSelectedItemId()) {
            showErrorDialog(R.string.em_error_message_4);
            return;
        }

        String email = emailText.getText().toString();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(QuickLearnPrefs.PREF_EMAIL, email);
        editor.putInt(QuickLearnPrefs.PREF_AGE, age);
        editor.putInt(QuickLearnPrefs.PREF_GENDER_POS, genderPos);
        editor.putString(QuickLearnPrefs.PREF_GENDER, Util.getGenderStr(genderPos));
        editor.putInt(QuickLearnPrefs.PREF_USAGE_POS, usagePos);
        editor.putString(QuickLearnPrefs.PREF_USAGE, Util.getUsageStr(usagePos));
        editor.putBoolean(QuickLearnPrefs.PREF_SETUP_FINISHED, true);
        editor.putLong(QLEARN_DATE_SETUP_COMPLETED, System.currentTimeMillis());
        editor.putString(QuickLearnPrefs.PREF_LANG_SOURCE, (String) languageSelectionSource.getSelectedItem());
        editor.putString(QuickLearnPrefs.PREF_LANG_TARGET, (String) languageSelectionTarget.getSelectedItem());
        editor.putString(QuickLearnPrefs.PREF_LANG_PROFICIENCY, (String) languageProficiency.getSelectedItem());
        editor.putLong(QuickLearnPrefs.PREF_SETUP_FINISHED_TIMESTAMP, System.currentTimeMillis());
        editor.putInt(QuickLearnPrefs.CURRENT_VERSION_CODE, BuildConfig.VERSION_CODE);
        editor.commit();

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "email is " + email);
            Log.i(TAG, "source lang is " + languageSelectionSource.getSelectedItem());
            Log.i(TAG, "target lang is " + languageSelectionTarget.getSelectedItem());
            Log.i(TAG, "proficiency lang is " + languageProficiency.getSelectedItem());
        }

        Toast.makeText(EnterEmailActivity.this, R.string.setup_completed, Toast.LENGTH_LONG).show();

        finish();
    }

    private int getAge() {
        String age = ageText.getText().toString();
        try {
            return Integer.parseInt(age);
        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, e + "while parsing: '" + age + "'");
            }
            return 0;
        }
    }

    class GenderSelectionListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            genderPos = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    class UsageSelectionListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            usagePos = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    private void showErrorDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.em_error_title);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.em_error_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

}
