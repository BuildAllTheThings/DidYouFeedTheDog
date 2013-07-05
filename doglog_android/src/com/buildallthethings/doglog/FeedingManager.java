package com.buildallthethings.doglog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.buildallthethings.doglog.db.FeedingController;
import com.buildallthethings.doglog.ui.FeedingHistoryListAdapter;
import com.buildallthethings.doglog.ui.MainActivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class FeedingManager implements OnSharedPreferenceChangeListener, OnFeedingHistoryChangedListener {
	// Singleton instance
	private static FeedingManager					_instance;
	
	protected final Context							context;
	protected final AlarmManager					alarmManager;
	protected final FeedingController				feedingController;
	protected final SharedPreferences				prefs;
	protected List<OnFeedingHistoryChangedListener>	listeners;
	
	private FeedingManager(Context context) {
		this.context = context;
		this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
		this.feedingController = new FeedingController(this.context);
		this.feedingController.registerOnFeedingHistoryChangedListener(this);
		this.feedingController.open();
		
		this.prefs = this.context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		this.listeners = new ArrayList<OnFeedingHistoryChangedListener>();
		
		this.schedule();
	}
	
	public void logFeeding() {
		this.feedingController.createFeeding();
	}
	
	public FeedingHistoryListAdapter getListViewAdapter() {
		FeedingHistoryListAdapter adapter = new FeedingHistoryListAdapter(this.context);
		adapter.setController(this.feedingController);
		return adapter;
	}
	
	protected long getMealSchedule(long timestamp, int offset) {
		Calendar schedule = Calendar.getInstance();
		schedule.setTimeInMillis(timestamp);
		Log.d(Constants.TAG, "FeedingManager getMealSchedule starting at " + schedule.toString());
		// Find the zero-indexed meal from this timestamp.
		// That means the meal that should be happening right now, or the next one up.
		if (schedule.get(Calendar.HOUR_OF_DAY) < 8) {
			schedule.set(Calendar.HOUR_OF_DAY, 8);
		} else if (schedule.get(Calendar.HOUR_OF_DAY) < 20) {
			schedule.set(Calendar.HOUR_OF_DAY, 20);
		} else {
			schedule.add(Calendar.DAY_OF_MONTH, 1);
			schedule.set(Calendar.HOUR_OF_DAY, 8);
		}
		schedule.set(Calendar.MINUTE, 0);
		schedule.set(Calendar.SECOND, 0);
		schedule.set(Calendar.MILLISECOND, 0);
		
		// If you requested a meal other than this one, tweak the timestamp
		// TODO once we let people configure meal schedules this won't work
		// TODO perhaps a recursive approach
		if (offset != 0) {
			schedule.add(Calendar.HOUR_OF_DAY, 12 * offset);
		}
		
		return schedule.getTimeInMillis();
	}
	
	public Meal getMeal(long timestamp, int offset) {
		Meal meal = new Meal(this.feedingController);
		meal.setPosixSchedule(this.getMealSchedule(timestamp, offset));
		
		return meal;
	}
	
	public List<Meal> getMealsForDay(long timestamp, int offset) {
		Calendar day = Calendar.getInstance();
		day.setTimeInMillis(timestamp);
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		day.add(Calendar.DAY_OF_MONTH, offset);
		
		List<Meal> meals = new ArrayList<Meal>();
		long since = day.getTimeInMillis();
		day.add(Calendar.DAY_OF_MONTH, 1);
		long until = day.getTimeInMillis();
		int i = 0;
		while (true) {
			Meal meal = this.getMeal(since, i);
			if (meal.getPosixSchedule() >= since && meal.getPosixSchedule() < until) {
				meals.add(meal);
				i++;
			} else {
				break;
			}
		}
		return meals;
	}
	
	public void recheck() {
		// Has the dog been fed yet?
		// First, determine which meal we think corresponds to "right now".
		long now = System.currentTimeMillis();
		long earlierMealSchedule = this.getMealSchedule(now, -1);
		long laterMealSchedule = this.getMealSchedule(now, 0);
		
		// Which meal is closer in time?
		Meal meal = new Meal(this.feedingController);
		if (now - earlierMealSchedule < laterMealSchedule - now) {
			meal.setPosixSchedule(earlierMealSchedule);
		} else {
			meal.setPosixSchedule(laterMealSchedule);
		}
		
		if (meal.getFedStatus() == false) {
			if (this.prefs.getBoolean(Constants.PREFS_SEND_NOTIFICATIONS, true) && this.prefs.getBoolean(Constants.PREFS_NOTIFY_FEEDING_TIME, true)) {
				this.sendNotification();
			}
		}
		
		// Schedule the next check
		this.schedule();
	}
	
	public void schedule() {
		// What is the next time we need to check at?
		// Dog gets fed at 8am and 8pm.
		Calendar nextFeeding = Calendar.getInstance();
		nextFeeding.setTimeInMillis(System.currentTimeMillis());
		int thisHour = nextFeeding.get(Calendar.HOUR_OF_DAY);
		if (thisHour < 8) {
			nextFeeding.set(Calendar.HOUR_OF_DAY, 8);
		} else if (thisHour < 20) {
			nextFeeding.set(Calendar.HOUR_OF_DAY, 20);
		} else {
			nextFeeding.add(Calendar.DATE, 1);
			nextFeeding.set(Calendar.HOUR_OF_DAY, 8);
		}
		nextFeeding.set(Calendar.MINUTE, 0);
		nextFeeding.set(Calendar.SECOND, 0);
		nextFeeding.set(Calendar.MILLISECOND, 0);
		
		// We're supposed to notify around feeding times.
		int minutes = this.prefs.getInt(Constants.PREFS_NOTIFY_FEEDING_TIME_OFFSET_MAGNITUDE, 60);
		int polarity = this.prefs.getInt(Constants.PREFS_NOTIFY_FEEDING_TIME_OFFSET_POLARITY, 1);
		nextFeeding.add(Calendar.MINUTE, minutes * polarity);
		
		// temporarily discard all that work for debugging purposes
		// nextFeeding.setTimeInMillis(System.currentTimeMillis() + 20000);
		
		Intent intent = new Intent(context, FeedingTimeReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 0);
		Log.d(Constants.TAG, "FeedingManager schedule next alarm set for " + nextFeeding.getTimeInMillis() / 1000 + ", " + (nextFeeding.getTimeInMillis() - System.currentTimeMillis()) / 1000
				+ " seconds from now");
		this.alarmManager.set(AlarmManager.RTC_WAKEUP, nextFeeding.getTimeInMillis(), pendingIntent);
	}
	
	protected PendingIntent buildPendingIntent(Class<?> cls, String action) {
		// Create an explicit content Intent that starts the main Activity
		Intent notificationIntent = new Intent(this.context.getApplicationContext(), cls);
		notificationIntent.setAction(action);
		
		// Construct a task stack
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.context);
		
		// Adds the main Activity to the task stack as the parent
		stackBuilder.addParentStack(MainActivity.class);
		
		// Push the content Intent onto the stack
		stackBuilder.addNextIntent(notificationIntent);
		
		// Get a PendingIntent containing the entire back stack
		PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		return pendingIntent;
	}
	
	protected void sendNotification() {
		// Get a notification builder that's compatible with platform versions
		// >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
		
		// Set the notification contents
		builder
		// Disappear when clicked
		.setAutoCancel(true)
				// We want the user to actually notice this
				.setPriority(Notification.PRIORITY_HIGH)
				// We'll be good and use the default notification settings,
				// though
				.setDefaults(Notification.DEFAULT_ALL)
				// Start the MainActivity on the Overview tab when clicked
				.setContentIntent(this.buildPendingIntent(MainActivity.class, Constants.INTENT_ACTION_VIEW_FEEDINGS))
				// Title and text
				.setContentTitle(this.context.getString(R.string.feeding_reminder_notification_title))
				.setContentText(this.context.getString(R.string.feeding_reminder_notification_text))
				// Icon
				.setSmallIcon(R.drawable.dog_head)
				// When expanded, show action buttons that short-circuit some of
				// the clicking around inside the activity.
				.addAction(R.drawable.ic_cab_done_holo_dark, "Definitely fed him", this.buildPendingIntent(MainActivity.class, Constants.INTENT_ACTION_LOG_FEEDING))
				.addAction(R.drawable.ic_clear_disabled, "Oh crap, forgot", this.buildPendingIntent(MainActivity.class, Constants.INTENT_ACTION_SKIP_FEEDING));
		
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Issue the notification
		mNotificationManager.notify(0, builder.build());
	}
	
	public void registerOnFeedingHistoryChangedListener(OnFeedingHistoryChangedListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFeedingHistoryChanged() {
		for (OnFeedingHistoryChangedListener listener : this.listeners) {
			listener.onFeedingHistoryChanged();
		}
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
