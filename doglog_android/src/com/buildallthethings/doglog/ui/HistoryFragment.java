package com.buildallthethings.doglog.ui;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.FeedingManager;
import com.buildallthethings.doglog.HomeManager;
import com.buildallthethings.doglog.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class HistoryFragment extends Fragment implements OnSharedPreferenceChangeListener {
	
	// UI elements
	protected ListView			feedingHistoryList;
	protected ImageView			newFeedingIcon;
	
	//
	protected HomeManager		homeManager;
	protected FeedingManager	feedingManager;
	protected SharedPreferences	prefs;
	
	public HistoryFragment() {
		// You might think that you should instantiate things like the singleton
		// HomeManager and FeedingManager here, but you'd be wrong, because they
		// require a context. We derive our context from our Activity which can
		// be null before we're attached to anything.
		;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_history, container, false);
		
		// Initialize data structures
		this.homeManager = HomeManager.getInstance(this.getActivity());
		this.feedingManager = FeedingManager.getInstance(this.getActivity());
		
		// Initialize UI elements
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
		
		return rootView;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(Constants.TAG, "History fragment notified of preference changed: " + key);
	}
}
