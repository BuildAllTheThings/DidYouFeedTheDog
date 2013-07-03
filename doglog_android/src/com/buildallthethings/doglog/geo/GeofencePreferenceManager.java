package com.buildallthethings.doglog.geo;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Assuming Google Play services are available on this device, add and remove
 * geofences as requested.
 * 
 * To use a GeofenceManager, instantiate it with a reference to your main
 * activity. Call addGeofence and removeGeofence as desired.
 * 
 */
public class GeofencePreferenceManager extends LocationAware implements OnAddGeofencesResultListener, OnRemoveGeofencesResultListener {
	// Stores the PendingIntent used to send geofence transitions back to the
	// app
	private PendingIntent								locationServicesPendingIntent;
	
	protected List<GeofencePreference>					currentGeofencePreferences;
	protected List<GeofencePreference>					pendingGeofencePreferenceAdditions;
	protected List<String>								pendingGeofenceIdRemovals;
	protected List<GeofencePreference>					queuedGeofencePreferenceAdditions;
	protected List<String>								queuedGeofenceIdRemovals;
	
	protected List<OnGeofencePreferenceAddedListener>	onGeofencePreferenceAddedListeners;
	
	public GeofencePreferenceManager(Context context) {
		super(context);
		
		this.locationServicesPendingIntent = null;
		this.locationServices = null;
		
		this.currentGeofencePreferences = new ArrayList<GeofencePreference>();
		this.pendingGeofencePreferenceAdditions = new ArrayList<GeofencePreference>();
		this.pendingGeofenceIdRemovals = new ArrayList<String>();
		this.queuedGeofencePreferenceAdditions = new ArrayList<GeofencePreference>();
		this.queuedGeofenceIdRemovals = new ArrayList<String>();
		
		this.onGeofencePreferenceAddedListeners = new ArrayList<OnGeofencePreferenceAddedListener>();
	}
	
	/**
	 * Returns the current PendingIntent to the caller.
	 * 
	 * @return The PendingIntent used to create the current set of geofences
	 */
	public PendingIntent getRequestPendingIntent() {
		return createRequestPendingIntent();
	}
	
	/**
	 * Start adding geofences. Save the geofences, then start adding them by
	 * requesting a connection
	 * 
	 * @param geofences
	 *            A List of one or more geofences to add
	 */
	public void addGeofence(GeofencePreference geofencePreference) {
		// Save the geofences so that they can be sent to Location Services once
		// the connection is available.
		// Ensure the geofence is not already present.
		this.removeGeofencePreference(geofencePreference);
		this.queuedGeofencePreferenceAdditions.add(geofencePreference);
		
		// This will either start the process of getting a new location services
		// client, or do nothing if such a process is already underway.
		// When a client becomes available, the pending geofences will be
		// requested.
		this.getLocationClient();
	}
	
	public void removeGeofenceById(String id) {
		// Ensure this geofence exists somewhere
		boolean found = false;
		boolean connectionRequired = false;
		
		// Check the addition lists first.
		for (GeofencePreference g : this.queuedGeofencePreferenceAdditions) {
			if (id == g.getRequestId()) {
				// It hasn't even been requested yet. Just don't add it and
				// we're done.
				this.queuedGeofencePreferenceAdditions.remove(g);
				found = true;
				break;
			}
		}
		if (!found) {
			for (GeofencePreference g : this.pendingGeofencePreferenceAdditions) {
				if (id == g.getRequestId()) {
					// It is in the middle of being added, this is tricky
					// We will add it to the queue for removal. We guarantee to
					// process removals before additions, so when we move the
					// queued removal into the pending state, we will re-check
					// the lists to make sure we remove this one. If the
					// addition succeeded, it will be in currentGeofenceIds. If
					// the addition failed, it will be in requestedGeofences. In
					// either case, we will remove it then.
					this.queuedGeofenceIdRemovals.add(id);
					found = true;
					// We're pretty sure there's already a connection since
					// there are pending additions, but let's be safe.
					connectionRequired = true;
					break;
				}
			}
		}
		
		if (!found) {
			for (GeofencePreference g : this.currentGeofencePreferences) {
				if (id == g.getRequestId()) {
					// It exists. Add it to "queued for removal".
					// When the removal is complete, it will removed from
					// "current".
					this.queuedGeofenceIdRemovals.add(id);
					found = true;
					connectionRequired = true;
					break;
				}
			}
		}
		
		// Is it already being removed?
		if (!found) {
			for (String queuedGeofenceId : this.queuedGeofenceIdRemovals) {
				if (id == queuedGeofenceId) {
					// Already requested for removal
					found = true;
					break;
				}
			}
		}
		if (!found) {
			for (String pendingGeofenceId : this.pendingGeofenceIdRemovals) {
				if (id == pendingGeofenceId) {
					// Already pending removal return;
					found = true;
					break;
				}
			}
		}
		
		if (!found) {
			// If we made it this far, there is no record of that id.
			// This is probably an error.
			// TODO error out
		} else if (connectionRequired) {
			// This will either start the process of getting a new location
			// services
			// client, or do nothing if such a process is already underway.
			// When a client becomes available, the pending geofences will be
			// removed.
			this.getLocationClient();
		}
	}
	
	public void removeGeofencePreference(GeofencePreference geofence) {
		removeGeofenceById(geofence.getRequestId());
	}
	
	/**
	 * Once the connection is available, send a request to add the Geofences
	 */
	protected void continueProcessingGeofences() {
		// Always process queued removals before queued additions
		if (this.queuedGeofenceIdRemovals.size() > 0) {
			
			assert this.pendingGeofenceIdRemovals.size() == 0;
			
			// Send a request to add the current geofences
			for (String id : queuedGeofenceIdRemovals) {
				this.pendingGeofenceIdRemovals.add(id);
			}
			this.queuedGeofenceIdRemovals.clear();
			
			this.locationServices.removeGeofences(this.pendingGeofenceIdRemovals, this);
			
		} else if (this.queuedGeofencePreferenceAdditions.size() > 0) {
			// Get the PendingIntent that Location Services will issue when a
			// geofence transition occurs
			PendingIntent pendingIntent = createRequestPendingIntent();
			
			assert this.pendingGeofencePreferenceAdditions.size() == 0;
			
			// Send a request to add the current geofences
			List<Geofence> requestedGeofences = new ArrayList<Geofence>();
			for (GeofencePreference g : queuedGeofencePreferenceAdditions) {
				this.pendingGeofencePreferenceAdditions.add(g);
				requestedGeofences.add(g.toGeofence());
			}
			this.queuedGeofencePreferenceAdditions.clear();
			
			this.locationServices.addGeofences(requestedGeofences, pendingIntent, this);
			
		} else {
			
			// Disconnect the location client
			this.terminateLocationClient();
		}
	}
	
	/*
	 * Handle the result of adding the geofences
	 */
	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		if (LocationStatusCodes.SUCCESS == statusCode) {
			// Adding the geofences was successful
			// Upgrade them from "pending" to "current"
			Set<String> newGeofenceIds = new HashSet<String>();
			for (String id : geofenceRequestIds) {
				newGeofenceIds.add(id);
			}
			// Remove all upgraded geofences from "pending"
			Iterator<GeofencePreference> i = this.pendingGeofencePreferenceAdditions.iterator();
			while (i.hasNext()) {
				GeofencePreference g = i.next();
				if (newGeofenceIds.contains(g.getRequestId())) {
					i.remove();
					this.currentGeofencePreferences.add(g);
					for (OnGeofencePreferenceAddedListener listener : this.onGeofencePreferenceAddedListeners) {
						listener.onGeofencePreferenceAdded(g);
					}
				}
			}
		} else {
			// Adding the geofences failed
			String errorMessage = this.context.getString(R.string.add_geofences_result_failure, statusCode, Arrays.toString(geofenceRequestIds));
			Log.e(Constants.TAG, errorMessage);
			
			// Create an Intent to broadcast to the app
			// Create a broadcast Intent that notifies other components of
			// success
			// or failure
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(Constants.INTENT_ACTION_GEOFENCE_ERROR).addCategory(Constants.INTENT_CATEGORY_LOCATION_SERVICES).putExtra(Constants.INTENT_EXTRA_GEOFENCE_STATUS, errorMessage);
			// Broadcast whichever result occurred
			LocalBroadcastManager.getInstance(this.context).sendBroadcast(broadcastIntent);
		}
		
		this.continueProcessingGeofences();
	}
	
	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
		// We don't remove geofences by intent.
	}
	
	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
		// If removing the geocodes was successful
		if (LocationStatusCodes.SUCCESS == statusCode) {
			// Removing the geofences was successful
			// Remove them from both "current" and "pending removal".
			for (String id : geofenceRequestIds) {
				this.pendingGeofenceIdRemovals.remove(id);
				this.currentGeofencePreferences.remove(id);
			}
		} else {
			// If removing the geofences failed, create a message containing the
			// error code and the list of geofence IDs you tried to remove
			String errorMessage = this.context.getString(R.string.remove_geofences_id_failure, statusCode, Arrays.toString(geofenceRequestIds));
			
			// Log an error
			Log.e(Constants.TAG, errorMessage);
			
			// Create an Intent to broadcast to the app
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(Constants.INTENT_ACTION_GEOFENCE_ERROR).addCategory(Constants.INTENT_CATEGORY_LOCATION_SERVICES).putExtra(Constants.INTENT_EXTRA_GEOFENCE_STATUS, errorMessage);
			LocalBroadcastManager.getInstance(this.context).sendBroadcast(broadcastIntent);
		}
		
		this.continueProcessingGeofences();
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		super.onConnected(bundle);
		
		// Continue adding the geofences
		continueProcessingGeofences();
	}
	
	/**
	 * Get a PendingIntent to send with the request to add Geofences. Location
	 * Services issues the Intent inside this PendingIntent whenever a geofence
	 * transition occurs for the current list of geofences.
	 * 
	 * @return A PendingIntent for the IntentService that handles geofence
	 *         transitions.
	 */
	private PendingIntent createRequestPendingIntent() {
		if (this.locationServicesPendingIntent == null) {
			// Create an Intent pointing to the IntentService
			Intent intent = new Intent(this.context, GeofenceMonitoringService.class);
			
			/*
			 * Return a PendingIntent to start the IntentService. Always create
			 * a PendingIntent sent to Location Services with
			 * FLAG_UPDATE_CURRENT, so that sending the PendingIntent again
			 * updates the original. Otherwise, Location Services can't match
			 * the PendingIntent to requests made with it.
			 */
			this.locationServicesPendingIntent = PendingIntent.getService(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		
		// Return the existing intent
		return this.locationServicesPendingIntent;
	}
	
	public void registerOnGeofencePreferenceAddedListener(OnGeofencePreferenceAddedListener listener) {
		this.onGeofencePreferenceAddedListeners.add(listener);
	}
}
