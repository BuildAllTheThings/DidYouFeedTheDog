package com.buildallthethings.doglog.ui;

import android.view.LayoutInflater;
import android.view.View;

public interface StyledListItem {
	public int getViewType();
	public View getView(LayoutInflater inflater, View convertView);
}
