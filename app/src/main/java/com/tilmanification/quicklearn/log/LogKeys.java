package com.tilmanification.quicklearn.log;

public interface LogKeys {

    // ========================================================================
    // LogKeys
    // ========================================================================

    String	TRUE						= "1";
    String	FALSE						= "0";
    String	EMPTY						= "";

    String	KEY_VC						= "vc";
    String	KEY_T						= "t";
    String	KEY_UPTIME_SINCE_START_S	= "up";

    String	FIRST_RUN					= "first_run_1";

    // Device-related
    String	UID							= "uid";
    String	INSTALL_DATE_EN				= "install_time_en";
    String	INSTALL_DATE_DE				= "install_time_de";
    String	INSTALL_TIME_MS				= "install_time_ms";
    String	LOG_VERSION					= "log_version";
    String	LOG_RELEASE_DATE			= "log_release_date";
    String	APP_VERSION					= "app_version";
    String	TIMEZONE					= "timezone";
    String	LOCALE						= "locale";
    String	MODEL						= "model";
    String	RELEASE						= "release";

    String	APP_STATUS					= "sts";
    /** After onDestroy() */
    String	APP_STATUS_DESTROYED		= "destroyed";
    /** Between onCreate() and onDestroy() */
    String	APP_STATUS_ALIVE			= "alive";
    /** Between onStart() and onStop() */
    String	APP_STATUS_VISIBLE			= "visible";
    /** Between onResume() and onPause() */
    String	APP_STATUS_FOREGROUND		= "foreground";

    // ========================================================================
    // SensorLogger
    // ========================================================================

    String	KEY_TIME_STAMP				= "time";
    String	KEY_DATE					= "date";


    String	KEY_LOC_TIME				= "ltime";
    String	KEY_LOC_LAT					= "lat";
    String	KEY_LOC_LON					= "lon";
    String	KEY_LOC_ALT					= "alt";
    String	KEY_LOC_ACCURACY			= "lacc";
    String	KEY_LOC_SUFF_ACC			= "slacc";
    String	KEY_LOC_PROVIDER			= "lprv";
    String	KEY_LOC_SPEED				= "lspd";
    String	KEY_LOC_BEARING				= "lbng";

    /** Proximity sensor distance measured in centimeters */
    String	KEY_PROX					= "prox";
    /** Ambient light level in SI lux units */
    String	KEY_LIGHT_LEVEL				= "llvl";
    /**
     * Azimuth, angle between the magnetic north direction and the y-axis,
     * around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West
     */
    String	KEY_AZIMUTH					= "azth";
    /**
     * Pitch, rotation around x-axis (-180 to 180), with positive values when
     * the z-axis moves toward the y-axis.
     */
    String	KEY_PITCH					= "ptch";
    /**
     * Roll, rotation around y-axis (-90 to 90), with positive values when the
     * x-axis moves toward the z-axis.
     */
    String	KEY_ROLL					= "roll";
    String	KEY_ORIENTATION_ACCURACY	= "oacc";

    String	KEY_BATTERY_LEVEL			= "btlvl";
    String	KEY_BATTERY_CHANGE_PER_HOUR	= "btcph";
}

