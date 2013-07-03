package com.buildallthethings.doglog.ui;

import java.util.ArrayList;
import java.util.List;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.FeedingManager;
import com.buildallthethings.doglog.HomeManager;
import com.buildallthethings.doglog.R;

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
import android.widget.ListView;

public class OverviewFragment extends Fragment implements OnSharedPreferenceChangeListener {
	
	// UI elements
	protected ImageView			homeIcon;
	protected ListView			feedingHistoryList;
	
	//
	protected HomeManager		homeManager;
	protected FeedingManager	feedingManager;
	protected SharedPreferences	prefs;
	
	public OverviewFragment() {
		// You might think that you should instantiate things like the singleton
		// HomeManager and FeedingManager here, but you'd be wrong, because they
		// require a context. We derive our context from our Activity which can
		// be null before we're attached to anything.
		;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
		
		// Initialize data structures
		this.homeManager = HomeManager.getInstance(this.getActivity());
		this.feedingManager = FeedingManager.getInstance(this.getActivity());
		
		// Initialize UI elements
		this.homeIcon = (ImageView) rootView.findViewById(R.id.home_icon);
		this.homeIcon.setColorFilter(Color.YELLOW);
		
		this.feedingHistoryList = (ListView) rootView.findViewById(R.id.feeding_history_list);
		final FeedingHistoryListAdapter adapter = new FeedingHistoryListAdapter(this.getActivity());
		feedingHistoryList.setAdapter(adapter);
		
		this.prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		
		setHomeStatus();
		
		return rootView;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(Constants.TAG, "Overview fragment notified of preference changed: " + key);
		if (key.equals(Constants.STATUS_USER_HOME)) {
			// Update our idea of whether people are home or not.
			this.setHomeStatus(prefs.getBoolean(key, false));
		}
	}
	
	protected void setHomeStatus(boolean home) {
		if (home) {
			this.homeIcon.setColorFilter(Color.GREEN);
		} else {
			this.homeIcon.setColorFilter(Color.RED);
		}
	}
	
	protected void setHomeStatus() {
		this.setHomeStatus(this.prefs.getBoolean(Constants.STATUS_USER_HOME, false));
	}
}
