package com.tilmanification.quicklearn;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;
import com.tilmanification.quicklearn.log.UuidGen;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tilmanification.quicklearn.QuickLearnPrefs.DEBUG_MODE;
import static com.tilmanification.quicklearn.QuickLearnPrefs.LANGUAGE_SWITCH_ENABLED;
import static com.tilmanification.quicklearn.QuickLearnPrefs.PREF_CONSENT_DATA_SHARING;
import static com.tilmanification.quicklearn.StudyManager.CONDITION_FLASHCARD;
import static com.tilmanification.quicklearn.StudyManager.CONDITION_MULTIPLE_CHOICE;
import static com.tilmanification.quicklearn.Util.getDateFromTimestamp;
import static com.tilmanification.quicklearn.log.QlearnKeys.QLEARN_DATE_LAST_LOG_UPLOAD;

public class SettingsActivity extends Fragment {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String	TAG	= SettingsActivity.class.getSimpleName();

    // ========================================================================
    // Class Variables
    // ========================================================================

    private View statisticsView;
    private SharedPreferences prefs;
    private StudyManager studyManager;

    private Spinner languageSelectionSource;
    private Spinner languageSelectionTarget;
    private Spinner learningModeSelection;

    private TextView deviceUid;
    private TextView lastLogUpload;
    private Button btnTriggerNotification;
    private CheckBox checkBoxDataConsent;

    private TextView appVersion;

    // ========================================================================
    // Methods
    // ========================================================================


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        statisticsView = inflater.inflate(R.layout.activity_settings, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        studyManager = StudyManager.getInstance(getActivity());

        deviceUid = (TextView) statisticsView.findViewById(R.id.text_settings_device_uid);
        deviceUid.setText(String.format(getActivity().getString(R.string.settings_device_uid), UuidGen.getPid6(getActivity())));

        lastLogUpload = (TextView) statisticsView.findViewById(R.id.text_settings_last_log_upload);
        Date dateLastLogDate = getDateFromTimestamp(Util.getLong(getActivity(), QLEARN_DATE_LAST_LOG_UPLOAD, 0));
        SimpleDateFormat dt = new SimpleDateFormat(QuickLearnPrefs.DATE_FORMAT);
        String lastLogDate = dt.format(dateLastLogDate);
        lastLogUpload.setText(String.format(getActivity().getString(R.string.settings_last_log_upload), lastLogDate));

        appVersion = (TextView) statisticsView.findViewById(R.id.text_settings_app_version);
        appVersion.setText(String.format(getActivity().getString(R.string.settings_app_version), BuildConfig.VERSION_NAME, Integer.toString(BuildConfig.VERSION_CODE)));

        btnTriggerNotification = (Button) statisticsView.findViewById(R.id.button_trigger_notification);

        btnTriggerNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationTriggerService.triggerTestNotification();
            }
        });

        checkBoxDataConsent = (CheckBox) statisticsView.findViewById(R.id.checkbox_data_consent);
        checkBoxDataConsent.setChecked(Util.getBool(getActivity(), PREF_CONSENT_DATA_SHARING, false));

        checkBoxDataConsent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {

                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setIcon(R.drawable.quicklearnnotificon);
                    alertDialog.setTitle(R.string.settings_data_consent_warning_title);
                    alertDialog.setMessage(getResources().getString(R.string.settings_data_consent_warning_desc));
                    alertDialog.setCancelable(true);
                    alertDialog.setButton(
                            AlertDialog.BUTTON_NEGATIVE,
                            getResources().getString(R.string.settings_data_consent_quit),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if(QuickLearnPrefs.DEBUG_MODE) {
                                        Log.i(TAG, "participant stopped data sharing");
                                    }
                                    Util.put(getActivity(), PREF_CONSENT_DATA_SHARING, false);
                                    QLearnJsonLog.logSensorSnapshot(QlearnKeys.QLEARN_DATA_SHARING_ENABLED, Boolean.toString(false));
                                    checkBoxDataConsent.setChecked(false);
                                }
                            });
                    alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            getResources().getString(R.string.settings_data_consent_cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if(QuickLearnPrefs.DEBUG_MODE) {
                                        Log.i(TAG, "participant keeps data sharing");
                                    }
                                    Util.put(getActivity(), PREF_CONSENT_DATA_SHARING, true);
                                    QLearnJsonLog.logSensorSnapshot(QlearnKeys.QLEARN_DATA_SHARING_ENABLED, Boolean.toString(true));
                                    checkBoxDataConsent.setChecked(true);
                                }
                            });
                    alertDialog.show();

                } else {
                    Util.put(getActivity(), PREF_CONSENT_DATA_SHARING, true);
                    Toast.makeText(getActivity(), getResources().getString(R.string.settings_data_consent_confirm), Toast.LENGTH_SHORT).show();
                }
            }
        });

        LanguageDictionary languageDictionary = new LanguageDictionary(getActivity());
        String[] languages = new String[languageDictionary.languages.length + 1];
        languages[0] = "-";
        for (int i = 0; i < languageDictionary.languages.length; i++) {
            languages[i + 1] = languageDictionary.languages[i];
        }

        // LANGUAGE SELECTIOn
        languageSelectionSource = (Spinner) statisticsView.findViewById(R.id.spinner_language_source);
        languageSelectionTarget = (Spinner) statisticsView.findViewById(R.id.spinner_language_target);

        ArrayAdapter<String> adapterLanguageSource = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, languages);
        ArrayAdapter<String> adapterLanguageTarget = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, languages);

        languageSelectionSource.setAdapter(adapterLanguageSource);
        languageSelectionTarget.setAdapter(adapterLanguageTarget);

        String sourceLanguage = prefs.getString(QuickLearnPrefs.PREF_LANG_SOURCE, "");
        languageSelectionSource.setSelection(getIndex(languageSelectionSource, sourceLanguage));

        String targetLanguage = prefs.getString(QuickLearnPrefs.PREF_LANG_TARGET, "");
        languageSelectionTarget.setSelection(getIndex(languageSelectionTarget, targetLanguage));

        //don't allow language change
        languageSelectionSource.setEnabled(LANGUAGE_SWITCH_ENABLED);
        languageSelectionTarget.setEnabled(LANGUAGE_SWITCH_ENABLED);

        //TODO: warn that changes will reset leitener indices
        languageSelectionSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        languageSelectionTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        // LEARNING MODE
        learningModeSelection = (Spinner) statisticsView.findViewById(R.id.spinner_learning_mode);

        String[] learningModes = new String[2];
        learningModes[CONDITION_FLASHCARD] = getResources().getString(R.string.learning_mode_fc);
        learningModes[CONDITION_MULTIPLE_CHOICE] = getResources().getString(R.string.learning_mode_mc);

        ArrayAdapter<String> adapterLearningMOdes= new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, learningModes);
        learningModeSelection.setAdapter(adapterLearningMOdes);
        learningModeSelection.setSelection(prefs.getInt(QuickLearnPrefs.PREF_STUDY_CONDITION, 0));

        learningModeSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                studyManager.updateStudyCondition(i);

                if(DEBUG_MODE) {
                    Log.i(TAG, "learning mode switched to: " + Integer.toString(i));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        return statisticsView;
    }

    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }
}
