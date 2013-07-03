package com.buildallthethings.doglog.geo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.buildallthethings.doglog.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

public class LocationAware implements ConnectionCallbacks, OnConnectionFailedListener {
	// Stores the context required to get a location client
	protected Context			context;
	
	// Stores the current instantiation of the location client
	protected LocationClient	locationServices;
	
	// Some advanced "nice-to-have" error handling requires an activity.
	protected FragmentActivity	mainActivity;
	
	protected LocationAware(Context context) {
		this.context = context;
	}
	
	/**
	 * Get the current location client, or create a new one if necessary.
	 * 
	 * @return A LocationClient object
	 */
	protected GooglePlayServicesClient getLocationClient() {
		if (this.locationServices == null) {
			this.locationServices = new LocationClient(this.context, this, this);
		}
		if (!this.locationServices.isConnected() && !this.locationServices.isConnecting()) {
			// This call returns immediately, but the request is not complete
			// until onConnected() or onConnectionFailure() is called.
			this.locationServices.connect();
		}
		return this.locationServices;
	}
	
	protected void terminateLocationClient() {
		if (this.locationServices != null) {
			if (this.locationServices.isConnected()) {
				this.locationServices.disconnect();
			}
			
			// Destroy the current location client
			this.locationServices = null;
		}
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		Log.d(Constants.TAG, this.getClass().getSimpleName() + " connected");
	}
	
	@Override
	public void onDisconnected() {
		Log.d(Constants.TAG, this.getClass().getSimpleName() + " disconnected");
	}
	
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Google Play services can resolve some errors it detects. If the error
		// has a resolution, try sending an Intent to start a Google Play
		// services activity that can resolve error.
		if (connectionResult.hasResolution() && this.mainActivity != null) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this.mainActivity, Constants.PLAY_CONNECTION_FAILURE_CODE);
				
			} catch (SendIntentException e) {
				// Thrown if Google Play services canceled the original
				// PendingIntent. Log the error
				e.printStackTrace();
			}
		} else {
			// If no resolution is available, put the error code in an error
			// Intent and broadcast it back to the main Activity. The Activity
			// then displays an error dialog. is out of date.
			Intent errorBroadcastIntent = new Intent(Constants.INTENT_ACTION_CONNECTION_ERROR);
			errorBroadcastIntent.addCategory(Constants.INTENT_CATEGORY_LOCATION_SERVICES).putExtra(Constants.INTENT_EXTRA_CONNECTION_ERROR_CODE, connectionResult.getErrorCode());
			LocalBroadcastManager.getInstance(this.context).sendBroadcast(errorBroadcastIntent);
		}
	}
	
	public void setMainActivity(FragmentActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
}
