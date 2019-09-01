package com.tilmanification.quicklearn;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.tilmanification.quicklearn.log.QlearnKeys;

public class MultipleChoiceNotification {

    public static final String NOTIFICATION_TAG = "TASK";
    public static final int    NOTIFICATION_ID  = 0;

    public static final String EXTRA_CORRECT = "EXTRA_CORRECT";

    private Context context;

    public MultipleChoiceNotification(Context context) {
        this.context = context;
    }

    public void showTask(String language, String word, String optionA, String optionB, String optionC, int solution) {
        // Button 1
        Intent intentSelectA = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentSelectA.setAction(NotificationTriggerService.NotificationActionReceiver.MULTIPLE_CHOICE_SELECT_A);
        intentSelectA.putExtra(EXTRA_CORRECT, solution == 1); // is this the correct solution? (boolean)
        PendingIntent pendingIntentSelectA = PendingIntent.getBroadcast(context, 1, intentSelectA, PendingIntent.FLAG_UPDATE_CURRENT);

        // Button 2
        Intent intentSelectB = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentSelectB.setAction(NotificationTriggerService.NotificationActionReceiver.MULTIPLE_CHOICE_SELECT_B);
        intentSelectB.putExtra(EXTRA_CORRECT, solution == 2); // is this the correct solution? (boolean)
        PendingIntent pendingIntentSelectB = PendingIntent.getBroadcast(context, 1, intentSelectB, PendingIntent.FLAG_UPDATE_CURRENT);

        // Button 3
        Intent intentSelectC = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentSelectC.setAction(NotificationTriggerService.NotificationActionReceiver.MULTIPLE_CHOICE_SELECT_C);
        intentSelectC.putExtra(EXTRA_CORRECT, solution == 3); // is this the correct solution? (boolean)
        PendingIntent pendingIntentSelectC = PendingIntent.getBroadcast(context, 1, intentSelectC, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent to open the MainActivity
        Util.putInt(context, QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_NOTIFICATION);
        Intent intentMain = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.quicklearnnotificon)
                .setContentTitle(String.format(context.getString(R.string.notif_title), language))
                .setContentText(String.format(context.getString(R.string.notif_mc_task), word))
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xF99847);

        // Sound/vibrate?
        if(QuickLearnPrefs.NOTIFICATION_MAKE_NOISE) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }

        // Add buttons
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel, optionA, pendingIntentSelectA).build());
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel, optionB, pendingIntentSelectB).build());
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel, optionC, pendingIntentSelectC).build());

        // Show notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);

        Util.put(context, QuickLearnPrefs.PREF_NOTIF_SHOWN, true);
    }

    public void showSolution(Word word, boolean correct) {
        // Continue button
        String language = word.target_language;
        String solution = word.translation + " : " + word.original;
        Intent intentContinue = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentContinue.setAction(NotificationTriggerService.NotificationActionReceiver.MULTIPLE_CHOICE_CONTINUE);
        PendingIntent pendingIntentContinue = PendingIntent.getBroadcast(context, 1, intentContinue, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent to open the MainActivity
        Util.putInt(context, QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_NOTIFICATION);
        Intent intentMain = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.quicklearnnotificon)
                .setContentTitle(String.format(context.getString(R.string.notif_title), language))
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xF99847);

        // Show correct/incorrect
        if(correct) {
            builder.setContentText(context.getString(R.string.notif_correct) + " " + solution);
        } else {
            builder.setContentText(context.getString(R.string.notif_incorrect) + " " + solution);
        }

        // Add button
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel,
                context.getString(R.string.notif_continue), pendingIntentContinue).build());

        // Show notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }

    public void showFinishScreen(String language) {
        // Quit button
        Intent intentQuit = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentQuit.setAction(NotificationTriggerService.NotificationActionReceiver.MULTIPLE_CHOICE_QUIT);
        PendingIntent pendingIntentQuit = PendingIntent.getBroadcast(context, 1, intentQuit, PendingIntent.FLAG_UPDATE_CURRENT);

        // More words button
        Intent intentMoreWords = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentMoreWords.setAction(NotificationTriggerService.NotificationActionReceiver.MULTIPLE_CHOICE_MORE_WORDS);
        PendingIntent pendingIntentMoreWords = PendingIntent.getBroadcast(context, 1, intentMoreWords, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent to open the MainActivity
        Util.putInt(context, QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_NOTIFICATION);
        Intent intentMain = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.quicklearnnotificon)
                .setContentTitle(String.format(context.getString(R.string.notif_title), language))
                .setContentText(context.getString(R.string.notif_task_complete))
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xF99847);

        // Add buttons
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel,
                context.getString(R.string.notif_quit), pendingIntentQuit).build());
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel,
                context.getString(R.string.notif_more_words), pendingIntentMoreWords).build());

        // Show notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }

}