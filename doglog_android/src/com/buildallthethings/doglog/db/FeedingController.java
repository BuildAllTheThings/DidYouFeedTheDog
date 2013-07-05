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
		// Stored as Unix timestamps
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
	
	protected List<Feeding> getAllFeedings(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		List<Feeding> feedings = new ArrayList<Feeding>();
		
		Cursor cursor = this.database.query(Helper.TABLE_FEEDINGS, this.allColumns, selection, selectionArgs, groupBy, having, orderBy);
		
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
	
	public List<Feeding> getAllFeedings() {
		return this.getAllFeedings(null, null, null, null, null);
	}
	
	private Feeding cursorToFeeding(Cursor cursor) {
		String debug = "";
		for (String col : cursor.getColumnNames()) {
			if (debug.length() > 0) {
				debug += ", ";
			}
			debug += col + ": " + cursor.getString(cursor.getColumnIndex(col)); 
		}
		Log.d(Constants.TAG, "Read feeding: " + debug);
		Feeding feeding = new Feeding();
		feeding.setId(cursor.getLong(0));
		feeding.setUnixTimestamp(cursor.getLong(1));
		return feeding;
	}
	
	public void registerOnFeedingHistoryChangedListener(OnFeedingHistoryChangedListener listener) {
		this.listeners.add(listener);
	}
	
	public List<Feeding> getAllFeedingsBetween(long since, long until) {
		// Passed Posix timestamps, but stored as Unix timestamps
		return this.getAllFeedings("timestamp >= ? AND timestamp < ?", new String[]{Long.toString(since / 1000), Long.toString(until / 1000)}, null, null, null);
	}
}
