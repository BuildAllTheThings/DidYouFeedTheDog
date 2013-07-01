package com.buildallthethings.doglog;

import com.buildallthethings.doglog.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
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
public class GeofenceManager extends LocationAware implements OnAddGeofencesResultListener, OnRemoveGeofencesResultListener {
	
	// Storage for a reference to the calling client
	private final Activity		activity;
	
	// Stores the PendingIntent used to send geofence transitions back to the
	// app
	private PendingIntent		locationServicesPendingIntent;
	
	protected List<String>		currentGeofenceIds;
	protected List<Geofence>	pendingGeofenceAdditions;
	protected List<String>		pendingGeofenceIdRemovals;
	protected List<Geofence>	queuedGeofenceAdditions;
	protected List<String>		queuedGeofenceIdRemovals;
	
	public GeofenceManager(Activity activity) {
		super(activity);
		
		this.activity = activity;
		
		this.locationServicesPendingIntent = null;
		this.locationServices = null;
		
		this.currentGeofenceIds = new ArrayList<String>();
		this.pendingGeofenceAdditions = new ArrayList<Geofence>();
		this.pendingGeofenceIdRemovals = new ArrayList<String>();
		this.queuedGeofenceAdditions = new ArrayList<Geofence>();
		this.queuedGeofenceIdRemovals = new ArrayList<String>();
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
	public void addGeofence(Geofence geofence) {
		// Save the geofences so that they can be sent to Location Services once
		// the connection is available.
		// Ensure the geofence is not already present.
		this.removeGeofence(geofence);
		this.queuedGeofenceAdditions.add(geofence);
		
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
		for (Geofence g : this.queuedGeofenceAdditions) {
			if (id == g.getRequestId()) {
				// It hasn't even been requested yet. Just don't add it and
				// we're done.
				this.queuedGeofenceAdditions.remove(g);
				found = true;
				break;
			}
		}
		if (!found) {
			for (Geofence g : this.pendingGeofenceAdditions) {
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
			for (String currentGeofenceId : this.currentGeofenceIds) {
				if (id == currentGeofenceId) {
					// It exists. Add it to "queued for removal".
					// When the removal is complete, it will removed from
					// "current".
					this.queuedGeofenceIdRemovals.add(currentGeofenceId);
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
	
	public void removeGeofence(Geofence geofence) {
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
			
		} else if (this.queuedGeofenceAdditions.size() > 0) {
			// Get the PendingIntent that Location Services will issue when a
			// geofence transition occurs
			PendingIntent pendingIntent = createRequestPendingIntent();
			
			assert this.pendingGeofenceAdditions.size() == 0;
			
			// Send a request to add the current geofences
			for (Geofence g : queuedGeofenceAdditions) {
				this.pendingGeofenceAdditions.add(g);
			}
			this.queuedGeofenceAdditions.clear();
			
			this.locationServices.addGeofences(this.pendingGeofenceAdditions, pendingIntent, this);
			
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
		
		// Create a broadcast Intent that notifies other components of success
		// or failure
		Intent broadcastIntent = new Intent();
		
		// Temp storage for messages
		String msg;
		
		if (LocationStatusCodes.SUCCESS == statusCode) {
			// Adding the geofences was successful
			// Upgrade them from "pending" to "current"
			Set<String> newGeofenceIds = new HashSet<String>();
			for (String id : geofenceRequestIds) {
				newGeofenceIds.add(id);
				this.currentGeofenceIds.add(id);
			}
			// Remove all upgraded geofences from "pending"
			Iterator<Geofence> i = this.pendingGeofenceAdditions.iterator();
			while (i.hasNext()) {
				Geofence g = i.next();
				if (newGeofenceIds.contains(g.getRequestId())) {
					i.remove();
				}
			}
			
			// Create a message containing all the geofence IDs added.
			msg = this.activity.getString(R.string.add_geofences_result_success, Arrays.toString(geofenceRequestIds));
			
			// In debug mode, log the result
			Log.d(GeofenceUtils.APPTAG, msg);
			
			// Create an Intent to broadcast to the app
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_ADDED).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
			
		} else {
			// Adding the geofences failed
			/*
			 * Create a message containing the error code and the list of
			 * geofence IDs you tried to add
			 */
			msg = this.activity.getString(R.string.add_geofences_result_failure, statusCode, Arrays.toString(geofenceRequestIds));
			
			// Log an error
			Log.e(GeofenceUtils.APPTAG, msg);
			
			// Create an Intent to broadcast to the app
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
		}
		
		// Broadcast whichever result occurred
		LocalBroadcastManager.getInstance(this.activity).sendBroadcast(broadcastIntent);
		
		this.continueProcessingGeofences();
	}
	
	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
		// We don't remove geofences by intent.
	}
	
	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
		// Create a broadcast Intent that notifies other components of success
		// or failure
		Intent broadcastIntent = new Intent();
		
		// Temp storage for messages
		String msg;
		
		// If removing the geocodes was successful
		if (LocationStatusCodes.SUCCESS == statusCode) {
			
			// Removing the geofences was successful
			// Remove them from both "current" and "pending removal".
			for (String id : geofenceRequestIds) {
				this.pendingGeofenceIdRemovals.remove(id);
				this.currentGeofenceIds.remove(id);
			}
			
			// Create a message containing all the geofence IDs removed.
			msg = this.activity.getString(R.string.remove_geofences_id_success, Arrays.toString(geofenceRequestIds));
			
			// In debug mode, log the result
			Log.d(GeofenceUtils.APPTAG, msg);
			
			// Create an Intent to broadcast to the app
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
			
		} else {
			// If removing the geocodes failed
			
			/*
			 * Create a message containing the error code and the list of
			 * geofence IDs you tried to remove
			 */
			msg = this.activity.getString(R.string.remove_geofences_id_failure, statusCode, Arrays.toString(geofenceRequestIds));
			
			// Log an error
			Log.e(GeofenceUtils.APPTAG, msg);
			
			// Create an Intent to broadcast to the app
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
		}
		
		// Broadcast whichever result occurred
		LocalBroadcastManager.getInstance(this.activity).sendBroadcast(broadcastIntent);
		
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
			Intent intent = new Intent(this.activity, GeofenceMonitoringService.class);
			
			/*
			 * Return a PendingIntent to start the IntentService. Always create
			 * a PendingIntent sent to Location Services with
			 * FLAG_UPDATE_CURRENT, so that sending the PendingIntent again
			 * updates the original. Otherwise, Location Services can't match
			 * the PendingIntent to requests made with it.
			 */
			this.locationServicesPendingIntent = PendingIntent.getService(this.activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		
		// Return the existing intent
		return this.locationServicesPendingIntent;
	}
	
	/*
	 * Implementation of OnConnectionFailedListener.onConnectionFailed If a
	 * connection or disconnection request fails, report the error
	 * connectionResult is passed in from Location Services
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Downgrade the "requested" geofences to "pending".
		for (Geofence g : this.pendingGeofenceAdditions) {
			this.queuedGeofenceAdditions.add(g);
		}
		this.pendingGeofenceAdditions.clear();
		
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this.activity, GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
				
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
			
			/*
			 * If no resolution is available, put the error code in an error
			 * Intent and broadcast it back to the main Activity. The Activity
			 * then displays an error dialog. is out of date.
			 */
		} else {
			
			Intent errorBroadcastIntent = new Intent(GeofenceUtils.ACTION_CONNECTION_ERROR);
			errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE, connectionResult.getErrorCode());
			LocalBroadcastManager.getInstance(activity).sendBroadcast(errorBroadcastIntent);
		}
	}
}
