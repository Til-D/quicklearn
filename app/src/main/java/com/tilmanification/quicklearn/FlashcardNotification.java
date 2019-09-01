package com.tilmanification.quicklearn;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.tilmanification.quicklearn.log.QlearnKeys;

public class FlashcardNotification {

    public static final String NOTIFICATION_TAG = "TASK";
    public static final int    NOTIFICATION_ID  = 0;

    private Context context;

    public FlashcardNotification(Context context) {
        this.context = context;
    }

    public void showFlashcard(String language, String word) {
        // Translate button
        Intent intentTranslate = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentTranslate.setAction(NotificationTriggerService.NotificationActionReceiver.FLASHCARD_TRANSLATE);
        PendingIntent pendingIntentTranslate = PendingIntent.getBroadcast(context, 1, intentTranslate, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent to open the MainActivity
        Util.putInt(context, QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_NOTIFICATION);
        Intent intentMain = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.quicklearnnotificon)
                .setContentTitle(String.format(context.getString(R.string.notif_title), language))
                .setContentText(word)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xF99847);

        // Sound/vibrate?
        if(QuickLearnPrefs.NOTIFICATION_MAKE_NOISE) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }

        // Add button
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel,
                context.getString(R.string.button_translate), pendingIntentTranslate).build());

        // Show notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);

        Util.put(context, QuickLearnPrefs.PREF_NOTIF_SHOWN, true);
    }

    public void showSolution(String language, String word, String translation) {
        // Knew it button
        Intent intentKnewIt = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentKnewIt.setAction(NotificationTriggerService.NotificationActionReceiver.FLASHCARD_KNEW_IT);
        PendingIntent pendingIntentKnewIt = PendingIntent.getBroadcast(context, 1, intentKnewIt, PendingIntent.FLAG_UPDATE_CURRENT);

        // Did not know button
        Intent intentDidNotKnow = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentDidNotKnow.setAction(NotificationTriggerService.NotificationActionReceiver.FLASHCARD_DID_NOT_KNOW);
        PendingIntent pendingIntentDidNotKnow = PendingIntent.getBroadcast(context, 1, intentDidNotKnow, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent to open the MainActivity
        Util.putInt(context, QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_NOTIFICATION);
        Intent intentMain = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.quicklearnnotificon)
                .setContentTitle(String.format(context.getString(R.string.notif_title), language))
                .setContentText(String.format(context.getString(R.string.notif_fc_result), word, translation))
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xF99847);

        // Add buttons
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel,
                context.getString(R.string.notif_knew_it), pendingIntentKnewIt).build());
        builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pixel,
                context.getString(R.string.notif_did_not_know), pendingIntentDidNotKnow).build());

        // Show notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }

    public void showFinishScreen(String language) {
        // Quit button
        Intent intentQuit = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentQuit.setAction(NotificationTriggerService.NotificationActionReceiver.FLASHCARD_QUIT);
        PendingIntent pendingIntentQuit = PendingIntent.getBroadcast(context, 1, intentQuit, PendingIntent.FLAG_UPDATE_CURRENT);

        // More words button
        Intent intentMoreWords = new Intent(context, NotificationTriggerService.NotificationActionReceiver.class);
        intentMoreWords.setAction(NotificationTriggerService.NotificationActionReceiver.FLASHCARD_MORE_WORDS);
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