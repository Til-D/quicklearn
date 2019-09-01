package com.tilmanification.quicklearn.sensors;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.tilmanification.quicklearn.Util;
import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

class FeatureSensorSemanticLocation extends FeatureSensor implements LocationListener {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    public static final String	TAG							= FeatureSensorSemanticLocation.class.getSimpleName();

    private static final Object	LOCATION_LIST_MUTEX			= new Object();

    private static final String	HOME_LATS_OBJ				= "home_lats.obj";
    private static final String	HOME_LONGS_OBJ				= "home_longs.obj";
    private static final String	WORK_LATS_OBJ				= "work_lats.obj";
    private static final String	WORK_LONGS_OBJ				= "work_longs.obj";

    private static final float	DISTANCE_THRESH				= 150;
    private static final long	LOC_UPDATE_THRESH_MS		= 1000 * 10 * 1;
    private static final long	SEMANTIC_LOC_UPDATE_CYCLE	= LOC_UPDATE_THRESH_MS * 5;

    // ========================================================================
    // Fields
    // ========================================================================

    private Timer				timer;

    private LocationManager		locationManager;

    private LinkedList<Double>	homeLats					= new LinkedList<Double>();
    private LinkedList<Double>	homeLongs					= new LinkedList<Double>();
    private LinkedList<Double>	workLats					= new LinkedList<Double>();
    private LinkedList<Double>	workLongs					= new LinkedList<Double>();

    private double				homeLatMedian;
    private double				homeLongMedian;
    private double				workLatMedian;
    private double				workLongMedian;

    private Location			homeLocation;
    private Location			workLocation;

    private String currentLocation;

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeatureSensorSemanticLocation(Context context, Features features) {
        super(context, features);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.homeLocation = new Location(LocationManager.NETWORK_PROVIDER);
        this.workLocation = new Location(LocationManager.NETWORK_PROVIDER);
        this.currentLocation = "unknown";
        // deleteAll();
    }

    public void onStart() {
        this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOC_UPDATE_THRESH_MS, 1, this);

        loadAll();

        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                updateHomeAndWorkLocations();
            }

        }, 0, SEMANTIC_LOC_UPDATE_CYCLE);
    }

    public void onStop() {
        saveAll();

        this.locationManager.removeUpdates(this);

        this.timer.cancel();
        this.timer.purge();
    }

    // ========================================================================
    // Methods
    // ========================================================================

    protected void updateHomeAndWorkLocations() {
        Log.i(TAG, "updateHomeAndWorkLocations()");

        synchronized (homeLats) {
            homeLatMedian = median(homeLats);
        }
        synchronized (homeLongs) {
            homeLongMedian = median(homeLongs);
        }
        synchronized (workLats) {
            workLatMedian = median(workLats);
        }
        synchronized (workLongs) {
            workLongMedian = median(workLongs);
        }

        // updating location objects
        if (homeLatMedian != Double.NaN && homeLongMedian != Double.NaN) {
            this.homeLocation.setLatitude(homeLatMedian);
            this.homeLocation.setLongitude(homeLongMedian);
        }
        if (workLatMedian != Double.NaN && workLongMedian != Double.NaN) {
            this.workLocation.setLatitude(workLatMedian);
            this.workLocation.setLongitude(workLongMedian);
        }

        saveAll();

        Log.i(TAG, "work: " + format(workLocation) + ", home: " + format(homeLocation));
    }

    private boolean isHome(Location location) {
        float distanceTo = homeLocation.distanceTo(location);
        // Log.i(TAG, "distance to home: " + distanceTo);
        if (distanceTo < DISTANCE_THRESH) { return true; }

        return false;
    }

    private boolean isWork(Location location) {
        float distanceTo = workLocation.distanceTo(location);
        // Log.i(TAG, "distance to work: " + distanceTo);
        if (distanceTo < DISTANCE_THRESH) { return true; }

        return false;
    }

    // ========================================================================
    // LocationListener
    // ========================================================================

    @Override
    public void onLocationChanged(Location location) {

        int hourOfDay = getHourOfDay();

        synchronized (LOCATION_LIST_MUTEX) {

            // if it is night
            if (hourOfDay >= 0 && hourOfDay <= 6 || hourOfDay >= 20) {
                Log.v(TAG, "home-time location: "
                        + format(location)
                        + ", accuracy: "
                        + location.getAccuracy()
                        + " at hour: "
                        + hourOfDay);

                homeLats.add(location.getLatitude());
                homeLongs.add(location.getLongitude());

            } else if (hourOfDay >= 10 && hourOfDay <= 18 && isWeekDay()) {
                Log.v(TAG, "work-time location: "
                        + format(location)
                        + ", accuracy: "
                        + location.getAccuracy()
                        + " at hour: "
                        + hourOfDay);

                workLats.add(location.getLatitude());
                workLongs.add(location.getLongitude());

            } else {
                Log.i(TAG, "other-time location: "
                        + format(location)
                        + ", accuracy: "
                        + location.getAccuracy()
                        + " at hour: "
                        + hourOfDay);
            }
        }

        boolean home = isHome(location);
        boolean work = isWork(location);
        Log.i(TAG, "home == " + home + ", work == " + work);

        String loc = Features.SEM_LOC_OTHER;
        if (home) {
//            features.onSemanticLocationChanged(Features.SEM_LOC_HOME);
            loc = Features.SEM_LOC_HOME;
        } else if (work) {
//            features.onSemanticLocationChanged(Features.SEM_LOC_WORK);
            loc = Features.SEM_LOC_WORK;
        } else {
//            features.onSemanticLocationChanged(Features.SEM_LOC_OTHER);
        }

        if(!loc.equals(currentLocation)) {
            QLearnJsonLog.logSensorSnapshot(QlearnKeys.FEATURE_SEMANTIC_LOCATION, loc);
            currentLocation = loc;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    // ========================================================================
    // Serialization Helpers
    // ========================================================================

    private void loadAll() {
        this.homeLats = load(HOME_LATS_OBJ);
        this.homeLongs = load(HOME_LONGS_OBJ);
        this.workLats = load(WORK_LATS_OBJ);
        this.workLongs = load(WORK_LONGS_OBJ);
    }

    @SuppressWarnings("unchecked")
    private LinkedList<Double> load(String filename) {
        try {
            File file = getFile(filename);
            // Log.v(TAG, "loading from " + file + ", exists == " + file.exists() + ", length " + file.length());

            synchronized (LOCATION_LIST_MUTEX) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                LinkedList<Double> list = (LinkedList<Double>) ois.readObject();
                ois.close();

                return list;
            }

        } catch (Exception e) {
            Log.w(TAG, e + " in load( " + filename + " )");
        }

        Log.i(TAG, "generating empty list");
        return new LinkedList<Double>();

    }

    private void saveAll() {
        save(homeLats, HOME_LATS_OBJ);
        save(homeLongs, HOME_LONGS_OBJ);
        save(workLats, WORK_LATS_OBJ);
        save(workLongs, WORK_LONGS_OBJ);
    }

    private void save(LinkedList<Double> list, String filename) {
        try {

            File file = getFile(filename);
            synchronized (LOCATION_LIST_MUTEX) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(list);
                oos.close();
            }
            // Log.v(TAG, file + " saved, length now " + file.length());

        } catch (Exception e) {
            Log.w(TAG, e + " in save( .., " + filename + " )");
        }
    }

    @SuppressWarnings("unused")
    private void deleteAll() {
        Log.e(TAG, "deleteAll() -- REMEMBER TO DISABLE");
        delete(HOME_LATS_OBJ);
        delete(HOME_LONGS_OBJ);
        delete(WORK_LATS_OBJ);
        delete(WORK_LONGS_OBJ);
    }

    private void delete(String filename) {
        try {

            File file = getFile(filename);
            boolean success = false;
            synchronized (LOCATION_LIST_MUTEX) {
                success = file.delete();
            }
            Log.d(TAG, file + " deleted, success == " + success);

        } catch (Exception e) {
            Log.w(TAG, e + " in save( .., " + filename + " )");
        }
    }

    private File getFile(String filename) throws IOException {
        File dir = Util.getDir(context);
        return new File(dir, filename);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private static double median(LinkedList<Double> list) {
        try {

            // if list is too small, return NaN
            if (list.size() < 2) { return Double.NaN; }

            // make copy and sort
            Double[] sorted = null;
            synchronized (list) {
                sorted = list.toArray(new Double[] {});
            }
            Arrays.sort(sorted);

            // get middle element
            int middle = sorted.length / 2;
            if (sorted.length % 2 == 1) {
                return sorted[middle];
            } else {
                return (sorted[middle - 1] + sorted[middle]) / 2.0;
            }

        } catch (Exception e) {
            Log.e(TAG, e + " in median(List)", e);
        }
        return Double.NaN;
    }

    private int getHourOfDay() {
        long millis = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    private boolean isWeekDay() {
        long millis = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    private static String format(Location l) {
        try {
            return l.getLatitude() + "," + l.getLongitude();
        } catch (Exception e) {
            return "" + l;
        }
    }

}
