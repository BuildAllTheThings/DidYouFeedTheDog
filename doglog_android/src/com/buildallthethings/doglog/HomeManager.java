package com.buildallthethings.doglog;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Assuming Google Play services are available on this device, check current
 * location to determine whether it is within "home" boundaries.
 */
public class HomeManager extends LocationAware implements OnSharedPreferenceChangeListener {
	// Singleton instance
	private static HomeManager			_instance;
	
	protected final Activity			activity;
	protected final SharedPreferences	prefs;
	protected final GeofenceManager		geofenceManager;
	protected final GeofencePreference	home;
	
	private HomeManager(Activity activity) {
		super(activity);
		
		this.activity = activity;
		
		this.prefs = this.activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.geofenceManager = new GeofenceManager(this.activity);
		
		this.home = new GeofencePreference(this.prefs, Constants.GEOFENCE_HOME_KEY);
	}
	
	/**
	 * Once the connection is available, send a request to add retrieve the
	 * location
	 */
	protected void continueDeterminingLocation() {
		Location userLocation = this.locationServices.getLastLocation();
		
		this.setUserHomeStatus(this.home.contains(userLocation.getLatitude(), userLocation.getLongitude()));
		
		// Disconnect the location client
		this.terminateLocationClient();
	}
	
	protected void setUserHomeStatus(boolean userIsHome) {
		Editor editor = this.prefs.edit();
		// Write the Geofence values to SharedPreferences
		editor.putBoolean(Constants.STATUS_USER_HOME, userIsHome);
		editor.commit();
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		super.onConnected(bundle);
		
		// Continue determining location
		continueDeterminingLocation();
	}
	
	/*
	 * Implementation of OnConnectionFailedListener.onConnectionFailed If a
	 * connection or disconnection request fails, report the error
	 * connectionResult is passed in from Location Services
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
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
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(Constants.TAG, "HomeOrNotHomeManager notified of preference changed: " + key);
		if (key.startsWith(Constants.GEOFENCE_HOME_KEY)) {
			// Update our idea of whether people are home or not.
			// This will either start the process of getting a new location
			// services
			// client, or do nothing if such a process is already underway.
			// When a client becomes available, the current location will be
			// determined, and then we will decide whether that is within the
			// new boundaries of home.
			this.getLocationClient();
		}
	}
	
	public void register() {
		this.geofenceManager.addGeofence(this.home.toGeofence());
	}
	
	public void setByAddress(String address, Context context) {
		Log.d(Constants.TAG, "Home set to " + address);
		Geocoder geocoder = new Geocoder(context);
		try {
			List<Address> addressChoices = geocoder.getFromLocationName(address, 1);
			if (addressChoices.size() == 1) {
				Address geocodedAddress = addressChoices.get(0);
				if (geocodedAddress.hasLatitude() && geocodedAddress.hasLongitude()) {
					this.setByLatLng(geocodedAddress.getLatitude(), geocodedAddress.getLongitude(), context);
					Toast.makeText(context, context.getString(R.string.toast_home_set_to) + " (" + this.home.getLatitude() + ", " + this.home.getLongitude() + ")", Toast.LENGTH_SHORT).show();
				}
			} else {
				Log.w(Constants.TAG, "Unable to resolve address: " + addressChoices.size() + " choices");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(context, context.getString(R.string.toast_unable_to_set_home), Toast.LENGTH_SHORT).show();
	}
	
	public void setByLatLng(double lat, double lng, Context context) {
		this.home.setByLatLng(lat, lng, this.home.getRadius());
	}
	
	/**
	 * Ensures there is only ever a single instance of Home
	 * 
	 * @return An instance of Home
	 */
	public static HomeManager getInstance(Activity activity) {
		if (_instance == null) {
			_instance = new HomeManager(activity);
		}
		return _instance;
	}
	
	public double getLatitude() {
		return this.home.getLatitude();
	}
	
	public double getLongitude() {
		return this.home.getLongitude();
	}
	
	public float getRadius() {
		return this.home.getRadius();
	}
}
