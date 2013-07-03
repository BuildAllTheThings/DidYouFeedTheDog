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
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString() {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(this.timestamp * 1000);
		return new SimpleDateFormat("E MMMM dd, hh:mm a").format(date.getTime());
	}

	public String amOrPm() {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(this.timestamp * 1000);
		return date.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());
	}
}
