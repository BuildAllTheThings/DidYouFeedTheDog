package com.buildallthethings.doglog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class OverviewFragment extends Fragment implements OnSharedPreferenceChangeListener {
	
	// UI elements
	protected ImageView				homeIcon;
	
	//
	protected HomeManager	homeOrNotHome;
	
	public OverviewFragment() {
		;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
		
		// Initialize UI elements
		this.homeIcon = (ImageView) rootView.findViewById(R.id.home_icon);
		this.homeIcon.setColorFilter(Color.YELLOW);
		
		SharedPreferences prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.homeOrNotHome = HomeManager.getInstance(this.getActivity());
		
		return rootView;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(Constants.TAG, "Overview fragment notified of preference changed: " + key);
		if (key.equals(Constants.STATUS_USER_HOME)) {
			// Update our idea of whether people are home or not.
			this.setHomeStatus(sharedPreferences.getBoolean(key, false));
		}
	}
	
	private void setHomeStatus(boolean home) {
		if (home) {
			this.homeIcon.setColorFilter(Color.GREEN);
		} else {
			this.homeIcon.setColorFilter(Color.RED);
		}
	}
}
