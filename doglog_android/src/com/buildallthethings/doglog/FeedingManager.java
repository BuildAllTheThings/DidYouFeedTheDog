package com.buildallthethings.doglog;

import java.util.Calendar;

import com.buildallthethings.doglog.geo.GeofencePreference;
import com.buildallthethings.doglog.geo.GeofencePreferenceManager;
import com.buildallthethings.doglog.geo.GeofenceTransitionReceiver;
import com.buildallthethings.doglog.ui.MainActivity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class FeedingManager {
	// Singleton instance
	private static FeedingManager	_instance;
	
	protected final Context			context;
	protected final AlarmManager	alarmManager;
	
	private FeedingManager(Context context) {
		this.context = context;
		this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
		
		// Since we depend on wall clock times, we need to know when that changes.
		IntentFilter timeChangeIntentFilter = new IntentFilter();
		timeChangeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		timeChangeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		TimeChangedReceiver timeChangeReceiver = new TimeChangedReceiver();
		this.context.registerReceiver(timeChangeReceiver, timeChangeIntentFilter);
		
		this.schedule();
	}
	
	public void logFeeding() {
		Feeding f = new Feeding();
		f.save();
	}
	
	public void recheck() {
		// Has the dog been fed yet?
		this.sendNotification();
		
		// Schedule the next check
		this.schedule();
	}
	
	public void schedule() {
		// What is the next time we need to check at?
		// Dog gets fed at 8am and 8pm.
		// Grace period of 1 hour after designated feeding times before I become
		// annoying.
		// TODO make these preferences.
		Calendar nextFeeding = Calendar.getInstance();
		nextFeeding.setTimeInMillis(System.currentTimeMillis());
		int thisHour = nextFeeding.get(Calendar.HOUR_OF_DAY);
		if (thisHour < 9) {
			nextFeeding.set(Calendar.HOUR_OF_DAY, 9);
		} else if (thisHour < 21) {
			nextFeeding.set(Calendar.HOUR_OF_DAY, 21);
		} else {
			nextFeeding.add(Calendar.DATE, 1);
			nextFeeding.set(Calendar.HOUR_OF_DAY, 9);
		}
		nextFeeding.set(Calendar.MINUTE, 0);
		nextFeeding.set(Calendar.SECOND, 0);
		nextFeeding.set(Calendar.MILLISECOND, 0);
		// temporarily discard all that work
		nextFeeding.setTimeInMillis(System.currentTimeMillis() + 60000);
		Intent intent = new Intent(context, FeedingTimeReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 0);
		Log.d(Constants.TAG, "FeedingManager schedule next alarm set for " + nextFeeding.getTimeInMillis() / 1000 + ", " + (nextFeeding.getTimeInMillis() - System.currentTimeMillis()) / 1000 + " seconds from now");
		this.alarmManager.set(AlarmManager.RTC_WAKEUP, nextFeeding.getTimeInMillis(), pendingIntent);
	}
	
	protected void sendNotification() {
		// Create an explicit content Intent that starts the main Activity
		Intent notificationIntent = new Intent(this.context.getApplicationContext(), MainActivity.class);
		
		// Construct a task stack
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.context);
		
		// Adds the main Activity to the task stack as the parent
		stackBuilder.addParentStack(MainActivity.class);
		
		// Push the content Intent onto the stack
		stackBuilder.addNextIntent(notificationIntent);
		
		// Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Get a notification builder that's compatible with platform versions
		// >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
		
		// Set the notification contents
		builder.setSmallIcon(R.drawable.ic_notification).setContentTitle(this.context.getString(R.string.feeding_reminder_notification_title))
				.setContentText(this.context.getString(R.string.feeding_reminder_notification_text)).setContentIntent(notificationPendingIntent);
		
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Issue the notification
		mNotificationManager.notify(0, builder.build());
	}
	
	/**
	 * Ensures there is only ever a single instance of FeedingManager
	 * 
	 * @return An instance of FeedingManager
	 */
	public static FeedingManager getInstance(Context context) {
		if (_instance == null) {
			_instance = new FeedingManager(context);
		}
		return _instance;
	}
}
