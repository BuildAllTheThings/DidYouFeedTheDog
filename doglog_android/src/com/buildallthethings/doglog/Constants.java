package com.buildallthethings.doglog;

public class Constants {
	// General purpose
	public static final String	PACKAGE										= Constants.class.getPackage().getName();
	public static final String	TAG											= PACKAGE;
	
	// Preferences
	public static final String	SHARED_PREFERENCES_NAME						= PACKAGE;
	public static final String	PREFS_GEOFENCE_HOME							= "geofence_home";
	public static final String	PREFS_USER_IS_HOME							= "status_user_home";
	public static final String	PREFS_SEND_NOTIFICATIONS					= "notifications_enabled";
	public static final String	PREFS_NOTIFY_FEEDING_TIME					= "notify_at_feeding_time";
	public static final String	PREFS_NOTIFY_FEEDING_TIME_OFFSET_MAGNITUDE	= "notify_at_feeding_time_offset_magnitude";
	public static final String	PREFS_NOTIFY_FEEDING_TIME_OFFSET_POLARITY	= "notify_at_feeding_time_offset_polarity";
	
	// Google Play services resolution
	public static final int		PLAY_CONNECTION_FAILURE_CODE				= 9000;
	
	// Intents
	public static final String	INTENT_ACTION_CONNECTION_ERROR				= PACKAGE + ".INTENT_ACTION_CONNECTION_ERROR";
	public static final String	INTENT_CATEGORY_LOCATION_SERVICES			= PACKAGE + ".INTENT_CATEGORY_LOCATION_SERVICES";
	public static final String	INTENT_EXTRA_CONNECTION_ERROR_CODE			= PACKAGE + ".INTENT_EXTRA_CONNECTION_ERROR_CODE";
	public static final String	INTENT_ACTION_GEOFENCE_ERROR				= PACKAGE + ".INTENT_ACTION_GEOFENCES_ERROR";
	public static final String	INTENT_EXTRA_GEOFENCE_STATUS				= PACKAGE + ".INTENT_EXTRA_GEOFENCE_STATUS";
	public static final String	INTENT_ACTION_GEOFENCE_TRANSITION			= PACKAGE + ".INTENT_ACTION_GEOFENCE_TRANSITION";
	public static final String	INTENT_EXTRA_GEOFENCE_TRANSITION			= PACKAGE + ".INTENT_EXTRA_GEOFENCE_TRANSITION";
	public static final String	INTENT_ACTION_VIEW_FEEDINGS					= PACKAGE + ".INTENT_ACTION_VIEW_FEEDINGS";
	public static final String	INTENT_ACTION_LOG_FEEDING					= PACKAGE + ".INTENT_ACTION_LOG_FEEDING";
	public static final String	INTENT_ACTION_SKIP_FEEDING					= PACKAGE + ".INTENT_ACTION_SKIP_FEEDING";
}
