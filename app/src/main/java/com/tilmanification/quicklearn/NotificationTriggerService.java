package com.tilmanification.quicklearn;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.tilmanification.quicklearn.log.LogUtil;
import com.tilmanification.quicklearn.log.QLearnJsonLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//import org.pielot.borpred.BoredomListener;
//import org.pielot.borpred.BoredomPrediction;
//import org.pielot.borpred.BoredomPredictionService;

//public class NotificationTriggerService extends Service implements BoredomListener, QuickLearnPrefs {
public class NotificationTriggerService extends Service implements QuickLearnPrefs {

    // TODO make sure this service is always running (add startService to everything)

    private static final String TAG = NotificationTriggerService.class.getSimpleName();

    private static final int NUMBER_OF_MULTIPLE_CHOICES = 3;
    private static final boolean DISABLE_NOTIFICATIONS = false;

    private StudyManager studyManager;
    private String language;
    public Word current_word;
    private static NotificationTriggerService instance;
    private Timer showNotifTimer;
    private Timer cancelNotifTimer;
    private BroadcastReceiver screenOnReceiver;
    private int notBoredCounter;
//    public static BoredomPrediction	used_prediction;
    public boolean screen_on;


    public static boolean notificationIsShown;
    public static boolean notificationIsScheduled;

    // Hours in which the experience sampling may be active
    private static final int QLEARN_START_HOUR					= 7;
    private static final int QLEARN_END_HOUR					= 23;

    private static final int QLEARN_MIN_DELAY = 10; //seconds

    private static final int QLEARN_MAX_DELAY = 5 * 60; //seconds

    private static final int MIN_TIME_SINCE_LAST_NOTIFICATION = 20; //20; // in minutes

    protected static final int		MIN_MS							= 1000 * 60; //milliseconds per minute
    protected static final int 		SEC_MS 							= 1000;	//milliseconds per second

    private static final int		CANCEL_NOTIFICATION_DELAY_MS	= MIN_MS * 5; //5

//    private static final int 		COUNTER_BALANCE_BORED_NON_BORED = 9; //9

    public NotificationTriggerService() {
        instance = this;
    }

    public static NotificationTriggerService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onBind()");
        }
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(screenOnReceiver);
        killService();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onCreate()");
        }

//        BoredomPredictionService.restartWhenPhoneReboots(this, true);

        // start the service. If manifest is up-to-date, the service will keep restarting when the application is
        // updated or when the phone reboots
//        BoredomPredictionService.startService(this);

        // Add listener to get updated about the predicted level of boredom
//        BoredomPredictionService.addBoredomListener(this);

        // Latest prediction should be NULL initially
//		if (BoredomPredictionService.latest_prediction != null) {
//			BoredomPredictionService.latest_prediction.toString();
//		}

        //register to SCREEN_ON event
        screen_on = false;
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenOnReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    screen_on = false;
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.d(TAG, Intent.ACTION_SCREEN_ON);
                    }
                    tryScheduleNotification();
                    screen_on = true;
                }
            }
        };
        registerReceiver(screenOnReceiver, intentFilter);


        studyManager = StudyManager.getInstance(this);
        language = Util.capitalizeWords(studyManager.vocabulary.target_language);
        notificationIsScheduled = false;
        notificationIsShown = false;
        notBoredCounter = 0;
        Util.putInt(getApplicationContext(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);

        QLearnJsonLog.startLogger(getApplicationContext());
    }

    /**
     *
     * Schedules a notification ahead of time
     *
     */
    public void tryScheduleNotification() {

        if (DISABLE_NOTIFICATIONS) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - disable_esm_notifs == TRUE");
            }
            return;
        }

        if (!Util.getBool(getApplicationContext(), QuickLearnPrefs.PREF_CONSENT_GIVEN, false)) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - still waiting for consent");
            }
            return;
        }

        if (!Util.getBool(getApplicationContext(), QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - still in setup");
            }
            return;
        }

        if (notificationIsShown) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - already shown");
            }
            return;
        }

        if (notificationIsScheduled) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - already scheduled");
            }
            return;
        }

        if (!isNotificationAllowed()) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - not allowed at this time -- IGNORED");
            }
            return;
        }

        if (!isMinTimeElapsed()) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - not enough time elapsed");
            }
            return;
        }

        if (applicationIsOpen()) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - application already in foreground");
            }
            return;
        }

        scheduleNotificationTimer();
    }

    private void scheduleNotificationTimer() {

        notificationIsScheduled = true;

        int delayMs = Util.randInt(QLEARN_MIN_DELAY, QLEARN_MAX_DELAY) * SEC_MS;

//        if(QuickLearnPrefs.DEBUG_MODE) {
//            delayMs = 5 * SEC_MS; //5sec
//        } else {
//            delayMs = Util.randInt(QLEARN_MIN_DELAY, QLEARN_MAX_DELAY) * SEC_MS;
//        }

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "scheduling notification - due in " + LogUtil.format((double) delayMs / MIN_MS) + " min");
        }

        if (showNotifTimer != null) {
            showNotifTimer.cancel();
            showNotifTimer.purge();
        }

        showNotifTimer = new Timer();
        showNotifTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                notificationIsScheduled = false;
                onNotificationTimerFired();
            }

        }, delayMs);

    }

    private void onNotificationTimerFired() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "onNotificationTimerFired()");
        }

        cancelNotification();

//        if (!userIsBored()) {
//            if(QuickLearnPrefs.DEBUG_MODE) {
//                Log.i(TAG, "notification aborted - user not bored and notBoredCounter not reached yet");
//            }
//            return;
        if (applicationIsOpen()) {

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification aborted - application already in foreground");
            }

        } else {
            triggerNotification();

            notificationIsShown = true;

            long delayMs;
            if(QuickLearnPrefs.DEBUG_MODE) {
                delayMs = 1 * MIN_MS;
            } else {
                delayMs = CANCEL_NOTIFICATION_DELAY_MS;
            }
//		if (DEBUG_MODE) {
//			delayMs = MIN_MS / 4;
//		}
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification - will cancel after " + LogUtil.format((double) delayMs / MIN_MS) + " min");
            }

            cancelNotifTimer = new Timer();
            cancelNotifTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    cancelNotification();
                }

            }, delayMs);
        }

    }

    public void cancelNotification() {

        if (cancelNotifTimer != null) {
            cancelNotifTimer.cancel();
            cancelNotifTimer.purge();
        }

        if (notificationIsShown) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "notification was shown but not clicked");
            }

            boolean firstInteraction = Util.getBool(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
            if(firstInteraction) {
                int condition = studyManager.current_condition;
                long notifPostedMillis = Util.getLong(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, 0);
//                String classifier = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED, "");
//                boolean prediction = Util.getBool(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_CLASSIFIER, false);
//                String probability = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_PROBABILITY, "");

                Util.put(getApplicationContext(), QuickLearnPrefs.NOTIF_IGNORED, true);
                QLearnJsonLog.onQLearnNotificationIgnored(condition, notifPostedMillis, getPackageName());
            }

//            //TODO: test if this works
//            if (BoredomPredictionService.isInitialized()) {
//                BoredomPredictionService.getInstance().onNewGroundTruth(NotificationTriggerService.used_prediction, false);
//            }
        }

        removeNotification();
    }

    private void removeNotification() {
        NotificationManager notifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
        notificationIsShown = false;
        Util.put(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
    }

    /**
     *
     * makes sure that notifications are only triggered during awake times
     *
     * @return
     */
    protected static boolean isNotificationAllowed() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            return true;
        }

        Calendar cal = Calendar.getInstance();
        Date now = new Date(System.currentTimeMillis());
        cal.setTime(now);
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

        // Log.i(TAG, "hour of the day is " + hourOfDay);

        if (hourOfDay >= QLEARN_START_HOUR && hourOfDay <= (QLEARN_END_HOUR - 1)) { return true; }

        return false;
    }

    /**
     *
     * @returns true when both screen is turned on and application is in foreground
     */
    protected boolean applicationIsOpen() {
        ActivityManager manager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
        if (tasks.isEmpty()) {
            return false;
        }
        String topActivityName = tasks.get(0).topActivity.getPackageName();
        return (topActivityName.equalsIgnoreCase(getPackageName()) && screen_on);
    }

    public void triggerNotification() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "triggerNotification");
        }

        //update PREF_LAST_NOTIFICATION_POSTED
//		Date now = new Date();
        Util.putLong(getApplicationContext(), QuickLearnPrefs.PREF_LAST_NOTIFICATION_POSTED_MS, System.currentTimeMillis());
        Util.putLong(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, System.currentTimeMillis());
        Util.put(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, true);

//        BoredomPrediction prediction = BoredomPredictionService.latest_prediction;
//        if(prediction != null) {
//            Util.putString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED, prediction.classifierName);
//            Util.put(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_CLASSIFIER, prediction.bored);
//            Util.putString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_PROBABILITY, String.valueOf(prediction.probability));
//        }

//        studyManager.updateStudyCondition();
        if(studyManager.current_condition == StudyManager.CONDITION_FLASHCARD) {
            showFlashcardTask();
        } else {
            showMultipleChoiceTask();
        }
    }

    public static void triggerTestNotification() {
        StudyManager studyManager = StudyManager.getInstance(getInstance());
        Word word = studyManager.vocabulary.nextWord();
        Util.putString(getInstance(), QuickLearnPrefs.PREF_CURRENT_WORD, word.key);
        getInstance().current_word = word;

        String language = studyManager.vocabulary.target_language;

        if(studyManager.current_condition == StudyManager.CONDITION_FLASHCARD) {
            FlashcardNotification n = new FlashcardNotification(getInstance());
            n.showFlashcard(language, word.translation);
        } else {
            MultipleChoiceNotification n = new MultipleChoiceNotification(getInstance());

            Random rand = new Random(System.currentTimeMillis());
            int option = 1 + rand.nextInt(NUMBER_OF_MULTIPLE_CHOICES);

            ArrayList<String> blacklist = new ArrayList<String>();
            blacklist.add(word.key);
            ArrayList<Word> deflectors = studyManager.vocabulary.getRandomWords(NUMBER_OF_MULTIPLE_CHOICES-1, blacklist);

            String a = "", b = "", c = "";
            switch(option) {
                case 1:
                    a = word.original;
                    b = deflectors.get(0).original;
                    c = deflectors.get(1).original;
                    break;
                case 2:
                    a = deflectors.get(0).original;
                    b = word.original;
                    c = deflectors.get(1).original;
                    break;
                case 3:
                    a = deflectors.get(0).original;
                    b = deflectors.get(1).original;
                    c = word.original;
                    break;
            }
            n.showTask(language, word.translation, a, b, c, option);
        }
        Util.putLong(getInstance(), QuickLearnPrefs.PREF_LAST_NOTIFICATION_POSTED_MS, System.currentTimeMillis());
        Util.putLong(getInstance(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, System.currentTimeMillis());
        Util.put(getInstance(), QuickLearnPrefs.NOTIF_POSTED, true);
    }

    private void showFlashcardTask() {
        nextWord();

        FlashcardNotification n = new FlashcardNotification(this);
        n.showFlashcard(language, current_word.translation);
    }

    private void showFlashcardSolution() {
        boolean firstInteraction = Util.getBool(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
        if(firstInteraction) {
            int condition = studyManager.current_condition;
            long notifPostedMillis = Util.getLong(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, 0);
//            String classifier = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED, "");
//            boolean prediction = Util.getBool(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_CLASSIFIER, false);
//            String probability = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_PROBABILITY, "");
            QLearnJsonLog.onNotificationInteraction(condition, notifPostedMillis);
            Util.put(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
        }
        studyManager.session_word_count++;
        FlashcardNotification n = new FlashcardNotification(this);
        n.showSolution(language, current_word.translation, current_word.original);
    }

    private void showFlashcardFinishScreen() {
        studyManager.session_set_count++;
        FlashcardNotification n = new FlashcardNotification(this);
        n.showFinishScreen(language);
    }

    private void showMultipleChoiceTask() {
        nextWord();

        Random rand = new Random(System.currentTimeMillis());
        int option = 1 + rand.nextInt(NUMBER_OF_MULTIPLE_CHOICES);

        ArrayList<String> blacklist = new ArrayList<String>();
        blacklist.add(current_word.key);
        ArrayList<Word> deflectors = studyManager.vocabulary.getRandomWords(NUMBER_OF_MULTIPLE_CHOICES-1, blacklist);

        String a = "", b = "", c = "";
        switch(option) {
            case 1:
                a = current_word.original;
                b = deflectors.get(0).original;
                c = deflectors.get(1).original;
                break;
            case 2:
                a = deflectors.get(0).original;
                b = current_word.original;
                c = deflectors.get(1).original;
                break;
            case 3:
                a = deflectors.get(0).original;
                b = deflectors.get(1).original;
                c = current_word.original;
                break;
        }

        MultipleChoiceNotification n = new MultipleChoiceNotification(this);
        n.showTask(language, current_word.translation, a, b, c, option);
    }

    private void showMultipleChoiceSolution(boolean correct) {
        boolean firstInteraction = Util.getBool(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
        if(firstInteraction) {
            int condition = studyManager.current_condition;
            long notifPostedMillis = Util.getLong(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, 0);
//            String classifier = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED, "");
//            boolean prediction = Util.getBool(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_CLASSIFIER, false);
//            String probability = Util.getString(getApplicationContext(), QuickLearnPrefs.BOREDOM_PRED_PROBABILITY, "");
            QLearnJsonLog.onNotificationInteraction(condition, notifPostedMillis);
            Util.put(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED, false);
        }
        studyManager.session_word_count++;
        MultipleChoiceNotification n = new MultipleChoiceNotification(this);
        n.showSolution(current_word, correct);
    }

    private void showMultipleChoiceFinishScreen() {
        studyManager.session_set_count++;
        MultipleChoiceNotification n = new MultipleChoiceNotification(this);
        n.showFinishScreen(language);
    }

    private void nextWord() {
        current_word = studyManager.vocabulary.nextWord();
        Util.putString(getApplicationContext(), QuickLearnPrefs.PREF_CURRENT_WORD, current_word.key);
    }

    private void updateWordList(boolean guessedCorrectly) {
        studyManager.vocabulary.updateWordList(guessedCorrectly);
    }

    // ========================================================================
    // Implementation of 'BoredomListener'
    // ========================================================================

//    /** Returns the probability that the user is bored */
//    public void onBoredomStateChanged(BoredomPrediction prediction) {
//
//        //instead of scheduling notifications here we subscribe to the event: user turned screen on
//        //- a) test whether notification is already scheduled, if so > nothing happens
//        //- b) test whether enough time has past since last notification: StudyManager.MIN_TIME_SINCE_LAST_NOTIFICATION (in mins) > if not: nothing happens
//        //- if a) and b), then schedule notification with a delay ranged from 10sec - 5mins
//        //- >> when notification is supposed to be triggered, check if user is bored, if so: post notification
//        //- >> if classification result is opposite (not bored), then only post in 1 out of 9 cases
//        //- have notification disappear when notification is ignored for more than 5 minutes
//
//        if (BoredomPredictionService.latest_prediction != null && BoredomPredictionService.latest_prediction.bored) {
//            tryScheduleNotification();
//        }
//
//    }

    public boolean isMinTimeElapsed() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            return true;
        }
        Date now = new Date();
        //Date minMinutesAgo = new Date(now.getTime() - (MIN_TIME_SINCE_LAST_NOTIFICATION * StudyManager.ONE_MINUTE_IN_MILLIS));
        long minMinutesAgo = now.getTime() - (MIN_TIME_SINCE_LAST_NOTIFICATION * StudyManager.ONE_MINUTE_IN_MILLIS);
//		Log.e(TAG, "Mon Sep 14 13:08:43 MESZ 2015");
        Date lastScheduled = Util.getDateFromTimestamp(Util.getLong(getApplicationContext(), QuickLearnPrefs.PREF_LAST_NOTIFICATION_POSTED_MS, minMinutesAgo)); //Util.getDateFromString("Mon Sep 14 13:08:43 MESZ 2015"); //
        long minutesPassed = ((now.getTime()/StudyManager.ONE_MINUTE_IN_MILLIS) - (lastScheduled.getTime()/StudyManager.ONE_MINUTE_IN_MILLIS));
        boolean notificationTimeoutPassed = (minutesPassed >= MIN_TIME_SINCE_LAST_NOTIFICATION);

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "isMinTimeElapsed: " + notificationTimeoutPassed + " (last notification posted " + minutesPassed + " Minutes ago. Required: " + MIN_TIME_SINCE_LAST_NOTIFICATION + " minutes)");
        }
        return notificationTimeoutPassed;
    }

//    public boolean userIsBored() {
//        boolean bored = false;
//        if (BoredomPredictionService.latest_prediction != null) {
//            if(QuickLearnPrefs.DEBUG_MODE) {
//                Log.i(TAG, BoredomPredictionService.latest_prediction.toString());
//            }
//            bored = BoredomPredictionService.latest_prediction.bored;
//        }
//
//        if(!bored) {
//            return notBoredCountReached();
//        } else {
//            return bored;
//        }
//    }

    /**
     * this method makes sure that we have roughly the same notifications posted in bored and non-bored states
     * COUNTER_BALANCE_BORED_NON_BORED regulates the ratio
     *
     *
     * @return
     */
//    public boolean notBoredCountReached() {
//        if(QuickLearnPrefs.DEBUG_MODE) {
//            return true;
//        }
//        //if classification result is opposite (not bored), then only post in 1 out of 9 cases
//        notBoredCounter++;
//        if(QuickLearnPrefs.DEBUG_MODE) {
//            Log.i(TAG, "notBoredCountReached: " + notBoredCounter);
//        }
//        if(notBoredCounter == COUNTER_BALANCE_BORED_NON_BORED) {
//            notBoredCounter = 0;
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * To stop the detection service and free the necessary resource, call the following methods
     */
    public void killService() {
        QLearnJsonLog.onStop0();
//        BoredomPredictionService.removeBoredomListener(this);
//        BoredomPredictionService.stopService(this);
    }

    // BroadcastReceiver to receive button presses from notifications
    public static class NotificationActionReceiver extends BroadcastReceiver {

        public final String	TAG	= NotificationActionReceiver.class.getSimpleName();

        public static final String FLASHCARD_TRANSLATE    = "org.hcilab.projects.borapplearn.FLASHCARD_TRANSLATE";
        public static final String FLASHCARD_KNEW_IT      = "org.hcilab.projects.borapplearn.FLASHCARD_KNEW_IT";
        public static final String FLASHCARD_DID_NOT_KNOW = "org.hcilab.projects.borapplearn.FLASHCARD_DID_NOT_KNOW";
        public static final String FLASHCARD_QUIT         = "org.hcilab.projects.borapplearn.FLASHCARD_QUIT";
        public static final String FLASHCARD_MORE_WORDS   = "org.hcilab.projects.borapplearn.FLASHCARD_MORE_WORDS";

        public static final String MULTIPLE_CHOICE_SELECT_A   = "org.hcilab.projects.borapplearn.MULTIPLE_CHOICE_SELECT_A";
        public static final String MULTIPLE_CHOICE_SELECT_B   = "org.hcilab.projects.borapplearn.MULTIPLE_CHOICE_SELECT_B";
        public static final String MULTIPLE_CHOICE_SELECT_C   = "org.hcilab.projects.borapplearn.MULTIPLE_CHOICE_SELECT_C";
        public static final String MULTIPLE_CHOICE_CONTINUE   = "org.hcilab.projects.borapplearn.MULTIPLE_CHOICE_CONTINUE";
        public static final String MULTIPLE_CHOICE_QUIT       = "org.hcilab.projects.borapplearn.MULTIPLE_CHOICE_QUIT";
        public static final String MULTIPLE_CHOICE_MORE_WORDS = "org.hcilab.projects.borapplearn.MULTIPLE_CHOICE_MORE_WORDS";

        public NotificationActionReceiver() {
        }

        private void logWordReviewed(Context context, boolean guessedCorrectly) {
            //log word review
            StudyManager studyManager = StudyManager.getInstance(context);
            String current_word = Util.getString(context, QuickLearnPrefs.PREF_CURRENT_WORD, "");
            boolean word_seen = studyManager.vocabulary.wordSeenBefore(current_word);
            QLearnJsonLog.onWordReviewed(current_word, studyManager.vocabulary.source_language, studyManager.vocabulary.target_language, QuickLearnPrefs.QLEARN_MODE_NOTIFICATION, studyManager.current_condition, guessedCorrectly, word_seen);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "notificationAction: " + intent.getAction());
            }

            if(intent.getAction().equals(FLASHCARD_TRANSLATE)) {
                NotificationTriggerService.getInstance().showFlashcardSolution();
            }

            if (intent.getAction().equals(FLASHCARD_KNEW_IT)) {
                logWordReviewed(context, true);
                NotificationTriggerService.getInstance().updateWordList(true);
                //update session_index
                int session_index = Util.getInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                session_index++;
                if(session_index == StudyManager.WORDS_PER_SET) {
                    Util.putInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                    NotificationTriggerService.getInstance().showFlashcardFinishScreen();
                } else {
                    Util.putInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, session_index);
                    NotificationTriggerService.getInstance().showFlashcardTask();
                }
            }

            if(intent.getAction().equals(FLASHCARD_DID_NOT_KNOW)) {
                logWordReviewed(context, false);
                NotificationTriggerService.getInstance().updateWordList(false);
                //update session_index
                int session_index = Util.getInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                session_index++;
                if(session_index == StudyManager.WORDS_PER_SET) {
                    Util.putInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                    NotificationTriggerService.getInstance().showFlashcardFinishScreen();
                } else {
                    Util.putInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, session_index);
                    NotificationTriggerService.getInstance().showFlashcardTask();
                }
            }

            if(intent.getAction().equals(FLASHCARD_QUIT)) {
                StudyManager studyManager = StudyManager.getInstance(context);
                QLearnJsonLog.onSessionFinished(QuickLearnPrefs.QLEARN_MODE_NOTIFICATION, studyManager.current_condition, studyManager.session_set_count, studyManager.session_word_count);
                studyManager.resetWordCount();
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(FlashcardNotification.NOTIFICATION_TAG, FlashcardNotification.NOTIFICATION_ID);
            }

            if(intent.getAction().equals(FLASHCARD_MORE_WORDS)) {
                NotificationTriggerService.getInstance().showFlashcardTask();
            }

            if(intent.getAction().equals(MULTIPLE_CHOICE_SELECT_A) || intent.getAction().equals(MULTIPLE_CHOICE_SELECT_B) || intent.getAction().equals(MULTIPLE_CHOICE_SELECT_C)) {
                boolean correct = intent.getBooleanExtra(MultipleChoiceNotification.EXTRA_CORRECT, false);
                logWordReviewed(context, correct);
                NotificationTriggerService.getInstance().updateWordList(correct);
                NotificationTriggerService.getInstance().showMultipleChoiceSolution(correct);
            }

            if(intent.getAction().equals(MULTIPLE_CHOICE_CONTINUE)) {
                //update session_index
                int session_index = Util.getInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                session_index++;
                if(session_index == StudyManager.WORDS_PER_SET) {
                    Util.putInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                    NotificationTriggerService.getInstance().showMultipleChoiceFinishScreen();
                } else {
                    Util.putInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, session_index);
                    NotificationTriggerService.getInstance().showMultipleChoiceTask();
                }
            }

            if(intent.getAction().equals(MULTIPLE_CHOICE_QUIT)) {
                StudyManager studyManager = StudyManager.getInstance(context);
                QLearnJsonLog.onSessionFinished(QuickLearnPrefs.QLEARN_MODE_NOTIFICATION, studyManager.current_condition, studyManager.session_set_count, studyManager.session_word_count);
                studyManager.resetWordCount();
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(MultipleChoiceNotification.NOTIFICATION_TAG, MultipleChoiceNotification.NOTIFICATION_ID);
            }

            if(intent.getAction().equals(MULTIPLE_CHOICE_MORE_WORDS)) {
                NotificationTriggerService.getInstance().showMultipleChoiceTask();
            }

        }

    }

}