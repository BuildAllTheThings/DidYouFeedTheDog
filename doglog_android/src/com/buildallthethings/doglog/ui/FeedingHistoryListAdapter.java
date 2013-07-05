package com.buildallthethings.doglog.ui;

import java.util.Calendar;

import com.buildallthethings.doglog.OnFeedingHistoryChangedListener;
import com.buildallthethings.doglog.R;
import com.buildallthethings.doglog.db.Feeding;
import com.buildallthethings.doglog.db.FeedingController;

import android.content.Context;

public class FeedingHistoryListAdapter extends TwoTextArrayAdapter implements OnFeedingHistoryChangedListener {
	protected final Context		context;
	protected FeedingController	feedingController;
	
	public FeedingHistoryListAdapter(Context context) {
		super(context);
		
		this.context = context;
	}
	
	@Override
	public void onFeedingHistoryChanged() {
		this.clear();
		
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis());
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		
		this.add(new HeaderListItem(this.context.getString(R.string.recent_feedings)));
		this.add(new HeaderListItem(this.context.getString(R.string.today)));
		for (Feeding f : this.feedingController.getAllFeedingsBetween(today.getTimeInMillis(), System.currentTimeMillis())) {
			this.add(new TwoTextListItem(f.amOrPm(), f.toString()));
		}
		this.add(new HeaderListItem(this.context.getString(R.string.previous)));
		for (Feeding f : this.feedingController.getAllFeedingsBetween(0, today.getTimeInMillis())) {
			this.add(new TwoTextListItem(f.amOrPm(), f.toString()));
		}
		
		this.notifyDataSetChanged();
	}
	
	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	public void setController(FeedingController feedingController) {
		this.feedingController = feedingController;
		this.feedingController.registerOnFeedingHistoryChangedListener(this);
		
		// Initial population
		this.onFeedingHistoryChanged();
	}
}
