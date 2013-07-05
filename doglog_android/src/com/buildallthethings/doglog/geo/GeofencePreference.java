package com.buildallthethings.doglog.geo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;

import com.buildallthethings.doglog.Constants;
import com.google.android.gms.location.Geofence;

/**
 * A simple object encapsulating some geofence logic. Defined by its center
 * (latitude and longitude) and radius. Persisted in SharedPreferences
 */
public class GeofencePreference {
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
		this.radius = prefs.getFloat(this.key + "_radius", 40);
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
	
	public String getRequestId() {
		return this.key;
	}
	
	/**
	 * Creates a Location Services Geofence object
	 * 
	 * @return A Geofence object
	 */
	public Geofence toGeofence() {
		// Build a new Geofence object
		return new Geofence.Builder().setRequestId(this.key).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
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
	
	public double distanceFrom(double latitude, double longitude) {
		float[] distance = new float[1];
		Location.distanceBetween(lat, lng, this.lat, this.lng, distance);
		return distance[0];
	}
	
	public boolean contains(double lat, double lng) {
		return this.distanceFrom(lat, lng) <= this.getRadius();
	}

	
}
