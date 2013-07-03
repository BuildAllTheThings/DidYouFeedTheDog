package com.buildallthethings.doglog.ui;

import com.buildallthethings.doglog.ui.GeofenceCreationDialogListener;
import com.buildallthethings.doglog.geo.GeofenceTransitionReceiver;
import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.HomeManager;
import com.buildallthethings.doglog.R;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements GeofenceCreationDialogListener {
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
		this.mIntentFilter.addAction(Constants.INTENT_ACTION_GEOFENCE_ERROR);
		this.mIntentFilter.addCategory(Constants.INTENT_CATEGORY_LOCATION_SERVICES);
		
		this.homeManager = HomeManager.getInstance(this);
		this.homeManager.setMainActivity(this);
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
		this.homeManager.setByAddress(address);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Choose what to do based on the request code
		switch (requestCode) {
			case Constants.PLAY_CONNECTION_FAILURE_CODE:
				// The request code matches the code sent in onConnectionFailed
				switch (resultCode) {
					case Activity.RESULT_OK:
						// Google Play services resolved the problem, so
						// re-attempt our registration.
						this.homeManager.register();
						break;
						
					default:
						// Report that Google Play services was unable to
						// resolve the problem.
						Log.d(Constants.TAG, getString(R.string.no_resolution));
						break;
				}
				break;
				
			default:
				// Report that this Activity received an unknown requestCode
				Log.d(Constants.TAG, getString(R.string.unknown_activity_request_code, requestCode));
				break;
		}
	}
}
