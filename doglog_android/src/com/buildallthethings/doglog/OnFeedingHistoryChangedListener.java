package com.buildallthethings.doglog;

import java.util.List;

import com.buildallthethings.doglog.Feeding;

public interface OnFeedingHistoryChangedListener {
	
	public abstract void onFeedingHistoryChanged(List<Feeding> history);
	
}