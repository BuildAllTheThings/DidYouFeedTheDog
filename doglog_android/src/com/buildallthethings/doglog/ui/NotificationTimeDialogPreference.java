package com.buildallthethings.doglog.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class NotificationTimeDialogPreference extends DialogPreference {
	//protected TextView 
	
	public NotificationTimeDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setPersistent(false);
	}
	
	@Override
	protected void onBindDialogView(View view) {
	    super.onBindDialogView(view);

	    SharedPreferences prefs = this.getSharedPreferences();
	    /*myView.setValue1(prefs.getString(myKey1, myDefaultValue1));
	    myView.setValue2(prefs.getString(myKey2, myDefaultValue2));*/
	}
}
