package com.tilmanification.quicklearn;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;
import com.tilmanification.quicklearn.log.ServerComm;
import com.tilmanification.quicklearn.log.UuidGen;
import com.tilmanification.quicklearn.sensors.SensorService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.tilmanification.quicklearn.R.menu.main;
import static com.tilmanification.quicklearn.log.QLearnJsonLog.logSensorSnapshot;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, QuickLearnPrefs {

    // ========================================================================
    // Constants
    // ========================================================================

    private final String				TAG		= 	MainActivity.class.getSimpleName();


    // ========================================================================
    // Fields
    // ========================================================================

    public static boolean				finishing		= false;

    private android.app.FragmentManager fragmentManager = getFragmentManager();
    private StudyManager studyManager;
    private boolean surveyTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    protected void onStart() {
        super.onStart();

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onStart()");
        }

//        if (finishing) {
//            if(QuickLearnPrefs.DEBUG_MODE) {
//                Log.i(TAG, "Finishing");
//            }
//            finishing = false;
//            finish();
        if (!Util.getBool(getApplicationContext(), PREF_SETUP_FINISHED, false)) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "Setup not yet finished");
            }
            final Intent intent = new Intent(this, ConsentActivity.class);
            startActivity(intent);
//            finish();
        } else {
            this.studyManager = StudyManager.getInstance(this);

            //update user data on server
            if ((!Util.getBool(this, QuickLearnPrefs.PREF_USER_REGISTERED, false)
                    && Util.getBool(this, PREF_CONSENT_GIVEN, false)
                    && Util.getBool(this, PREF_SETUP_FINISHED, false))) {

                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "+ registering user on server");
                }

                String src_language = Util.getString(this, QuickLearnPrefs.PREF_LANG_SOURCE, "unknown");
                String target_language = Util.getString(this, QuickLearnPrefs.PREF_LANG_TARGET, "unknown");
                String proficiency = Util.getString(this, QuickLearnPrefs.PREF_LANG_PROFICIENCY, "unknown");

                String email = Util.getString(getApplicationContext(), QuickLearnPrefs.PREF_EMAIL, "");
                int age = Util.getInt(getApplicationContext(), QuickLearnPrefs.PREF_AGE, 0);
                String gender = Util.getString(getApplicationContext(), QuickLearnPrefs.PREF_GENDER, "unknown");
                String phoneUsage = Util.getString(getApplicationContext(), QuickLearnPrefs.PREF_USAGE, "unknown");
                String uid = UuidGen.getPid6(this);
                String locale = Util.getLocale();
                String timezone = Util.getTimezone();
                String os = android.os.Build.VERSION.RELEASE;
                String device = android.os.Build.MANUFACTURER + "-" + android.os.Build.MODEL;
                int sdk = android.os.Build.VERSION.SDK_INT;
                int version = BuildConfig.VERSION_CODE;
                String simcardInfo = Util.getSimcardInfo(this);
                boolean accessibility_enabled = Util.isAccessibilityEnabled(getApplicationContext());
                boolean notification_access = EnableNotificationActivity.isNotificationAccessEnabled();
                boolean location_access = Util.isNetworkLocationEnabled(getApplicationContext());

                Util.putInt(getApplicationContext(), QuickLearnPrefs.CURRENT_VERSION_CODE, version);

                // get list of installed apps
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                JSONObject installedApps = new JSONObject();
                try {
                    JSONObject values = new JSONObject();

                    for(ApplicationInfo app : apps) {
                        values.put(app.packageName, app.enabled);
                    }

                    installedApps.put(QlearnKeys.SENSOR_ID, QlearnKeys.INSTALLED_APPS);
                    installedApps.put(QlearnKeys.SENSOR_VALUE, values);
                } catch (JSONException e) {
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.e(TAG, "ERROR accessing installed apps");
                    }
                    e.printStackTrace();
                }

                ServerComm.sendUserRegistrationToServerAsync(getApplicationContext(), uid, os, sdk, device, locale, timezone, version, simcardInfo, installedApps.toString(), accessibility_enabled, notification_access, location_access, email, age, gender, phoneUsage, src_language, target_language, proficiency);

            } else {

                QLearnJsonLog.startLogger(getApplicationContext());
                ServerComm.tryInformServerSetupCompleted(getApplicationContext());

                //check whether update has been made
                int version = Util.getInt(getApplicationContext(), QuickLearnPrefs.CURRENT_VERSION_CODE, 0);
                if(version!=BuildConfig.VERSION_CODE) {

                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.i(TAG, "New Version: update user");
                    }
                    logSensorSnapshot(QlearnKeys.APP_UPDATE, Integer.toString(BuildConfig.VERSION_CODE));
                    ServerComm.sendUserUpdate(getApplicationContext(), BuildConfig.VERSION_CODE);
                }

            }

            //start background services
            startService(new Intent(this, NotificationTriggerService.class));
            startService(new Intent(this, SensorService.class));

            Fragment quizScreen;
            if (studyManager.current_condition == StudyManager.CONDITION_FLASHCARD) {
                quizScreen = new FlashcardActivity();
            } else {
                quizScreen = new MultipleChoiceActivity();
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame,
                            quizScreen)
                    .commit();

            // counting the number of times the app is opened
            int app_opened_count = Util.getInt(this, QuickLearnPrefs.PREF_NUMBER_OF_TIMES_APP_OPENED, 0);

            if(app_opened_count>0) { //avoid triggering survey on first launch
                //trigger survey
                if (!surveyTriggered && app_opened_count % StudyManager.SURVEY_TRIGGERED_PER_APP_STARTS == 0) {
                    if (QuickLearnPrefs.DEBUG_MODE) {
                        Log.i(TAG, "triggering survey");
                    }
                    surveyTriggered = true;
                    startActivity(new Intent(getApplicationContext(), SurveyActivity.class));
                } else {
                    if (QuickLearnPrefs.DEBUG_MODE) {
                        Log.i(TAG, "app launch counter: " + app_opened_count);
                    }
                    surveyTriggered = false;
                    Util.putInt(this, QuickLearnPrefs.PREF_NUMBER_OF_TIMES_APP_OPENED, ++app_opened_count);
                }
            } else {
                Util.putInt(this, QuickLearnPrefs.PREF_NUMBER_OF_TIMES_APP_OPENED, ++app_opened_count);
            }

            //check if vocabulary should be extended, and if so:
            Date lastVocabExtension = Util.getDateFromTimestamp(Util.getLong(getApplicationContext(), QlearnKeys.QLEARN_DATE_LAST_VOCAB_EXTENSION, System.currentTimeMillis()));
            SimpleDateFormat dt = new SimpleDateFormat(QuickLearnPrefs.DATE_FORMAT);
            String lastVocabExtensionDate = dt.format(lastVocabExtension);
            long days_passed = Util.getDaysPassedSince(lastVocabExtension);

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "test for vocab extension: last extension: " + lastVocabExtensionDate + ", i.e., days passed: " + days_passed + ", extension interval: "  + StudyManager.DAY_INTERVAL_FOR_VOCAB_EXTENSION);
            }

            if(days_passed >= StudyManager.DAY_INTERVAL_FOR_VOCAB_EXTENSION) {
                studyManager.vocabulary.extendWordlist();
            }

        }

        if(QuickLearnPrefs.DEBUG_MODE) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            Log.d(TAG, "***** QuickLearn onStart() ******");
            Log.d(TAG, "participant age: "    + prefs.getInt(QuickLearnPrefs.PREF_AGE, 0));
            Log.d(TAG, "participant gender: " + prefs.getString(QuickLearnPrefs.PREF_GENDER, "not defined") + "(" + prefs.getInt(QuickLearnPrefs.PREF_GENDER_POS, 3) + ")");
            Log.d(TAG, "participant email: "  + prefs.getString(QuickLearnPrefs.PREF_EMAIL, "not available"));
            Log.d(TAG, "current version: " + Util.getInt(getApplicationContext(), QuickLearnPrefs.CURRENT_VERSION_CODE, 0));

            Log.d(TAG, "*********************************");
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if(QuickLearnPrefs.DEBUG_MODE) {
//            Log.i(TAG, "onResume()");
//        }
//    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_learn) {

            if(DEBUG_MODE) {
                Log.i(TAG, "nav item clicked: review vocabulary");
            }

            Fragment quizScreen;
            if (studyManager.current_condition == StudyManager.CONDITION_FLASHCARD) {
                quizScreen = new FlashcardActivity();
            } else {
                quizScreen = new MultipleChoiceActivity();
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame,
                            quizScreen)
                    .commit();

        } else if (id == R.id.nav_settings) {

            if(DEBUG_MODE) {
                Log.i(TAG, "nav item clicked: settings");
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame,
                            new SettingsActivity())
                    .commit();

        } else if (id == R.id.nav_statistics) {

            if(DEBUG_MODE) {
                Log.i(TAG, "nav item clicked: statistics");
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame,
                            new StatisticsActivity())
                    .commit();

//        } else if (id == R.id.nav_setup) {
//
//            if(DEBUG_MODE) {
//                Log.i(TAG, "nav item clicked: setup");
//            }
//
//            consentShown = false;
//            if (!Util.getBool(getApplicationContext(), PREF_SETUP_FINISHED, false)) {
//                if (QuickLearnPrefs.DEBUG_MODE) {
//                    Log.i(TAG, "Setup not yet finished");
//                }
//                Toast.makeText(MainActivity.this, "launching consent form", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(getApplicationContext(), ConsentActivity.class);
////                finish();
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            } else {
//                Toast.makeText(MainActivity.this, R.string.setup_completed, Toast.LENGTH_LONG).show();
//            }

        } else if (id == R.id.nav_about) {

            if(DEBUG_MODE) {
                Log.i(TAG, "nav item clicked: about");
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame,
                            new AboutUsActivity())
                    .commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * method for sdks >= 23 where permissions are checked on the fly
     */
    public static void checkAppPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {
//            if (checkPermission())
//            {
//                // Code for above or equal 23 API Oriented Device
//                // Your Permission granted already .Do next code
//            } else {
//                requestPermission(); // Code for permission
//            }
        } else {

            // Code for Below 23 API Oriented Device
            // Do next code
        }
    }
}
