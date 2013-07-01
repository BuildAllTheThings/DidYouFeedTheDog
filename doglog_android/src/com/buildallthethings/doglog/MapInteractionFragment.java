package com.buildallthethings.doglog;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapInteractionFragment extends SupportMapFragment implements OnMarkerDragListener, OnMarkerClickListener, OnSharedPreferenceChangeListener {
	
	protected HomeManager homeManager;
	
	public MapInteractionFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		this.getMap().setOnMarkerDragListener(this);
		this.getMap().setOnMarkerClickListener(this);
		
		this.getMap().setMyLocationEnabled(true);
		
		SharedPreferences prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.homeManager = HomeManager.getInstance(this.getActivity());
		
		this.redraw();
		
		return rootView;
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
		this.homeManager.setByLatLng(dragPosition.latitude, dragPosition.longitude, this.getActivity());
		this.redraw();
	}
	
	@Override
	public void onMarkerDragStart(Marker marker) {
		;
	}
	
	protected void redraw() {
		this.getMap().clear();
		
		double lat = this.homeManager.getLatitude();
		double lng = this.homeManager.getLongitude();
		float radius = this.homeManager.getRadius();
		LatLng latLng = new LatLng(lat, lng);
		
		this.getMap().addMarker(new MarkerOptions().draggable(true).position(latLng).title(this.getString(R.string.home)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_home)));
		
		this.getMap().addCircle(new CircleOptions().center(latLng).radius(radius).fillColor(Color.parseColor("#CCFF00")));
		
		CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, 14);
		this.getMap().moveCamera(position);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(Constants.TAG, "Map fragment notified of preference changed: " + key);
		if (key.startsWith(Constants.GEOFENCE_HOME_KEY)) {
			this.redraw();
		}
	}
	
}
