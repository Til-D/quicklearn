package com.tilmanification.quicklearn;

/**
 * Created by tilman on 09/07/15.
 */
public interface QuickLearnPrefs {

    //Setup
    String PREF_DATABASE_ID				    	= "DbId";
    String PREF_SETUP_COMPLETE_COMMUNICATED  	= "SetupCompleteCommunicated";
    String PREF_CONSENT_GIVEN                   = "Consent given";
    String PREF_SETUP_FINISHED                  = "Setup Finished";
    String PREF_SETUP_FINISHED_TIMESTAMP        = "setup_finished_timestamp";
    String PREF_CONSENT_DATA_SHARING            = "Data sharing enabled";
    String PREF_NUMBER_OF_TIMES_APP_OPENED      = "AppOpenedCount";

    // Email and demographics
    String	PREF_EMAIL							= "Email";
    String  PREF_USER_REGISTERED                = "User registered";
    String	PREF_ASK_DEMOGRAPHICS				= "ask_demographics";
    String	PREF_AGE							= "age";
    String	PREF_GENDER_POS						= "gender_pos";
    String	PREF_GENDER 						= "gender";
    String	PREF_USAGE_POS						= "usage_pos";
    String	PREF_USAGE   						= "phone usage";

    // StudyManager
    String  PREF_STUDY_CONDITION                = "StudyCondition";
    String  PREF_DATE_STUDY_CONDITION_STARTED_MS  = "StudyConditionStartedMs";
    String  PREF_DATE_LAST_SURVEY               = "DateLastSurvey";
    String  PREF_DATE_LAST_SURVEY_MS            = "DateLastSurveyMs";
    String  PREF_REINITIALIZE_VOCABLUARY        = "ReInitVocab";    //triggers the creation of a new vocabulary (i.e. in case wordlists change), leitener_indices will be newly built
    String  PREF_LEITENER_INDICES               = "leitener_indices";
    String  PREF_WORDS_SEEN_FLASHCARD           = "words_seen_flashcard";
    String  PREF_WORDS_SEEN_MULTIPLE_CHOICE     = "words_seen_multiple_choice";
    String  PREF_CURRENT_WORD                   = "current_word";
    String  PREF_LAST_NOTIFICATION_POSTED       = "LastNotificationPosted";
    String  PREF_LAST_NOTIFICATION_POSTED_MS    = "LastNotificationPostedMS";
    String  PREF_SURVEY_COUNT                   = "SurveyCount";
    String  PREF_SURVEY_ATTEMPT                 = "SurveyAttempt";
    String  CURRENT_VERSION_CODE                = "CurrentVersionCode";

    //MISC
    String DATE_FORMAT                          = "dd.MM.yyyy, hh:mm"; //"EEE MMM dd kk:mm:ss zzz yyyy";

    String PREF_SHOW_INFO_DIALOG                = "SHOW_INFO_DIALOG"; // display the info dialog?
    String PREF_NOTIF_SHOWN                     = "NOTIF_SHOWN"; // was there ever a notification shown?

    //QuickLearnConst
    boolean	DEBUG_MODE					        = false;
    int QLEARN_MODE_NOTIFICATION                = 0;
    int QLEARN_MODE_APP                         = 1;
    boolean RESTART_SERVICE_AFTER_REBOOT	    = true;
    boolean NETWORKLOCATIONENFORCED             = false;


    //QLEARN Notifications
    String	NOTIF_POSTED    					= "notif_posted";
    String	NOTIF_POSTED_MILLIS					= "notif_posted_millis";
    String	NOTIF_IGNORED    					= "notif_ignored";
    String	NOTIF_CLICKED_MILLIS				= "notif_clicked_millis";
    String  QLEARN_SESSION_INDEX                = "session index";
    boolean NOTIFICATION_MAKE_NOISE             = false;

    //Boredom Prediction
    String	BOREDOM_PRED				        = "boredom_prediction";
    String  BOREDOM_PRED_CLASSIFIER		        = "boredom_prediction_classifier";
    String	BOREDOM_PRED_PROBABILITY	        = "boredom_prediction_probability";

    String APP_NAME                             = BuildConfig.APPLICATION_ID;

    String	LOG_SERVER_DEV				= "https://172.31.231.26:3000";
    String	LOG_SERVER_PROD				= "https://projects.hcilab.org:10933";
    String  LOG_SERVER                  = LOG_SERVER_PROD;
    String	PREF_UID				    = "uid";
    String	HTTPGETSALT				    = "434u23u4z5z47";

    boolean	LOG_USE_3G					= false;
    int		LOG_FLUSH_FREQ				= 1000;
    boolean	LOG_DELETE_WHEN_BROKEN		= true;
    boolean	LOG_INCLUDE_EMPTY_VALUES	= false;
    boolean AGE_SPECIFICATION_MANDATORY = false;
    boolean GENDER_SPECIFICATION_MANDATORY = false;

    String	CRLF						= "\r\n";
    String	SEPARATOR					= ",";

    String	ACCESSIBILITY_SERVICE		= "com.tilmanification.qicklearn/.QLearnAccessibilityService";

    boolean	DISCARD_ONGOING_NOTIFS		= true;

    // Language stuff
    String PREF_LANG_SOURCE             = "lang_source";
    String PREF_LANG_TARGET             = "lang_target";
    String PREF_LANG_PROFICIENCY        = "lang_proficiency";

    boolean LANGUAGE_SWITCH_ENABLED     = false;
}
