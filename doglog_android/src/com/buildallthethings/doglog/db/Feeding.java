package com.buildallthethings.doglog.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Feeding {
	private long id;
	private long timestamp;
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getPosixTimestamp() {
		// Timestamps are stored as Unix but passed in Java-land as Posix
		return this.timestamp * 1000;
	}
	
	public void setPosixTimestamp(long timestamp) {
		// Timestamps are stored as Unix but passed in Java-land as Posix
		this.timestamp = timestamp / 1000;
	}
	
	public void setUnixTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString() {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(this.getPosixTimestamp());
		return new SimpleDateFormat("E MMMM dd, hh:mm a").format(date.getTime());
	}

	public String amOrPm() {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(this.getPosixTimestamp());
		return date.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());
	}
}
