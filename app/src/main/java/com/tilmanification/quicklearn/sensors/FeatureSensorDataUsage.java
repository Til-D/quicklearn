package com.tilmanification.quicklearn.sensors;

import android.content.Context;
import android.net.TrafficStats;

import com.tilmanification.quicklearn.log.QLearnJsonLog;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

class FeatureSensorDataUsage extends FeatureSensor {

    // ========================================================================
    // Fields
    // ========================================================================

    public final String			TAG			= FeatureSensorDataUsage.class.getSimpleName();
    private static final long	LOG_IMPULSE	= 1000 * 60 * 5; //every 5 minutes

    private Timer				timer;
    DataUsage					mobileRxUsage;
    DataUsage					mobileTxUsage;

    // ========================================================================
    // Sensor start on stop
    // ========================================================================

    public FeatureSensorDataUsage(Context context, Features features) {
        super(context, features);
        mobileRxUsage = new DataUsage(TrafficStats.getTotalRxBytes());
        mobileTxUsage = new DataUsage(TrafficStats.getTotalTxBytes());
    }

    public void onStart() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                updateDataUsage();
            }

        }, LOG_IMPULSE, LOG_IMPULSE);
    }

    public void onStop() {
        timer.cancel();
    }

    // ========================================================================
    // Method
    // ========================================================================

    private void updateDataUsage() {
        long totalRxBytes = TrafficStats.getTotalRxBytes();
        long totalTxBytes = TrafficStats.getTotalTxBytes();

        long totalRxInTimeWindow = mobileRxUsage.updateDataUsage(totalRxBytes);
        long totalTxInTimeWindow = mobileTxUsage.updateDataUsage(totalTxBytes);

        // Log.i(TAG, "totalRxBytes: " + totalRxBytes + ", totalRxInTimeWindow: " + totalRxInTimeWindow);

        QLearnJsonLog.onUpdateDataUsage(LOG_IMPULSE, Long.toString(totalRxInTimeWindow), Long.toString(totalTxInTimeWindow));

    }

    class DataUsage {
        private LinkedList<Long>	bytesList	= new LinkedList<Long>();
        private long				total;

        DataUsage(long total) {
            this.total = total;
        }

        public long updateDataUsage(long newTotal) {

            // compute difference since last check
            long bytesAdded = newTotal - this.total;

            // update current total byte count
            this.total = newTotal;

            // add number of added bytes too list
            bytesList.add(bytesAdded);

            // delete those that are older than the time window
            while (bytesList.size() * LOG_IMPULSE > Features.TIME_WINDOW_MS) {
                bytesList.removeFirst();
            }

            // compute sum of data activity in last time window
            long bytesInTimeWindow = 0;
            for (long b : bytesList) {
                bytesInTimeWindow += b;
            }

            return bytesInTimeWindow;
        }
    }

}
