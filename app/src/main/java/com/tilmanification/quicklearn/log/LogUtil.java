/****************************************************************************
 * Util
 ****************************************************************************/
package com.tilmanification.quicklearn.log;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import com.tilmanification.quicklearn.QuickLearnPrefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

// import com.google.android.maps.GeoPoint;

/**
 * This is a Util class!
 *
 * @author <a href="mailto:"martin.pielot@offis.de">Martin Pielot</a>
 * @version 12.02.2010
 */

@SuppressLint("SimpleDateFormat")
public class LogUtil {

    // ========================================================================
    // Constant Fields;
    // ========================================================================

    public static final boolean				USE_EXPERIMENTAL_CODE	= true;

    private static final String				TAG						= LogUtil.class.getSimpleName();

    public static final double				MS_TO_SEC				= 1000;
    public static final double				MS_TO_MIN				= MS_TO_SEC * 60;
    public static final double				MS_TO_HOUR				= MS_TO_MIN * 60;
    public static final double				MS_TO_DAY				= MS_TO_HOUR * 24;

    public static final int					FULL_CIRCLE				= 360;
    public static final int					HALF_CIRCLE				= 180;
    private static final double				FORMAT_CONSTANT			= 100.0;

    public static final boolean				ORIGIN_TOP_LEFT			= true;
    public static final boolean				ORIGIN_BOTTOM_LEFT		= false;

    private static final SimpleDateFormat	sdfEn					= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final SimpleDateFormat	sdfDe					= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final SimpleDateFormat	sdfMySql				= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static long						start_time				= System.currentTimeMillis();

    // ========================================================================
    // Methods
    // ========================================================================

    public static long getUpTimeSec() {
        final long now = System.currentTimeMillis();
        final long upTimeSec = (now - start_time) / 1000;
        return upTimeSec;
    }

    public static int within(int i, int max, int min) {
        if (i > max) i = max;

        if (i < min) i = min;

        return i;
    }

    public static double within(double d, int max, int min) {
        if (d > max) d = max;

        if (d < min) d = min;

        return d;
    }

    /**
     * Applies a mathematically correct mod operation to v. It will not return
     * negative values.
     */
    public static int mod(int v, int ring) {
        v %= ring;
        if (v < 0) v += ring;
        return v;
    }

    public static double modDouble(double d, int ring) {
        while (d < ring)
            d += ring;

        while (d >= ring)
            d -= ring;
        return d;
    }

    public static int signedDirection(int relDirection) {
        final int signedDirection = (relDirection > HALF_CIRCLE ? relDirection - FULL_CIRCLE : relDirection);
        return signedDirection;
    }

    /**
     * Shuffles the order of the objects in the given array by using the
     * Fisher�Yates algorithm.
     */
    public static void randomizeOrder(Object[] ar) {
        Random rnd = new Random(System.currentTimeMillis());
        for (int i = ar.length - 1; i >= 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Object a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static String format(double d) {
        return format(d, FORMAT_CONSTANT);
    }

    public static String format(double d, double formatConstant) {
        int i = (int) (d * formatConstant);
        return "" + i / formatConstant;
    }

    public static String format(Location l) {
        try {
            return format(l.getLatitude(), 10000) + "/" + format(l.getLongitude(), 10000) + " " + l.getProvider();
        } catch (Exception e) {
            // Log.w(TAG, "Could not format location " + l + " due to " +
            // e.getMessage());
            return "" + l;
        }
    }

    public static double azimuth(int ax, int ay, int bx, int by, boolean originIsTopLeft) {

        // Transform positions so that posA has the position (0,0)
        bx -= ax;
        by -= ay;
        ax = 0;
        ay = 0;

        double azimuth = 0.0;
        double dx = bx - ax;
        double dy = originIsTopLeft ? ay - by : by - ay;

        // Position A and B have the same coordinates
        if (dx == 0.0 && dy == 0.0) {
            return -1.0;

        } else if (dx == 0.0 && dy > 0.0) {
            azimuth = 0.0;

        } else if (dx == 0.0 && dy < 0.0) {
            azimuth = Math.PI;

        } else if (dy == 0.0 && dx > 0.0) {
            azimuth = Math.PI / 2.0;

        } else if (dy == 0.0 && dx < 0.0) {
            azimuth = (3.0 / 2.0) * Math.PI;

            // upper right quadrant
        } else if (dx > 0.0 && dy > 0.0) {
            azimuth = Math.atan(Math.abs(dx / dy));

            // upper left quadrant
        } else if (dx < 0.0 && dy > 0.0) {
            azimuth = 2.0 * Math.PI - Math.atan(Math.abs(dx / dy));

            // lower left quadrant
        } else if (dx < 0.0 && dy < 0.0) {
            azimuth = Math.PI + Math.atan(Math.abs(dx / dy));

            // lower right quadrant
        } else if (dx > 0.0 && dy < 0.0) {
            azimuth = Math.PI - Math.atan(Math.abs(dx / dy));
        }

        // conversion from radiant to degree
        azimuth = azimuth * (180.0 / Math.PI);

        return azimuth;
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        final int xv = x1 - x2;
        final int yv = y1 - y2;
        return Math.sqrt(xv * xv + yv * yv);
    }

    /**
     * Calculates the difference between two angles, e.g. the trajectory and the
     * azimuth between the user and another place.
     *
     * @param referenceAngle
     *            the reference angle
     * @param otherAngle
     *            the second angle
     * @return the signed difference between the to angle, seen from the first
     *         angle
     */
    public static double difference(double referenceAngle, double otherAngle) {

        double difference = otherAngle - referenceAngle;

        if (difference > 180) difference = difference - 360;

        if (difference <= -180) difference = difference + 360;

        return difference;
    }

    public static void copyFile(File src, File dst) throws IOException {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "copying " + src + " to " + dst);
        }
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "copying successful");
        }
    }

    public static double cutDecimalPlaces(double d, double digit10) {
        int i = (int) (d * digit10 + .5);
        return i / digit10;
    }

    public static void setVisible(Activity activity, final View view, final boolean visible) {
        Log.i(TAG, "set " + view.getClass().getSimpleName() + " visible == " + visible);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
                view.postInvalidate();
            }
        });
    }

    /**
     * Creates the address String from an Address object
     *
     * @param address
     *            the address object
     * @return the String with the address
     */
    public static String getAddressName(Address address) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(address.getAddressLine(i));
        }

        return sb.toString();
    }

    public static String dateEn() {
        final long now = System.currentTimeMillis();
        final String date = getTimeStringEn(now);
        return date;
    }

    public static void addDateEn(Map<String, String> log) {
        final String date = dateEn();
        log.put(LogKeys.KEY_DATE, date);
    }

    public static String getTimeStringEn(long now) {
        Date d = new Date(now);
        return sdfEn.format(d);
    }

    public static String getTimeStringDe(long now) {
        Date d = new Date(now);
        return sdfDe.format(d);
    }

    public static String getTimeStringMySql(long now) {
        Date d = new Date(now);
        return sdfMySql.format(d);
    }

    public static boolean isPlugged(Context context) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        boolean plugged = usbCharge || acCharge;

        return plugged;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        return isScreenOn;
    }

    public static String getRingerModeString(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        return getRingerModeString(ringerMode);
    }

    public static String getRingerModeString(int ringerMode) {
        String ringerModeStr = "Unknown";
        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            ringerModeStr = "Normal";
        } else if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            ringerModeStr = "Silent";
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            ringerModeStr = "Vibrate";
        }
        return ringerModeStr;
    }

    public static double getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return getBatteryLevel(batteryIntent);
    }

    public static double getBatteryLevel(Intent batteryIntent) {
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) { return 0.0f; }

        return 100.0 * level / scale;
    }

    //TODO: uncomment this as soon as JSONLogTransmitter is ready
//    public static String getPhoneNumberHash(String number) {
//        try {
//            String digits = number.replaceAll("[^0-9]", "");
//            String numSub = digits.substring(Math.max(digits.length() - 8, 0));
//            // Log.i(TAG, number + " --> " + digits + " --> " + numSub);
//            return JsonLogTransmitter.getHashSum(numSub);
//
//        } catch (Exception e) {
//            return "X" + JsonLogTransmitter.getHashSum(number);
//
//        }
//    }

    public static boolean recursiveDelete(File file) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "recursiveDelete "+file);
        }
        if(file.isDirectory()) {
            boolean success = true;
            File [] files = file.listFiles();
            if(files != null) {
                for(File f : files) {
                    success |= recursiveDelete(f);
                }
            }
            return success;
        }else{
            return file.delete();
        }
    }
}
