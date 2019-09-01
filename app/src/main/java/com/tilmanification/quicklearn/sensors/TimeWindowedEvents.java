package com.tilmanification.quicklearn.sensors;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

class TimeWindowedEvents {
    private static final String				TAG			= TimeWindowedEvents.class.getSimpleName();
    // Time window is 5 minutes
    private static final int				TIME_WINDOW	= 5 * 60 * 1000;

    private LinkedList<TimeWindowedEvent>	events;

    public TimeWindowedEvents() {
        this.events = new LinkedList<TimeWindowedEvent>();
    }

    public TimeWindowedEvents(TimeWindowedEvents copy) {
        this.events = new LinkedList<TimeWindowedEvent>(copy.events);
    }

    public TimeWindowedEvent addEvent(String name) {
        TimeWindowedEvent event = new TimeWindowedEvent(name);
        this.events.add(event);
        return event;
    }

    public TimeWindowedEvent getLastEvent() {
        try {
            return events.getLast();
        } catch (Exception e) {
            return null;
        }
    }

    public TimeWindowedEvent getFirstEvent() {
        try {
            return events.getFirst();
        } catch (Exception e) {
            return null;
        }
    }

    public int getNumEventsInTimeWindow() {
        return events.size();
    }

    public void endAllOngoingEvents() {
        for (TimeWindowedEvent event : events) {
            event.onEventEnded();
        }
    }

    public void removeOldEvents() {
        // determine how many app usage events are too old
        final long now = System.currentTimeMillis();
        int noToRemove = 0;
        for (TimeWindowedEvent event : events) {
            long appClosed = event.started + event.duration;
            if (now - appClosed > TIME_WINDOW) {
                Log.d(TAG, event.name + " too old --> to be removed");
                noToRemove++;
            }
        }
        // remove app usage events that are too old
        for (int i = 0; i < noToRemove; i++) {
            Log.i(TAG, "removing " + events.get(0));
            events.remove(0);
        }
    }

    public String mostCommonEventInTimeWindow(String defaultValue) {

        Map<String, Long> totalDuration = new LinkedHashMap<String, Long>();
        for (TimeWindowedEvent appUsageEvent : events) {
            String eventName = appUsageEvent.name;
            long duration = appUsageEvent.duration;
            if (totalDuration.containsKey(eventName)) {
                long dur = totalDuration.get(eventName);
                totalDuration.put(eventName, dur + duration);
            } else {
                totalDuration.put(eventName, duration);
            }
        }

        String mostCommonEvent = defaultValue;
        long duration = 0;
        Iterator<String> iter = totalDuration.keySet().iterator();
        while (iter.hasNext()) {
            String eventName = iter.next();
            long dur = totalDuration.get(eventName);

            Log.d(TAG, eventName + " lasting for " + dur + " seconds");

            if (dur > duration) {
                mostCommonEvent = eventName;
                duration = dur;
            }
        }

        Log.d(TAG, "Most common event in time window is " + mostCommonEvent);

        return mostCommonEvent;
    }
}

class TimeWindowedEvent {

    // ========================================================================
    // The actual event
    // ========================================================================

    public String	name;
    public long		started;
    public long		duration;

    public TimeWindowedEvent(String name) {
        this.name = name;
        this.started = System.currentTimeMillis();
    }

    void onEventEnded() {
        if (duration == 0) {
            duration = System.currentTimeMillis() - started;
        }
    }

    public String toString() {
        final long now = System.currentTimeMillis();

        if (duration == 0) {
            return name + " started " + ((now - started) / 1000.0) + " seconds ago and still ongoing";
        } else {
            return name
                    + " started "
                    + ((now - started) / 1000.0)
                    + " seconds ago. Lasted for "
                    + (duration / 1000.0)
                    + " seconds";
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (started ^ (started >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TimeWindowedEvent other = (TimeWindowedEvent) obj;
        if (started != other.started) return false;
        return true;
    }

}
