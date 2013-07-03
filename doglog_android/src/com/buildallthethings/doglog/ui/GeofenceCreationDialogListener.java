package com.buildallthethings.doglog.ui;

/*
 * The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks. Each method
 * passes the DialogFragment in case the host needs to query it.
 */
public interface GeofenceCreationDialogListener {
	public void onGeofenceAddressSelected(String address);
}