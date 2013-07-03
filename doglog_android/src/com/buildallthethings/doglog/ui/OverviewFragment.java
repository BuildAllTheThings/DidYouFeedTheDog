package com.buildallthethings.doglog.ui;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OverviewFragment extends Fragment implements OnSharedPreferenceChangeListener {
	
	// UI elements
	protected ImageView			homeIcon;
	protected TextView 			homeText;
	protected ListView			feedingHistoryList;
	protected ImageView			newFeedingIcon;
	
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
		this.homeText = (TextView) rootView.findViewById(R.id.home_text);
		this.homeText.setText("?");
		this.newFeedingIcon = (ImageView) rootView.findViewById(R.id.add_new_feeding_icon);
		
		this.newFeedingIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				feedingManager.logFeeding();
			}
		});
		
		this.feedingHistoryList = (ListView) rootView.findViewById(R.id.feeding_history_list);
		final FeedingHistoryListAdapter adapter = this.feedingManager.getListViewAdapter();
		feedingHistoryList.setAdapter(adapter);
		
		this.prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		
		setHomeStatus();
		
		return rootView;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(Constants.TAG, "Overview fragment notified of preference changed: " + key);
		if (key.equals(Constants.PREFS_USER_IS_HOME)) {
			// Update our idea of whether people are home or not.
			this.setHomeStatus(prefs.getBoolean(key, false));
		}
	}
	
	protected void setHomeStatus(boolean home) {
		if (home) {
			this.homeIcon.setColorFilter(Color.GREEN);
			this.homeText.setText(R.string.you_are_home);
		} else {
			this.homeIcon.setColorFilter(Color.RED);
			this.homeText.setText(R.string.you_are_not_home);
		}
	}
	
	protected void setHomeStatus() {
		this.setHomeStatus(this.prefs.getBoolean(Constants.PREFS_USER_IS_HOME, false));
	}
}
