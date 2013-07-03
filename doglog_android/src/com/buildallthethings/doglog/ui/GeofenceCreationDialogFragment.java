package com.buildallthethings.doglog.ui;

import com.buildallthethings.doglog.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class GeofenceCreationDialogFragment extends DialogFragment implements OnEditorActionListener {
	
	// Use this instance of the interface to deliver action events
	GeofenceCreationDialogListener	addressListener;
	// Save a reference to the text field so our listener can read the value
	private EditText				addressText;
	
	public GeofenceCreationDialogFragment() {
		;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			this.addressListener = (GeofenceCreationDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString() + " must implement GeofenceCreationDialogListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dialog_set_home, container);
		this.getDialog().setTitle(R.string.set_address);
		// this.setIcon(R.drawable.ic_menu_home);
		
		this.addressText = (EditText) view.findViewById(R.id.value_address);
		
		// Show soft keyboard automatically
		this.addressText.requestFocus();
		this.getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		this.addressText.setOnEditorActionListener(this);
		
		return view;
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			// Return input text to activity
			this.addressListener.onGeofenceAddressSelected(this.addressText.getText().toString());
			this.dismiss();
			return true;
		}
		return false;
	}
}
