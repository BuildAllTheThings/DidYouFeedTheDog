package com.buildallthethings.doglog;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

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
		Log.d("LocationAware", this.getClass().getSimpleName() + " connected");
	}
	
	@Override
	public void onDisconnected() {
		Log.d("LocationAware", this.getClass().getSimpleName() + " disconnected");
	}
	
	// There are almost certainly better ways of handling this, you may want to override it.
	// For instance, actually parsing the connection result would be a better way.
	public void onConnectionFailed(ConnectionResult connectionResult) {
		;
	}
}