package com.buildallthethings.doglog.geo;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.HomeManager;
import com.buildallthethings.doglog.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GeofenceMonitoringService extends IntentService {
	
	protected final HomeManager	homeManager;
	
	public GeofenceMonitoringService() {
		super("GeofenceMonitoringService");
		
		this.homeManager = HomeManager.getInstance(this);
	}
	
	/**
	 * Handles incoming intents which represent geofence transitions. If errors
	 * have occurred, these are broadcasted to the activity for rendering to the
	 * user. If a transition has occurred, this is broadcasted to the activity
	 * to handle as appropriate.
	 * 
	 * @param intent
	 *            The Intent sent by Location Services. This Intent is provided
	 *            to Location Services (inside a PendingIntent) when you call
	 *            addGeofence()
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		// Create a local broadcast Intent
		Intent broadcastIntent = new Intent();
		broadcastIntent.addCategory(Constants.INTENT_CATEGORY_LOCATION_SERVICES);
		
		// First check for errors
		if (LocationClient.hasError(intent)) {
			// Get the error message
			int errorCode = LocationClient.getErrorCode(intent);
			String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);
			
			// Log the error
			Log.e(Constants.TAG, getString(R.string.geofence_transition_error_detail, errorMessage));
			
			// Set the action and error message for the broadcast intent
			broadcastIntent.setAction(Constants.INTENT_ACTION_GEOFENCE_ERROR).putExtra(Constants.INTENT_EXTRA_GEOFENCE_STATUS, errorMessage);
			
		} else {
			// there's no error, get the transition type and broadcast it.
			
			// Get the type of transition (entry or exit)
			int transition = LocationClient.getGeofenceTransition(intent);
			
			// Test that a valid transition was reported
			if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
				broadcastIntent.setAction(Constants.INTENT_ACTION_GEOFENCE_TRANSITION).putExtra(Constants.INTENT_EXTRA_GEOFENCE_TRANSITION, transition);
				
				// An invalid transition was reported
			} else {
				// Always log as an error
				Log.e(Constants.TAG, getString(R.string.geofence_transition_invalid_type, transition));
			}
		}
		
		if (broadcastIntent.getAction() != null) {
			// Broadcast the error *locally* to other components in this app
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		}
	}
}
