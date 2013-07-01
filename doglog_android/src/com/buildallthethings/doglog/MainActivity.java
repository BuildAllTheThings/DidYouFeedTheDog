package com.buildallthethings.doglog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements GeofenceCreationDialogFragment.GeofenceCreationDialogListener, OnSharedPreferenceChangeListener {
	// UI
	protected MainActivityFragmentPagerAdapter	pagerAdapter;
	protected ViewPager							mViewPager;
	
	// Geofencing
	protected HomeManager						homeManager;
	private GeofenceTransitionReceiver			mBroadcastReceiver;
	private IntentFilter						mIntentFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		this.pagerAdapter = new MainActivityFragmentPagerAdapter(this);
		
		// Set up the ViewPager with the sections adapter.
		this.mViewPager = (ViewPager) findViewById(R.id.pager);
		this.mViewPager.setAdapter(pagerAdapter);
		
		// Create a new broadcast receiver to receive updates from the listeners
		// and service
		this.mBroadcastReceiver = new GeofenceTransitionReceiver();
		
		// Create an intent filter for the broadcast receiver
		this.mIntentFilter = new IntentFilter();
		
		// Action for broadcast Intents that report successful addition of
		// geofences
		this.mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);
		
		// Action for broadcast Intents that report successful removal of
		// geofences
		this.mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
		
		// Action for broadcast Intents containing various types of geofencing
		// errors
		this.mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
		
		// All Location Services sample apps use this category
		this.mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		
		// Shared preference listener lets us re-register the geofence for home whenever it changes
		SharedPreferences prefs = this.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.homeManager = HomeManager.getInstance(this);
		
		// Force an initial registration
		this.register();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onGeofenceAddressSelected(String address) {
		this.homeManager.setByAddress(address, this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(Constants.TAG, "Main activity notified of preference changed: " + key);
		if (key.startsWith(Constants.GEOFENCE_HOME_KEY)) {
			// Re-register geofence
			this.register();
		}
	}
	
	private void register() {
		/*
		 * Check for Google Play services. Do this after setting the request
		 * type. If connecting to Google Play services fails, onActivityResult
		 * is eventually called, and it needs to know what type of request was
		 * in progress.
		 */
		if (!servicesConnected()) {
			return;
		}
		
		this.homeManager.register();
	}
	
	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Geofence Detection", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, Constants.CONNECTION_FAILURE_CODE);
			
			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getSupportFragmentManager(), "Geofence Detection");
			}
			return false;
		}
	}
	
	/*
	 * Whenever the Activity resumes, reconnect the client to Location Services
	 * and reload the last geofences that were set
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
		
		// Re-register geofences
		this.register();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Choose what to do based on the request code
		switch (requestCode) {
			case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:
				// The request code matches the code sent in onConnectionFailed
				switch (resultCode) {
					case Activity.RESULT_OK:
						// Google Play services resolved the problem, so
						// re-attempt our registration.
						this.register();
						
						// If any other result was returned by Google Play
						// services
					default:
						
						// Report that Google Play services was unable to
						// resolve the
						// problem.
						Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
				}
				
				// If any other request code was received
			default:
				// Report that this Activity received an unknown requestCode
				Log.d(GeofenceUtils.APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
				
				break;
		}
	}
}
