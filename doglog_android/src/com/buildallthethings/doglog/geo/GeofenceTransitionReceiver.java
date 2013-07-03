package com.buildallthethings.doglog.geo;

import java.util.ArrayList;
import java.util.List;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.R;
import com.buildallthethings.doglog.ui.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class GeofenceTransitionReceiver extends BroadcastReceiver {
	
	protected List<GeofenceTransitionListener>	onGeofenceTransitionListeners;
	
	public GeofenceTransitionReceiver() {
		this.onGeofenceTransitionListeners = new ArrayList<GeofenceTransitionListener>();
	}
	
	/*
	 * Define the required method for broadcast receivers This method is invoked
	 * when a broadcast Intent triggers the receiver
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Check the action code and determine what to do
		String action = intent.getAction();
		
		if (action.equals(Constants.INTENT_ACTION_GEOFENCE_ERROR)) {
			// Intent contains information about errors in adding or removing
			// geofences
			handleGeofenceError(context, intent);
			
		} else if (action.equals(Constants.INTENT_ACTION_GEOFENCE_TRANSITION)) {
			int transition = intent.getIntExtra(Constants.INTENT_EXTRA_GEOFENCE_TRANSITION, -1);
			List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
			if (geofences != null) {
				for (Geofence g : geofences) {
					handleGeofenceTransition(context, intent, g, transition);
					
					for (GeofenceTransitionListener listener : this.onGeofenceTransitionListeners) {
						listener.onGeofenceTransition(g, transition);
					}
				}
			}
		} else {
			// The Intent contained an invalid action
			Log.e(Constants.TAG, context.getString(R.string.invalid_action_detail, action));
			Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Report geofence transitions to the UI
	 * 
	 * @param context
	 *            A Context for this component
	 * @param intent
	 *            The Intent containing the transition
	 * @param transition
	 * @param geofence
	 */
	private void handleGeofenceTransition(Context context, Intent intent, Geofence geofence, int transition) {
		// Post a notification
		String transitionType = getTransitionString(context, transition);
		
		sendNotification(context, transitionType, geofence.getRequestId());
		
		// Log the transition type and a message
		Log.d(Constants.TAG, context.getString(R.string.geofence_transition_notification_title, transitionType, geofence.getRequestId()));
		Log.d(Constants.TAG, context.getString(R.string.geofence_transition_notification_text));
	}
	
	/**
	 * Posts a notification in the notification bar when a transition is
	 * detected. If the user clicks the notification, control goes to the main
	 * Activity.
	 * 
	 * @param transitionType
	 *            The type of transition that occurred.
	 * 
	 */
	private void sendNotification(Context context, String transitionType, String id) {
		
		// Create an explicit content Intent that starts the main Activity
		Intent notificationIntent = new Intent(context.getApplicationContext(), MainActivity.class);
		
		// Construct a task stack
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		
		// Adds the main Activity to the task stack as the parent
		stackBuilder.addParentStack(MainActivity.class);
		
		// Push the content Intent onto the stack
		stackBuilder.addNextIntent(notificationIntent);
		
		// Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Get a notification builder that's compatible with platform versions
		// >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		
		// Set the notification contents
		builder.setSmallIcon(R.drawable.ic_notification).setContentTitle(context.getString(R.string.geofence_transition_notification_title, transitionType, id))
				.setContentText(context.getString(R.string.geofence_transition_notification_text)).setContentIntent(notificationPendingIntent);
		
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Issue the notification
		mNotificationManager.notify(0, builder.build());
	}
	
	/**
	 * Maps geofence transition types to their human-readable equivalents.
	 * 
	 * @param transitionType
	 *            A transition type constant defined in Geofence
	 * @return A String indicating the type of transition
	 */
	private String getTransitionString(Context context, int transitionType) {
		switch (transitionType) {
		
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return context.getString(R.string.geofence_transition_entered);
				
			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return context.getString(R.string.geofence_transition_exited);
				
			default:
				return context.getString(R.string.geofence_transition_unknown);
		}
	}
	
	/**
	 * Report addition or removal errors to the UI, using a Toast
	 * 
	 * @param intent
	 *            A broadcast Intent sent by ReceiveTransitionsIntentService
	 */
	private void handleGeofenceError(Context context, Intent intent) {
		String msg = intent.getStringExtra(Constants.INTENT_EXTRA_GEOFENCE_STATUS);
		Log.e(Constants.TAG, msg);
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	public void registerOnGeofenceTransitionListener(GeofenceTransitionListener listener) {
		this.onGeofenceTransitionListeners.add(listener);
	}
}
