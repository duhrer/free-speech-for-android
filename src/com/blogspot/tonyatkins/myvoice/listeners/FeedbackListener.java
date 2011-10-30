package com.blogspot.tonyatkins.myvoice.listeners;

import com.atlassian.jconnect.droid.Api;
import com.blogspot.tonyatkins.myvoice.Constants;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class FeedbackListener implements OnClickListener {
	public static final int FEEDBACK_REQUEST_CODE = 1330;
	Activity activity;
	
	public FeedbackListener(Activity activity) {
		this.activity = activity;
	}
	
	public void onClick(View v) {
		Intent feedbackIntent = Api.createFeedbackIntent(Constants.JMC_URL, Constants.JMC_PROJECT);
		
		// FIXME: Pass the crash data we've collected to the feedback activity.
		activity.startActivityForResult(feedbackIntent,FEEDBACK_REQUEST_CODE);			
	}
}
