package com.buildallthethings.doglog.ui;

import com.buildallthethings.doglog.R;
import com.buildallthethings.doglog.ui.TwoTextArrayAdapter.RowType;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class HeaderListItem implements StyledListItem {
	private final String	name;
	
	public HeaderListItem(String name) {
		this.name = name;
	}
	
	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}
	
	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.header_list_item, null);
		} else {
			view = convertView;
		}
		
		TextView text = (TextView) view.findViewById(R.id.separator);
		text.setText(name);
		
		return view;
	}
}
