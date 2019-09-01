package com.tilmanification.quicklearn.sensors;

import android.content.Context;

import com.tilmanification.quicklearn.log.QLearnJsonLog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tilman
 *
 * Defines fields and methods all sensors must provide
 *
 */
public abstract class AbstractSensor {

    private List<com.tilmanification.quicklearn.sensors.QLearnSensorEventListener> listeners = new ArrayList<com.tilmanification.quicklearn.sensors.QLearnSensorEventListener>();

    public AbstractSensor() {
    }

    abstract public String getSensorName();

    abstract public boolean isAvailable(Context context);

    abstract public boolean isRunning();

    abstract public void start(Context context);

    abstract public void stop();

    abstract public String getLogKey();

    abstract public String getLogValue();

    abstract public JSONObject jsonify();

    abstract public void log();

    public void addSensorEventListener(com.tilmanification.quicklearn.sensors.QLearnSensorEventListener listener) {
        listeners.add(listener);
    }

    public void notifySensorEventListeners(Object o) {
        for (com.tilmanification.quicklearn.sensors.QLearnSensorEventListener listener : listeners) {
            listener.sensorEventReceived(o);
        }
    }

    public void writeStateToLog() {
        QLearnJsonLog.log(this);
    }

}
