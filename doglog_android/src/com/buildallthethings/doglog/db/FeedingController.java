package com.buildallthethings.doglog.db;

import java.util.ArrayList;
import java.util.List;

import com.buildallthethings.doglog.Constants;
import com.buildallthethings.doglog.OnFeedingHistoryChangedListener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FeedingController {
	protected SQLiteDatabase						database;
	protected Helper								dbHelper;
	protected String[]								allColumns	= { Helper.COLUMN_ID, Helper.COLUMN_TIMESTAMP };
	protected List<OnFeedingHistoryChangedListener>	listeners;
	
	public FeedingController(Context context) {
		this.dbHelper = new Helper(context);
		this.listeners = new ArrayList<OnFeedingHistoryChangedListener>();
	}
	
	public void open() throws SQLException {
		this.database = this.dbHelper.getWritableDatabase();
	}
	
	public void close() {
		this.dbHelper.close();
	}
	
	protected void notifyListeners() {
		for (OnFeedingHistoryChangedListener listener : this.listeners) {
			listener.onFeedingHistoryChanged();
		}
	}
	
	public Feeding createFeeding() {
		ContentValues values = new ContentValues();
		values.put(Helper.COLUMN_TIMESTAMP, System.currentTimeMillis() / 1000);
		long id = this.database.insert(Helper.TABLE_FEEDINGS, null, values);
		Log.d(Constants.TAG, "Feeding created with id: " + id);
		Cursor cursor = this.database.query(Helper.TABLE_FEEDINGS, this.allColumns, Helper.COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Feeding newFeeding = cursorToFeeding(cursor);
		cursor.close();
		this.notifyListeners();
		return newFeeding;
	}
	
	public void deleteFeeding(Feeding feeding) {
		long id = feeding.getId();
		Log.d(Constants.TAG, "Feeding deleted with id: " + id);
		this.database.delete(Helper.TABLE_FEEDINGS, Helper.COLUMN_ID + " = " + id, null);
		this.notifyListeners();
	}
	
	public List<Feeding> getAllFeedings() {
		List<Feeding> feedings = new ArrayList<Feeding>();
		
		Cursor cursor = this.database.query(Helper.TABLE_FEEDINGS, this.allColumns, null, null, null, null, null);
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Feeding feeding = cursorToFeeding(cursor);
			feedings.add(feeding);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return feedings;
	}
	
	private Feeding cursorToFeeding(Cursor cursor) {
		Feeding feeding = new Feeding();
		feeding.setId(cursor.getLong(0));
		feeding.setTimestamp(cursor.getLong(1));
		return feeding;
	}
	
	public void registerOnFeedingHistoryChangedListener(OnFeedingHistoryChangedListener listener) {
		this.listeners.add(listener);
	}
	
	public List<Feeding> getAllFeedingsBetween(long since, long until) {
		return this.getAllFeedings();
	}
}
