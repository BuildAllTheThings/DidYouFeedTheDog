package com.buildallthethings.doglog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.buildallthethings.doglog.geo.GeofencePreference;
import com.buildallthethings.doglog.geo.GeofencePreferenceManager;
import com.buildallthethings.doglog.geo.GeofenceTransitionListener;
import com.buildallthethings.doglog.geo.GeofenceTransitionReceiver;
import com.buildallthethings.doglog.geo.LocationAware;
import com.buildallthethings.doglog.geo.OnGeofencePreferenceAddedListener;
import com.buildallthethings.doglog.ui.ErrorDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Dialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Assuming Google Play services are available on this device, check current
 * location to determine whether it is within "home" boundaries.
 */
public class HomeManager extends LocationAware implements OnSharedPreferenceChangeListener, OnGeofencePreferenceAddedListener, GeofenceTransitionListener, LocationListener {
	// Singleton instance
	private static HomeManager					_instance;
	
	protected final SharedPreferences			prefs;
	protected final GeofencePreferenceManager	geofencePreferenceManager;
	protected final GeofenceTransitionReceiver	geofenceTransitionReceiver;
	protected final IntentFilter				geofenceTransitionIntentFilter;
	protected final GeofencePreference			home;
	
	protected final List<LocationListener>		locationListeners;
	
	private HomeManager(Context context) {
		super(context);
		
		this.prefs = this.context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.geofencePreferenceManager = new GeofencePreferenceManager(this.context);
		this.geofencePreferenceManager.registerOnGeofencePreferenceAddedListener(this);
		
		// Create a new broadcast receiver to receive updates from the listeners
		// and service
		this.geofenceTransitionReceiver = new GeofenceTransitionReceiver();
		this.geofenceTransitionReceiver.registerOnGeofenceTransitionListener(this);
		
		// Create an intent filter for the broadcast receiver
		this.geofenceTransitionIntentFilter = new IntentFilter();
		this.geofenceTransitionIntentFilter.addAction(Constants.INTENT_ACTION_GEOFENCE_TRANSITION);
		this.geofenceTransitionIntentFilter.addCategory(Constants.INTENT_CATEGORY_LOCATION_SERVICES);
		
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this.context).registerReceiver(this.geofenceTransitionReceiver, this.geofenceTransitionIntentFilter);
		
		this.home = new GeofencePreference(this.prefs, Constants.PREFS_GEOFENCE_HOME);
		
		this.locationListeners = new ArrayList<LocationListener>();
		
		// Register a geofence in case we change our position. The act of
		// registering will turn on the location services components. When they
		// are connected, we'll retrieve our current location and see if we're
		// starting off at home or not.
		this.register();
	}
	
	/**
	 * Once the connection is available, send a request to receive location
	 * updates. This is only used to display lat/long in the UI. Once we have an
	 * initial location, the geofence will handle actual transitions.
	 */
	protected void continueDeterminingLocation() {
		Location userLocation = this.locationServices.getLastLocation();
		
		this.setUserHomeStatus(this.home.contains(userLocation.getLatitude(), userLocation.getLongitude()));
		
		if (this.prefs.getBoolean(Constants.PREFS_UI_DISPLAY_LAT_LONG_OF_USER, true)) {
			LocationRequest request = new LocationRequest();
			request.setFastestInterval(10000);
			request.setInterval(600000);
			request.setSmallestDisplacement(5);
			request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			
			this.locationServices.requestLocationUpdates(request, this);
		} else {
			// Disconnect the location client
			this.terminateLocationClient();
		}
	}
	
	protected void setUserHomeStatus(boolean userIsHome) {
		Editor editor = this.prefs.edit();
		// Write the Geofence values to SharedPreferences
		editor.putBoolean(Constants.PREFS_USER_IS_HOME, userIsHome);
		editor.commit();
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		super.onConnected(bundle);
		
		// Continue determining location
		continueDeterminingLocation();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(Constants.TAG, "HomeOrNotHomeManager notified of preference changed: " + key);
		if (key.startsWith(Constants.PREFS_GEOFENCE_HOME)) {
			// Somebody has re-defined home. Un-register the previous geofence
			// and register the new one.
			this.register();
		}
	}
	
	public void onGeofencePreferenceAdded(GeofencePreference geofencePreference) {
		// Update our idea of whether people are home or not.
		// This will either start the process of getting a new location
		// services
		// client, or do nothing if such a process is already underway.
		// When a client becomes available, the current location will be
		// determined, and then we will decide whether that is within the
		// new boundaries of home.
		if (geofencePreference.getRequestId() == Constants.PREFS_GEOFENCE_HOME) {
			this.getLocationClient();
		}
	}
	
	protected boolean havePlayServices() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.context);
		
		if (resultCode == ConnectionResult.SUCCESS) {
			// If Google Play services was available
			// In debug mode, log the status
			Log.d(Constants.TAG, "Google Play services is available.");
			// Continue
			return true;
		} else {
			// Google Play services was not available for some reason
			if (this.mainActivity != null) {
				// Get the error dialog from Google Play services
				Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this.mainActivity, Constants.PLAY_CONNECTION_FAILURE_CODE);
				
				// If Google Play services can provide an error dialog
				if (errorDialog != null) {
					// Create a new DialogFragment for the error dialog
					ErrorDialogFragment errorFragment = new ErrorDialogFragment();
					// Set the dialog in the DialogFragment
					errorFragment.setDialog(errorDialog);
					// Show the error dialog in the DialogFragment
					errorFragment.show(this.mainActivity.getSupportFragmentManager(), this.context.getString(R.string.app_name));
				}
			}
			return false;
		}
	}
	
	public void register() {
		if (this.havePlayServices()) {
			this.geofencePreferenceManager.addGeofence(this.home);
		}
	}
	
	public void setByAddress(String address) {
		Log.d(Constants.TAG, "Home set to " + address);
		Geocoder geocoder = new Geocoder(this.context);
		try {
			List<Address> addressChoices = geocoder.getFromLocationName(address, 1);
			if (addressChoices.size() == 1) {
				Address geocodedAddress = addressChoices.get(0);
				if (geocodedAddress.hasLatitude() && geocodedAddress.hasLongitude()) {
					this.setByLatLng(geocodedAddress.getLatitude(), geocodedAddress.getLongitude());
					Toast.makeText(this.context, this.context.getString(R.string.toast_home_set_to) + " (" + this.home.getLatitude() + ", " + this.home.getLongitude() + ")", Toast.LENGTH_SHORT)
							.show();
					return;
				}
			} else {
				Log.w(Constants.TAG, "Unable to resolve address: " + addressChoices.size() + " choices");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(this.context, this.context.getString(R.string.toast_unable_to_set_home), Toast.LENGTH_SHORT).show();
	}
	
	public void setByLatLng(double lat, double lng) {
		this.home.setByLatLng(lat, lng, this.home.getRadius());
	}
	
	/**
	 * Ensures there is only ever a single instance of HomeManager
	 * 
	 * @return An instance of HomeManager
	 */
	public static HomeManager getInstance(Context context) {
		if (_instance == null) {
			_instance = new HomeManager(context);
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
	
	public void setMainActivity(FragmentActivity mainActivity) {
		super.setMainActivity(mainActivity);
		this.geofencePreferenceManager.setMainActivity(mainActivity);
	}
	
	@Override
	public void onGeofenceTransition(Geofence g, int transition) {
		if (g.getRequestId().equals(Constants.PREFS_GEOFENCE_HOME)) {
			this.setUserHomeStatus(transition == Geofence.GEOFENCE_TRANSITION_ENTER);
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		for (LocationListener listener : this.locationListeners) {
			listener.onLocationChanged(location);
		}
	}
	
	public Location getUserLocation() {
		return this.locationServices.getLastLocation();
	}
	
	public double getDistanceToHome() {
		Location userLocation = this.getUserLocation();
		if (userLocation != null) {
			return this.home.distanceFrom(userLocation.getLatitude(), userLocation.getLongitude());
		} else {
			return -1;
		}
	}

	public void registerLocationListener(LocationListener listener) {
		this.locationListeners.add(listener);
	}
}
