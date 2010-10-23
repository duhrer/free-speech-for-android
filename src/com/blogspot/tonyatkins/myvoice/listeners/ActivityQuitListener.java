package com.blogspot.tonyatkins.myvoice.listeners;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.view.View.OnClickListener;

public class ActivityQuitListener implements OnCancelListener, OnClickListener, android.content.DialogInterface.OnClickListener {
	private Activity activity;
	
	public ActivityQuitListener(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		activity.finish();
	}
	
	@Override
	public void onClick(View v) {
		activity.finish();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		activity.finish();
	}
}


