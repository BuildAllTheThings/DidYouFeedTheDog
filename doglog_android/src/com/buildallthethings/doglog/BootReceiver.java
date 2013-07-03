package com.buildallthethings.doglog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Constants.TAG, "BootReceiver onReceive");
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Ensure HomeManager is instantiated so that the current location is checked and the geofences are registered.
			HomeManager.getInstance(context);
			// Ensure FeedingManager is instantiated so that alarms are set
			FeedingManager.getInstance(context);
		}
	}
	
}
