package com.tilmanification.quicklearn;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Util {

    private static final String	TAG	= Util.class.getSimpleName();

    public static boolean	use_external_dir	= true;

    public static void put(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean getBool(Context context, String key, boolean defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean result = prefs.getBoolean(key, defValue);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "getBool for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String result = prefs.getString(key, defValue);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "getString for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int result = prefs.getInt(key, defValue);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "getInt for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long result = prefs.getLong(key, defValue);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "getLong for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static File getDir(Context context) {

        if (use_external_dir) {
            File filesDir = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
            if (!filesDir.exists()) {
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.w(TAG, "creating directory " + filesDir);
                }
                filesDir.mkdirs();
            }
            return filesDir;
        } else {
            return context.getFilesDir();
        }
    }

    public static String getFilePath(Context context, String filename) throws IOException {

        File filesDir = getDir(context);

        File file = new File(filesDir, filename);
        if (!file.exists()) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.w(TAG, file + " not found, copying from assets");
            }
            retrieveFromAssets(context, filename);
        } else {}

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Using " + file);
        }
        return file.getAbsolutePath();
    }

    public static void retrieveFromAssets(Context context, String filename) throws IOException {

        InputStream is = context.getAssets().open(filename);

        // Destination
        // File filesDir = context.getFilesDir();
        File filesDir = getDir(context);
        if (!filesDir.exists()) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, filesDir.getAbsolutePath() + " does not exist -> creating directory");
            }
            filesDir.mkdirs();
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "dir exists == " + filesDir.exists());
            }
        }
        File outFile = new File(filesDir, filename);

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "retrieveFromAssets( .. ) copying " + filename + " to " + filesDir);
        }

        FileOutputStream fos = new FileOutputStream(outFile);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[8 * 1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        // Close the streams
        fos.flush();
        fos.close();
        is.close();
    }

    static String	ACCESSIBILITY_SERVICE_NAME	= "BoredomAccessibilityService";

    /*
     * http://stackoverflow.com/questions/5081145/android-how-do-you-check-if-a-
     * particular-accessibilityservice-is-enabled
     */
    public static boolean isAccessibilityEnabled(Context context) {
        return isAccessibilityEnabled0(context) || isAccessibilityEnabled1(context);
    }

    /*
     * http://stackoverflow.com/questions/5081145/android-how-do-you-check-if-a-
     * particular-accessibilityservice-is-enabled
     */
    public static boolean isAccessibilityEnabled0(Context context) {

        String id = ACCESSIBILITY_SERVICE_NAME;

        // Log.i(TAG, "isAccessibilityEnabled0() - " + id);

        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        // logInstalledAccessiblityServices(context);

        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            String serviceId = service.getId();

            if (serviceId.indexOf(id) >= 0) { return true; }
        }

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.w(TAG, "did NOT find " + id);
        }

        // fallback
        return false;
    }

    public static boolean isAccessibilityEnabled1(Context context) {
        String id = ACCESSIBILITY_SERVICE_NAME;
        // Log.i(TAG, "isAccessibilityEnabled1() - " + id);

        int accessibilityEnabled = 0;
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (SettingNotFoundException e) {}

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {

            String settingValue = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {

                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    if (accessabilityService.indexOf(id) >= 0) { return true; }
                }
            }
        } else {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.w(TAG, "accessibility services appears to be disabled");
            }
        }
        return accessibilityFound;
    }

    public static boolean isNetworkLocationEnabled(Context context) {
        boolean networkEnabled = false;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.w(TAG, e + " when checking if network provider is enabled");
            }
        }
        // Log.i(TAG, "network location enabled == " + networkEnabled);
        return networkEnabled;
    }

    /**
     *
     * @param item
     * @param arr
     * @return whether an int is unique in a given array
     */
    public static boolean isUnique(int item, ArrayList<Integer> arr) {
        int count = 0;
        for(int i=0; i<arr.size(); i++) {
            if(arr.get(i)==item) {
                count++;
            }
        }
        return count == 1;
    }

    public static String capitalizeWords(String sentence) {
        return sentence = (sentence.length() != 0) ? sentence.toString().toLowerCase().substring(0,1).toUpperCase().concat(sentence.substring(1)): sentence;
    }

    /**
     * BROKEN since it does not support various timezones (3 or 4 letters (z))
     * @param dateSring
     * @return
     */
//	public static Date getDateFromString(String dateSring) {
//		try {
////			Log.i(TAG, "Locale: " + Locale.getDefault().toString());
//			DateFormat df = new SimpleDateFormat(QuickLearnPrefs.DATE_FORMAT, Locale.getDefault()); //TODO: test whether Locale.US is needed as 2nd parameter
//			return df.parse(dateSring);
//		} catch (ParseException pe) {
//			pe.printStackTrace();
//            return new Date();
//		}
//	}

    /**
     * parses a timestamp into a Date object, if not parseable, yesterday's Date is returned
     * @param ts
     * @return
     */
    public static Date getDateFromTimestamp(long ts) {
        Date date;
        try {
            date = new Date(ts);
        } catch (Exception pe) {
            pe.printStackTrace();
            //return date one day ago
            long DAY_IN_MS = 1000 * 60 * 60 * 24;
            date = new Date(System.currentTimeMillis() - (1 * DAY_IN_MS));
        }
        return date;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * translates gender index into string
     *
     * @param pos
     * @return
     */
    public static String getGenderStr(int pos) {
        if (pos == 1) return "female";
        else if (pos == 2) return "male";
        else if (pos == 3) return "other";
        else if (pos == 4) return "prefer not to say";
        else return "unknown";
    }

    /**
     * translates usage index into string
     *
     * @param pos
     * @return
     */
    public static String getUsageStr(int pos) {
        if (pos == 1) return "only";
        else if (pos == 2) return "friends";
        else if (pos == 3) return "family";
        else if (pos == 4) return "both";
        else if (pos == 5) return "other";
        else return "unknown";
    }

    public static String getTimezone() {
        return "" + Calendar.getInstance().get(Calendar.ZONE_OFFSET)
                / (60 * 60 * 1000);
    }

    public static String getSimcardInfo(Context context) {

        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return "absent";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "network locked";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "pin required";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "puk required";
            case TelephonyManager.SIM_STATE_READY:
                return "ready";
            case TelephonyManager.SIM_STATE_UNKNOWN:
                return "unknown";
            default:
                return "other";
        }

    }

    public static String getLocale() {
        return "" + Locale.getDefault().toString();
    }

    /**
     *
     * @param d  Date
     * @return number of days since Date d
     */
    public static long getDaysPassedSince(Date d) {
        Date now = new Date();

        long diffInMillies = now.getTime() - d.getTime();
        long days_passed = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return days_passed;
    }
}
