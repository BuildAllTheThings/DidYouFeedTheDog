package com.buildallthethings.doglog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.buildallthethings.doglog.db.Feeding;
import com.buildallthethings.doglog.db.FeedingController;

public class Meal {
	public static long HOUR = 60 * 60 * 1000;
	
	protected FeedingController controller;
	
	protected long schedule;
	protected Feeding feeding;
	
	public Meal(FeedingController controller) {
		this.controller = controller;
	}
	
	public void setPosixSchedule(long schedule) {
		this.schedule = schedule;
		
		List<Feeding> feedings = this.controller.getAllFeedingsBetween(this.schedule - 3 * HOUR, this.schedule + 3 * HOUR);
		if (!feedings.isEmpty()) {
			this.feeding = feedings.get(0);
		}
	}
	
	public long getPosixSchedule() {
		return this.schedule;
	}
	
	public String getName() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(this.schedule);
		if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
			return "Breakfast";
		} else {
			return "Dinner";
		}
	}
	
	public boolean getFedStatus() {
		return (this.feeding != null);
	}
	
	public long getPosixTimestamp() {
		if (this.feeding != null) {
			return this.feeding.getPosixTimestamp();
		} else {
			return -1;
		}
	}
	
	@Override
	public String toString() {
		Calendar date = Calendar.getInstance();
		if (this.getFedStatus()) {
			date.setTimeInMillis(this.getPosixTimestamp());
		} else {
			date.setTimeInMillis(this.getPosixSchedule());
		}
		String timestamp = new SimpleDateFormat("E MMMM dd, hh:mm a").format(date.getTime());
		return this.getName() + " " + (this.getFedStatus() ? "fed" : "due") + " at " + timestamp;
	}
}
