package com.buildallthethings.doglog.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TwoTextArrayAdapter extends ArrayAdapter<StyledListItem> {
    protected LayoutInflater mInflater;

    public static enum RowType {
        LIST_ITEM, HEADER_ITEM
    }

    public TwoTextArrayAdapter(Context context, List<StyledListItem> items) {
        super(context, 0, items);
        this.mInflater = LayoutInflater.from(context);
    }
    
    public TwoTextArrayAdapter(Context context) {
    	super(context, 0);
    	this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return TwoTextArrayAdapter.RowType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return this.getItem(position).getView(this.mInflater, convertView);
    }
}