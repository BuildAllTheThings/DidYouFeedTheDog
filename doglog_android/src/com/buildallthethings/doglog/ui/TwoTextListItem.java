package com.buildallthethings.doglog.ui;

import com.buildallthethings.doglog.R;
import com.buildallthethings.doglog.ui.TwoTextArrayAdapter.RowType;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class TwoTextListItem implements StyledListItem {
	protected final String			str1;
	protected final String			str2;
	
	public TwoTextListItem(String text1, String text2) {
		this.str1 = text1;
		this.str2 = text2;
	}
	
	@Override
	public int getViewType() {
		return RowType.LIST_ITEM.ordinal();
	}
	
	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.two_text_list_item, null);
		} else {
			view = convertView;
		}
		
		TextView text1 = (TextView) view.findViewById(R.id.list_content1);
		TextView text2 = (TextView) view.findViewById(R.id.list_content2);
		text1.setText(str1);
		text2.setText(str2);
		
		return view;
	}
}
