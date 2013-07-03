package com.buildallthethings.doglog.geo;

import com.google.android.gms.location.Geofence;

public interface GeofenceTransitionListener {
	public void onGeofenceTransition(Geofence g, int transition);
}
