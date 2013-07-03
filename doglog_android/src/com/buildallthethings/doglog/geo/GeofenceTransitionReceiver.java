package com.buildallthethings.doglog.geo;

import java.util.ArrayList;
import java.util.List;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class GeofenceTransitionReceiver extends BroadcastReceiver {
	
	protected List<GeofenceTransitionListener>	onGeofenceTransitionListeners;
	
	public GeofenceTransitionReceiver() {
		this.onGeofenceTransitionListeners = new ArrayList<GeofenceTransitionListener>();
	}
	
	/*
	 * Define the required method for broadcast receivers This method is invoked
	 * when a broadcast Intent triggers the receiver
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Check the action code and determine what to do
		String action = intent.getAction();
		
		if (action.equals(Constants.INTENT_ACTION_GEOFENCE_ERROR)) {
			// Intent contains information about errors in adding or removing
			// geofences
			handleGeofenceError(context, intent);
			
		} else if (action.equals(Constants.INTENT_ACTION_GEOFENCE_TRANSITION)) {
			int transition = intent.getIntExtra(Constants.INTENT_EXTRA_GEOFENCE_TRANSITION, -1);
			List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
			if (geofences != null) {
				for (Geofence g : geofences) {
					for (GeofenceTransitionListener listener : this.onGeofenceTransitionListeners) {
						listener.onGeofenceTransition(g, transition);
					}
				}
			}
		} else {
			// The Intent contained an invalid action
			Log.e(Constants.TAG, context.getString(R.string.invalid_action_detail, action));
			Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Report addition or removal errors to the UI, using a Toast
	 * 
	 * @param intent
	 *            A broadcast Intent sent by ReceiveTransitionsIntentService
	 */
	private void handleGeofenceError(Context context, Intent intent) {
		String msg = intent.getStringExtra(Constants.INTENT_EXTRA_GEOFENCE_STATUS);
		Log.e(Constants.TAG, msg);
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	public void registerOnGeofenceTransitionListener(GeofenceTransitionListener listener) {
		this.onGeofenceTransitionListeners.add(listener);
	}
}
