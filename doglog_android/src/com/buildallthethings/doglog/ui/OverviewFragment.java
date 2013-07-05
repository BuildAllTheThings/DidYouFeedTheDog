package com.buildallthethings.doglog.ui;

import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.List;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.FeedingManager;
import com.buildallthethings.doglog.HomeManager;
import com.buildallthethings.doglog.Meal;
import com.buildallthethings.doglog.OnFeedingHistoryChangedListener;
import com.buildallthethings.doglog.R;
import com.google.android.gms.location.LocationListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewFragment extends Fragment implements OnSharedPreferenceChangeListener, OnFeedingHistoryChangedListener, LocationListener {
	
	// UI elements
	protected ImageView			homeIcon;
	protected TextView			homeText;
	protected ImageView			latLongIcon;
	protected TextView			latLongText;
	protected TextView			distanceText;
	protected ImageView			lastMealIcon;
	protected TextView			lastMealText;
	protected ImageView			nextMealIcon;
	protected TextView			nextMealText;
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
		
		this.latLongIcon = (ImageView) rootView.findViewById(R.id.lat_long_icon);
		this.latLongText = (TextView) rootView.findViewById(R.id.lat_long_text);
		this.distanceText = (TextView) rootView.findViewById(R.id.distance_text);
		
		this.lastMealIcon = (ImageView) rootView.findViewById(R.id.last_meal_icon);
		this.lastMealIcon.setColorFilter(Color.YELLOW);
		this.lastMealText = (TextView) rootView.findViewById(R.id.last_meal_text);
		this.lastMealText.setText("?");
		this.nextMealIcon = (ImageView) rootView.findViewById(R.id.next_meal_icon);
		this.nextMealIcon.setColorFilter(Color.YELLOW);
		this.nextMealText = (TextView) rootView.findViewById(R.id.next_meal_text);
		this.nextMealText.setText("?");
		this.newFeedingIcon = (ImageView) rootView.findViewById(R.id.add_new_feeding_icon);
		
		this.newFeedingIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				feedingManager.logFeeding();
			}
		});
		
		this.prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.setHomeStatus();
		this.onFeedingHistoryChanged();
		
		this.homeManager.registerLocationListener(this);
		
		return rootView;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(Constants.TAG, "Overview fragment notified of preference changed: " + key);
		if (key.equals(Constants.PREFS_USER_IS_HOME)) {
			// Update our idea of whether people are home or not.
			this.setHomeStatus();
			this.setPositionAndDistance();
		}
	}
	
	protected void setHomeStatus() {
		if (this.prefs.getBoolean(Constants.PREFS_USER_IS_HOME, false)) {
			this.homeIcon.setColorFilter(Color.GREEN);
			this.homeText.setText(R.string.you_are_home);
		} else {
			this.homeIcon.setColorFilter(Color.RED);
			this.homeText.setText(R.string.you_are_not_home);
		}
	}
	
	protected void setPositionAndDistance() {
		Location userLocation = this.homeManager.getUserLocation();
		if (userLocation != null) {
			double distance = this.homeManager.getDistanceToHome();
			this.latLongIcon.setColorFilter(Color.GREEN);
			
			this.latLongText.setText("(" + new Formatter().format("%.6f", userLocation.getLatitude()) + ", " + new Formatter().format("%.6f", userLocation.getLongitude()) + ")");
			this.distanceText.setText(new Formatter().format("%d", (int) distance) + " meters from home");
		} else {
			this.latLongIcon.setColorFilter(Color.RED);
			this.latLongText.setText("unknown location");
			this.distanceText.setText("unknown distance");
		}
	}
	
	@Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this.getActivity(), msg, Toast.LENGTH_SHORT).show();

		this.setPositionAndDistance();
	}
	
	@Override
	public void onFeedingHistoryChanged() {
		long now = System.currentTimeMillis();
		List<Meal> meals = this.feedingManager.getMealsForDay(now, 0);
		int i = 0;
		for (Meal meal : meals) {
			Log.d(Constants.TAG, "OverviewFragment onFeedingHistoryChanged today's meals #" + i + ": " + meal.toString());
			long timestamp = (meal.getFedStatus() ? meal.getPosixTimestamp() : meal.getPosixSchedule());
			String text = meal.getName() + " at " + new SimpleDateFormat("h:mm a").format(timestamp);
			int icon = (meal.getFedStatus() ? R.drawable.ic_cab_done_holo_dark : (meal.getPosixSchedule() > System.currentTimeMillis() ? R.drawable.ic_menu_recent_history : R.drawable.ic_clear_disabled));
			int color = (meal.getFedStatus() ? Color.GREEN : (meal.getPosixSchedule() > System.currentTimeMillis() ? Color.YELLOW : Color.RED));
			if (i == 0) {
				this.lastMealIcon.setImageResource(icon);
				this.lastMealIcon.setColorFilter(color);
				this.lastMealText.setText(text);
			} else if (i == 1) {
				this.nextMealIcon.setImageResource(icon);
				this.nextMealIcon.setColorFilter(color);
				this.nextMealText.setText(text);
			}
			i++;
		}
	}
}
