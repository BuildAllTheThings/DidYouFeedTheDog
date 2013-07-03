package com.buildallthethings.doglog.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buildallthethings.doglog.Feeding;
import com.buildallthethings.doglog.OnFeedingHistoryChangedListener;

import android.content.Context;
import android.widget.ArrayAdapter;

public class FeedingHistoryListAdapter extends ArrayAdapter<String> implements OnFeedingHistoryChangedListener {
	Map<String, Integer>	stringIds	= new HashMap<String, Integer>();
	
	public FeedingHistoryListAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_1);
		
		// Populate the entries
		String[] values = new String[] { "Android", "iPhone", "WindowsMobile", "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
				"OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Android", "iPhone", "WindowsMobile" };
		
		final List<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
			this.stringIds.put(values[i], i);
		}
	}
	
	@Override
	public long getItemId(int position) {
		String item = getItem(position);
		return this.stringIds.get(item);
	}
	
	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	@Override
	public void onFeedingHistoryChanged(List<Feeding> history) {
		this.notifyDataSetChanged();
	}
}
