package com.tilmanification.quicklearn.sensors;

public interface NotificationListener {

    void onNotificationPosted(String packageName);

    void onNotificationRemoved(String packageName);

}
