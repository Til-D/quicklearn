package com.tilmanification.quicklearn.log;

public interface QlearnKeys extends LogKeys {

    String	INST								= "installations";

    String	KEY_PID								= "PID";

    // QLearn
    String	QLEARN_NOTIF_POSTED_MS				= "NotifPostedMs";
    String	QLEARN_NOTIF_POSTED_DATE			= "NotifPostedDate";
    String	QLEARN_NOTIF_CANCELLED_AFTER_MS		= "NotifCancelledAfterMS";
    String	QLEARN_NOTIF_DISMISSED_AFTER_MS		= "NotifDismissedAfterMS";
    String	QLEARN_NOTIF_DISMISSED_PENDING_COUNT = "NotifDismissedPendingCount";
    String	QLEARN_NOTIF_FIRST_INTERACTION      = "NotifFirstInteraction";
    String	QLEARN_IGNORED						= "NotifIgnored";
    String	QLEARN_DISMISSED                    = "NotifDismissed";
    String  QLEARN_SESSION_FINISHED              = "SessionFinished";
    String  QLEARN_WORD_REVIEWED                = "WordReviewed";
    String  QLEARN_DATE_SETUP_COMPLETED         = "DateSetupCompleted";
    String  QLEARN_DATE_LAST_LOG_UPLOAD         = "DateLastLogUpload";
    String  QLEARN_DATE_LAST_VOCAB_EXTENSION    = "DateLastVocabExtension";
    String  QLEARN_VOCAB_EXTENSION_TRIGGERED    = "VocabExtensionTriggered";
    String  VOCAB_EXTENSION_PREV_SIZE           = "previousSize";
    String  VOCAB_EXTENSION_NEW_SIZE            = "newSize";
    String  QLEARN_DATA_SHARING_ENABLED         = "DataSharingEnabled";

    String  QLEARN_APP_LAUNCH                   = "AppLaunch";
    String	QLEARN_MODE							= "Mode"; //in notification 0, in app 1
    String	QLEARN_CONDITION					= "Condition";

    String	QLEARN_CONDITION_SWITCH				= "ConditionSwitch";
    String	QLEARN_CONDITION_OLD_COND			= "OldCondition";
    String	QLEARN_CONDITION_NEW_COND			= "NewCondition";
    String	QLEARN_CONDITION_DAYS_PASSED		= "ConditionDaysPassed";
    String	QLEARN_CONDITIONAL_WORD_LIST_RESET	= "ConditionalWordListReset";

    String  QLEARN_NUMBER_OF_WORDS_DONE         = "NumberOfWordsDone";
    String  QLEARN_NUMBER_OF_SETS_DONE          = "NumberOfSetsDone";

    String WORD_KEY                             = "Key";
    String WORD_SRC_LANGUAGE                    = "SrcLanguage";
    String WORD_TARGET_LANGUAGE                 = "TargetLanguage";
    String WORD_SEEN_BEFORE                     = "SeenBefore";
    String WORD_GUESSED_CORRECTLY               = "GuessedCorrectly";

    String	QLEARN_NOTIFICATION_CLICKED_AFTER_SEC	= "NotifClickedAfterSec";
    String QLEARN_NOTIFICATION_INTERACTION      = "NotificationInteraction";

    //Survey
    String  QLEARN_SURVEY_RESPONSE              = "ESMProbeResponse";
    String  QLEARN_SURVEY_TRIGGERED             = "ESMProbeTriggered";
    String  QLEARN_SURVEY_DISMISSED             = "ESMProbeDismissed";
    String  SURVEY_SOCIAL_CONTEXT               = "SocialContext";
    String  SURVEY_LOCATION                     = "Location";
    String  SURVEY_TIMING_OPPORTUNE             = "TimingOpportune";

    //Features
    String FEATURES_SNAPSHOT                    = "Features";
    String FEATURE_APP_IN_FOCUS                 = "AppInFocus";
    String FEATURE_APP_CAT_IN_FOCUS             = "AppCatInFocus";
    String FEATURE_DATA_USAGE                   = "DataUsage";
    String FEATURE_BYTES_RECEIVED               = "BytesReceived";
    String FEATURE_BYTES_TRANSMITTED            = "BytesTransmitted";
    String FEATURE_LOG_IMPULSE_IN_MS            = "LogImpulseInMs";
    String FEATURE_CHARGING                     = "Charging";
    String FEATURE_DAY_OF_WEEK                  = "DayOfWeek";
    String FEATURE_HOUR_OF_DAY                  = "HourOfDay";
    String FEATURE_LAST_NOTIF                   = "LastNotif";
    String FEATURE_LAST_NOTIF_CAT               = "LastNotifCat";
    String FEATURE_MOST_USED_APP                = "MostUsedApp";
    String FEATURE_MOST_USED_APP_CAT            = "MostUsedAppCat";
    String FEATURE_NUM_APPS                     = "NumApps";
    String FEATURE_NUM_NOTIFS                   = "NumNotifs";
    String FEATURE_NUM_UNLOCKS                  = "NumUnlocks";
    String FEATURE_PROXIMITY                    = "Proximity";
    String FEATURE_RINGER_MODE                  = "RingerMode";
    String FEATURE_SCREEN_ORIENTATION_CHANGES   = "ScreenOrientationChanges";
    String FEATURE_SEMANTIC_LOCATION            = "SemanticLocation";
    String FEATURE_TIME_LAST_SMS_READ           = "TimeLastSMSRead";
    String FEATURE_TIME_LAST_SMS_SENT           = "TimeLastSMSSent";
    String FEATURE_TIME_LAST_SMS_RECEIVED       = "TimeLastSMSReceived";
    String FEATURE_TIME_LAST_INCOMING_PHONECALL = "TimeLastIncomingPhonecall";
    String FEATURE_TIME_LAST_NOTIF              = "TimeLastNotif";
    String FEATURE_TIME_LAST_NOTIF_CENTER_ACCESS    = "TimeLastNotifCenterAccess";
    String FEATURE_TIME_LAST_OUTGOING_PHONECALL = "TimeLastOutgoingPhonecall";
    String FEATURE_TIME_SPENT_IN_COMMUNICATION_APPS = "TimeInCommApps";

    // Wakeup - sleep
    String	AWAKE								= "Awake";
    String	WAKE_UP								= "Wake Up";
    String	SLEEP								= "Sleep";

    // Status
    String	SENSOR_ID							= "SensId";
    String	SENSOR_VALUE						= "SensVal";
    String	SCREEN_ON							= "ScrOn";
    String	PHONE_UNLOCKED						= "Unlocked";
    String	PROXIMITY							= "Prox";
    String	RINGER_MODE							= "Ringer";
    String	RINGER_MODE_SILENT					= "Slnt";
    String	RINGER_MODE_VIBRATE					= "Vbrt";
    String	RINGER_MODE_NORMAL					= "Nrml";
    String	RINGER_MODE_UNDEFINED				= "Undf";
    String	CHARGING_CHANGED					= "ChargingChanged";
    String	ACCELERATION						= "Acc";
    String	AIRPLANE_MODE						= "Airpln";
    String	GYROSCOPE							= "Gyro";
    String	MAGNETOMETER						= "Mgnet";
    String	DEVICE_ORIENTATION					= "DevOrient";
    String	SCREEN_ORIENTATION					= "ScrOrient";
    String	ORIENTATION_LANDSCAPE				= "Lndscp";
    String	ORIENTATION_PORTRAIT				= "Prtrt";
    String	ORIENTATION_UNDEFINED				= "Undf";
    String	LOCATION							= "Loc";
    String	AUDIOJACK							= "Audio";
    String	BLUETOOTH							= "Bt";
    String	SPEAKERPHONE						= "Spkr";
    String	WIREDHEADSET						= "Headph";
    String	AUDIO_OUTPUT_NONE					= "None";
    String	CELL_TOWER_INFO						= "CllTwrInfo";
    String  PHONE_REBOOT                        = "PhoneReboot";

    // Applications
    String	PACKAGE								= "Pkg";
    String  APP_UPDATE                          = "ApplicationUpdate";
    String  INSTALLED_APPS                      = "InstalledApps";

    // Notifications
    String	OPEN_NOTIFICATION_CENTER			= "NotifsViewed";
    String	NOTIFS_CLEARED						= "NotifsCleared";
    String	CLEAR_NOTIFICATION					= "NotifCleared";
    String	NOTIFICATION						= "Notif";
    String	NOTIF_ID							= "NotifID";
    String	NOTIF_ACTION						= "NotifActn";
    String	NOTIF_POSTED        				= "NotifPosted";
    String	NOTIF_POSTED_PACKAGE				= "NotifPostedPackage";
    String	NOTIF_REMOVED_PACKAGE				= "NotifRemoved";
    String	NOTIF_POSTED_CLEARABLE				= "NotifClearable";
    String	NOTIF_CONTENT						= "NotifContent";
    String	NOTIF_CONTENT_HASH					= "NotifContentHash";
    String	NOTIF_REMOVED						= "NotifRemoved";
    String	NOTIF_DISMISSED						= "NotifDismissed";
    String	PENDING_NOTIF_COUNT					= "NotifCount";
    String	NOTIF_ACC							= "NotifAcc";
    String	NOTIF_DRAWER_ACC    				= "NotifDrawerAcc";

    // SMS
    String	SMS									= "Sms";
    String	SMS_EVENT							= "SmsEvt";
    String	SMS_ID								= "SmsId";
    String	SMS_ADDR							= "SmsAdd";
    String	SMS_LENGTH							= "SmsLen";
    String	SMS_PERSON							= "SmsPersId";
    String	SMS_PROTOCOL						= "SmsPrtcl";

    // Phone Calls
    String	PHONE_CALL_STATUS					= "PhnStat";
    String	PHONE_CALL_INCOMING					= "PhnIn";
    String	PHONE_CALL_OUTGOING					= "PhnOut";
    String	PHONE_CALLING						= "PhnCalling";
    String	PHONE_SUCCESS						= "PhnSuccess";
    String	PHONE_NUMBER						= "PhnNum";

    // Data Activity
    String	DATA_ACTIVITY						= "DataAct";
    String	DATA_BYTES_RECEIVED_TOTAL			= "DataByteRcvdTotal";
    String	DATA_BYTES_TRANSMITTED_TOTAL		= "DataByteTransTotal";
    String	DATA_PACKETS_RECEIVED_TOTAL			= "DataPcktRcvdTotal";
    String	DATA_PACKETS_TRANSMITTED_TOTAL		= "DataPcktTransTotal";
    String	DATA_BYTES_RECEIVED_MOBILE			= "DataByteRcvdMble";
    String	DATA_BYTES_TRANSMITTED_MOBILE		= "DataByteTransMble";
    String	DATA_PACKTES_RECEIVED_MOBILE		= "DataPcktRcvdMble";
    String	DATA_PACKETS_TRANSMITTED_MOBILE		= "DataPcktTransMble";

    // WIFI
    String	WIFI								= "WiFi";
    String	WIFI_CONNECTED						= "WiFiCon";
    String	WIFI_SSID							= "WiFiSsid";
    String	WIFI_BSSID							= "WiFiBssid";
    String	WIFI_RSSI							= "WiFiRssi";
    String	WIFI_LINK_SPEED						= "WiFiSpeed";
    String	WIFI_NED_ID							= "WiFiNetId";
    String	WIFI_IP								= "WiFiIp";

    // Ambient Noise Level
    String	AMBIENT_NOISE_LEVEL					= "Noise";

    // Battery Status Level
    String	BATTERY_STATUS						= "BtryStat";
    String	BATTERY_CHARGE_PLUG					= "BtryChrgPlug";
    String	BATTERY_USB_CHARGE					= "BtryChrgUsb";
    String	BATTERY_AC_CHARGE					= "BtryChrgAc";
    String	BATTERY_PLUGGED						= "BtryPlugged";
    String	BATTERY_PREV_LEVEL					= "BtryPrevLev";
    String	BATTERY_PREV_CURRENT_LEVEL			= "BtryCurrentLev";
    String	BATTERY_SCALE						= "BtryScle";
    String	BATTERY_PERCENTAGE					= "BtryPct";

    // Battery Drain
    String	BATTERY_DRAIN						= "BtryDrain";

    // CELL TOWER INFO
    String	CELL_TOWER_NEIGHBORS				= "CTNeighbrs";
    String	CELL_TOWER_ID						= "CTId";
    String	CELL_TOWER_NETWORK_TYPE				= "CTNetType";
    String	CELL_TOWER_RSSI						= "CTRssi";
    String	CELL_TOWER_LAC						= "CTLac";
    String	CELL_TOWER_LOCATION					= "CTLoc";
    String	CELL_TOWER_PSC						= "CTPsc";
    String	CELL_TOWER_INFOS					= "CTInfos";
    String	CELL_TOWER_REGISTERED				= "CTRegistered";
    String	CELL_TOWER_MSC						= "CTMsc";
    String	CELL_TOWER_SIGNAL_STRENGTH			= "CTSigStrngth";
    String	CELL_TOWER_CDMA_DBM					= "CTCdmaDbm";
    String	CELL_TOWER_CDMA_ECIO				= "CTCdmaEcio";
    String	CELL_TOWER_EVDO_DBM					= "CTEvdoDbm";
    String	CELL_TOWER_EVDO_ECIO				= "CTEvdoEcio";
    String	CELL_TOWER_EVDO_SNR					= "CTEvdoSnr";
    String	CELL_TOWER_GSM_BIT_ERROR_RATE		= "CTGmsBitErr";
    String	CELL_TOWER_GSM_SIGNAL_STRENGTH		= "CTGmsSigStrngth";
    String	CELL_TOWER_DATA_STATE				= "CTDataStat";
    String	CELL_TOWER_NETWORK_OPERATOR			= "CTNetOp";
    String	CELL_TOWER_SIM_OPERATOR				= "CTSimOp";
    String	CELL_TOWER_NETWORK_ROAMING			= "CTNetRoam";

    // DEVICE ORIENTATION
    String	ORIENTATION_AZIMUTH					= "OrAz";
    String	ORIENTATION_PITCH					= "OrPitch";
    String	ORIENTATION_ROLL					= "OrRoll";

    // GYRO
    String	GYRO_RELIABLE						= "GyrRel";
    String	GYRO_ROTATION_ACCELERATION			= "GyrAcc";

    // LOCATION
    String	LOCATION_PROVIDER					= "LocProv";
    String	LOCATION_LAT						= "LocLat";
    String	LOCATION_LON						= "LocLon";
    String	LOCATION_ACC						= "LocAcc";

    // RINGER MODE
    String	RINGER_MODUS						= "RingMod";
    String	RINGER_VOLUME						= "RingVol";
    String	RINGER_ALARM_VOLUME					= "RingVolAlarm";
    String	RINGER_MUSIC_VOLUME					= "RingVolMusic";
    String	RINGER_SYSTEM_VOLUME				= "RingVolSys";

    // SERVER CONNECTION CONFIG
    String	APPLICATION_ID						= "AppId";
    String	APPLICATION_UID						= "AppUid";
    String	APPLICATION_DATA					= "AppData";
    String	APPLICATION_CS						= "AppCs";						// checksum

    // TEMPERATURE
    String	TEMPERATURE							= "Temp";
    String	TEMPERATURE_DEVICE					= "DevTemp";
    String	TEMPERATURE_ENV						= "EnvTemp";

    // HUMIDITY
    String	HUMIDITY							= "Hum";

    // AIR PRESSURE
    String	AIR_PRESSURE						= "APress";

    // LIGHT
    String	LIGHT								= "Light";

    // GRAVITY
    String	GRAVITY								= "Grvty";
    String	GRAV_X								= "GrvX";
    String	GRAV_Y								= "GrvY";
    String	GRAV_Z								= "GrvZ";
}
