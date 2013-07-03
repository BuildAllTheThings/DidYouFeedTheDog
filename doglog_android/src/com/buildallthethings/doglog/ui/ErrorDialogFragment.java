package com.buildallthethings.doglog.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment {
	private Dialog	mDialog;
	
	public ErrorDialogFragment() {
		super();
		mDialog = null;
	}
	
	public void setDialog(Dialog dialog) {
		mDialog = dialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return mDialog;
	}
}
