package com.buildallthethings.doglog.ui;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.R;
import com.buildallthethings.doglog.ui.GeofenceCreationDialogFragment;
import com.buildallthethings.doglog.HomeManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapInteractionFragment extends SupportMapFragment implements OnMarkerDragListener, OnMarkerClickListener, OnMapLongClickListener, OnMyLocationChangeListener,
		OnSharedPreferenceChangeListener {
	
	protected HomeManager	homeManager;
	
	public MapInteractionFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		this.getMap().setOnMarkerDragListener(this);
		this.getMap().setOnMarkerClickListener(this);
		this.getMap().setOnMapLongClickListener(this);
		
		this.getMap().setMyLocationEnabled(true);
		this.getMap().setOnMyLocationChangeListener(this);
		
		SharedPreferences prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.homeManager = HomeManager.getInstance(this.getActivity());
		
		return rootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.redraw();
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		DialogFragment dialog = new GeofenceCreationDialogFragment();
		dialog.show(getFragmentManager(), "GeofenceCreationDialogFragment");
		
		return true;
	}
	
	@Override
	public void onMarkerDrag(Marker marker) {
		;
	}
	
	@Override
	public void onMarkerDragEnd(Marker marker) {
		LatLng dragPosition = marker.getPosition();
		this.homeManager.setByLatLng(dragPosition.latitude, dragPosition.longitude);
		this.redraw();
	}
	
	@Override
	public void onMarkerDragStart(Marker marker) {
		;
	}
	
	@Override
	public void onMapLongClick(LatLng point) {
		this.homeManager.setByLatLng(point.latitude, point.longitude);
		this.redraw();
	}
	
	@Override
	public void onMyLocationChange(Location location) {
		this.redraw();
	}
	
	protected void redraw() {
		if (this.getMap() != null) {
			this.getMap().clear();
			
			float homeRadius = this.homeManager.getRadius();
			LatLng homeLatLng = new LatLng(this.homeManager.getLatitude(), this.homeManager.getLongitude());
			
			this.getMap().addMarker(new MarkerOptions().draggable(true).position(homeLatLng).title(this.getString(R.string.home)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_home)));
			this.getMap().addCircle(new CircleOptions().center(homeLatLng).radius(homeRadius).fillColor(Color.parseColor("#CCFF00")));
			
			CameraUpdate position = CameraUpdateFactory.newLatLngZoom(homeLatLng, 14);
			Location userLocation = this.getMap().getMyLocation();
			if (userLocation != null) {
				float[] distanceBetween = new float[1];
				Location.distanceBetween(homeLatLng.latitude, homeLatLng.longitude, userLocation.getLatitude(), userLocation.getLongitude(), distanceBetween);
				if (distanceBetween[0] > 10 * homeRadius) {
					LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
					position = CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder().include(homeLatLng).include(userLatLng).build(), 10);
				}
			}
			
			this.getMap().moveCamera(position);
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(Constants.TAG, "Map fragment notified of preference changed: " + key);
		if (key.startsWith(Constants.GEOFENCE_HOME_KEY)) {
			this.redraw();
		}
	}
}
