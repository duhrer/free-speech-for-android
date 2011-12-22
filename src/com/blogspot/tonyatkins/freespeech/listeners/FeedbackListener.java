/**
 * Copyright 2011 Tony Atkins <duhrer@gmail.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Tony Atkins ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Tony Atkins OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 */
package com.blogspot.tonyatkins.freespeech.listeners;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;

import com.atlassian.jconnect.droid.Api;
import com.atlassian.jconnect.droid.config.BaseConfig;
import com.blogspot.tonyatkins.freespeech.Constants;

public class FeedbackListener implements OnClickListener {
	public static final int FEEDBACK_REQUEST_CODE = 1330;
	Activity activity;
	
	public FeedbackListener(Activity activity) {
		this.activity = activity;
	}
	
	public void onClick(View v) {
//		Intent feedbackIntent = Api.createFeedbackIntent(Constants.JMC_URL, Constants.JMC_PROJECT);
		Intent feedbackIntent = Api.createFeedbackIntent();
		
		SharedPreferences preferences = activity.getPreferences(Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		
		editor.putString(BaseConfig.SERVER_URL_KEY, Constants.JMC_URL);
		editor.putString(BaseConfig.PROJECT_KEY, Constants.JMC_PROJECT);
		editor.putString(BaseConfig.API_KEY, Constants.JMC_API_KEY);
		
		editor.commit();
		
//		public static final String SERVER_URL_KEY = "jconnect.droid.config.server_url";
//	    public static final String PROJECT_KEY = "jconnect.droid.config.project";
//	    public static final String API_KEY = "jconnect.droid.config.api_key";
		
		// FIXME: Pass the crash data we've collected to the feedback activity.
		activity.startActivityForResult(feedbackIntent,FEEDBACK_REQUEST_CODE);			
	}
}
