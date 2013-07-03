package com.buildallthethings.doglog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FeedingTimeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		FeedingManager feedingManager = FeedingManager.getInstance(context);
		feedingManager.recheck();
	}
}
