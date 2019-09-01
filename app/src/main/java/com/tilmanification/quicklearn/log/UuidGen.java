package com.tilmanification.quicklearn.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.tilmanification.quicklearn.QuickLearnPrefs;

import java.security.MessageDigest;
import java.util.UUID;

public class UuidGen {

    private static String uuid;
    private static String pid;

    /**
     * Returns the unique user id.
     */
    public static String getPid(Context context) {
        if (pid == null) {
            String uuid = getUniqueId(context);
            pid = uuid.substring(0, 5);
        }
        return pid;
    }

    /**
     * Returns the unique user id.
     */
    public static String getPid6(Context context) {
        if (pid == null) {
            String uuid = getUniqueId(context);
            pid = uuid.substring(0, 6);
        }
        return pid;
    }

    /**
     * Returns the unique user id.
     */
    public static String getUniqueId(Context context) {
        if (uuid == null) {
            uuid = retrieveUniqueId(context);
        }
        return uuid;
    }

    private static String retrieveUniqueId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String userId = sharedPreferences.getString(QuickLearnPrefs.PREF_UID, "");

        // if user ID is still there, store it
        if (userId.length() > 0)
            return userId;

        // otherwise, create new user ID
        userId = fromAndroidId(context);

        // store user id in preferences
        Editor edit = sharedPreferences.edit();
        edit.putString(QuickLearnPrefs.PREF_UID, userId);
        edit.commit();

        return userId;
    }

    @SuppressWarnings("unused")
    private static String createUuid() {
        String userId;
        UUID uuid = UUID.randomUUID();
        userId = uuid.toString();
        return userId;
    }

    private static String fromAndroidId(Context context) {
        String userId = retrieveAndroidId(context);

        try {
            MessageDigest md5;
            md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(userId.getBytes());
            byte[] result = md5.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                hexString.append(Integer.toHexString(0xFF & result[i]));
            }
            userId = hexString.toString();
        } catch (Exception e) {
            userId = "nullId";
        }

        // check if this is the infected user id and extend randomly
        if (userId.equals("cf95dc53f383f9a836fd749f3ef439cd")) {
            userId = userId + "-" + System.currentTimeMillis();
        }
        return userId;
    }

    private static String retrieveAndroidId(Context context) {
        String userId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return userId;
    }
}
