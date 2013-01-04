/**
 * Copyright 2012-2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
package com.blogspot.tonyatkins.freespeech.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespeech.listeners.FeedbackListener;
import com.blogspot.tonyatkins.freespeech.listeners.GenerateCrashListener;

public class AboutActivity extends FreeSpeechActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		boolean enableDevOptions = preferences.getBoolean(Constants.DEV_OPTIONS_PREF, false);
		if (enableDevOptions) {
			Button crashButton = (Button) findViewById(R.id.aboutCrashButton);
			crashButton.setVisibility(View.VISIBLE);
			crashButton.setOnClickListener(new GenerateCrashListener());
		}
		
		Button feedbackButton = (Button) findViewById(R.id.aboutFeedbackButton);
		feedbackButton.setOnClickListener(new FeedbackListener(this));
		
		Button moreInfoButton = (Button) findViewById(R.id.aboutMoreInfoButton);
		moreInfoButton.setOnClickListener(new LaunchUrlListener());
		
		Button closeButton = (Button) findViewById(R.id.aboutCloseButton);
		closeButton.setOnClickListener(new ActivityQuitListener(this));
	}

	private class LaunchUrlListener implements android.view.View.OnClickListener {
		public void onClick(View v) {
			Uri uri = Uri.parse("https://bitbucket.org/duhrer/my-voice-for-android");
			Intent intent = new Intent(Intent.ACTION_VIEW,uri);
			startActivity(intent);
		}
	}
}
