package com.buildallthethings.doglog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.android.gms.location.Geofence;

/**
 * A simple object encapsulating some geofence logic. Defined by its center
 * (latitude and longitude) and radius. Persisted in SharedPreferences
 */
public class GeofencePreference {
	// Radius of the earth for Haversine calculations, in kilometers
	protected static final double		earthRadius	= 6372.8;
	
	// Instance variables
	private double						lat;
	private double						lng;
	private float						radius;
	
	// Persist this in a SharedPreferences object
	protected final SharedPreferences	prefs;
	protected final String				key;
	
	public GeofencePreference(SharedPreferences prefs, String key) {
		this.prefs = prefs;
		this.key = key;
		
		this.restore();
	}
	
	public GeofencePreference(SharedPreferences prefs) {
		this.prefs = prefs;
		this.key = this.hashCode() + "_" + System.currentTimeMillis();
		
		// Nothing to restore, since we just made up the key.
	}
	
	private void restore() {
		this.lat = prefs.getFloat(this.key + "_latitude", 38.8977f);
		this.lng = prefs.getFloat(this.key + "_longitude", -77.0366f);
		this.radius = prefs.getFloat(this.key + "_radius", 20);
	}
	
	private void persist() {
		Editor editor = this.prefs.edit();
		
		// Write our values to SharedPreferences
		editor.putFloat(this.key + "_latitude", (float) this.getLatitude());
		editor.putFloat(this.key + "_longitude", (float) this.getLongitude());
		editor.putFloat(this.key + "_radius", this.getRadius());
		
		editor.commit();
	}

	public double getLatitude() {
		return this.lat;
	}

	public double getLongitude() {
		return this.lng;
	}
	
	public float getRadius() {
		return this.radius;
	}
	
	/**
	 * Creates a Location Services Geofence object
	 * 
	 * @return A Geofence object
	 */
	public Geofence toGeofence() {
		// Build a new Geofence object
		return new Geofence.Builder().setRequestId("home").setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
				.setCircularRegion(this.getLatitude(), this.getLongitude(), this.getRadius()).setExpirationDuration(Geofence.NEVER_EXPIRE).build();
	}
	
	public boolean setByLatLng(double lat, double lng, float radius) {
		Log.d(Constants.TAG, "Home set to (" + lat + ", " + lng + ")");
		this.lat = lat;
		this.lng = lng;
		this.radius = radius;
		
		this.persist();
		
		return true;
	}
	
	protected static double haversine(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return earthRadius * c;
	}
	
	public boolean contains(double lat, double lng) {
		double distanceInKilometers = GeofencePreference.haversine(lat, lng, this.lat, this.lng);
		return  ((distanceInKilometers * 1000) <= this.radius);
	}
}
