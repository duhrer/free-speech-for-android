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
package com.blogspot.tonyatkins.myvoice.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.exception.ExceptionHandler;
import com.blogspot.tonyatkins.myvoice.listeners.FeedbackListener;

public class ExceptionCatcherActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exception_catcher);
		
		Button closeButton = (Button) findViewById(R.id.exception_catcher_close_button);
		closeButton.setOnClickListener(new ActivityQuitListener(this));
		
		Button feedbackButton = (Button) findViewById(R.id.exception_catcher_feedback_button);
		// FIXME: This feedback listener won't know anything about the crash at the moment.
		feedbackButton.setOnClickListener(new FeedbackListener(this));

		File crashDir = this.getDir(".crashes", Activity.MODE_WORLD_READABLE);
		File[] crashInstances = crashDir.listFiles();
		long newestTime = 0;
		File newestCrashDir = null;
		
		for (File crashInstanceDir : crashInstances) {
			if (crashInstanceDir.lastModified() > newestTime) {
				newestTime = crashInstanceDir.lastModified();
				newestCrashDir = crashInstanceDir;
			}
		}
		
		if (newestCrashDir == null ) {
			Log.w(this.getClass().getName(), "No crash data found, exiting crash catcher activity.");
			finish();
		}
		else {
			File stackTraceFile = new File(newestCrashDir.getAbsolutePath() + "/" + ExceptionHandler.STACKTRACE_FILENAME);
			FileReader reader;
			try {
				reader = new FileReader(stackTraceFile);
				BufferedReader br = new BufferedReader(reader);
				StringBuffer output = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					output.append(line + "\n");
				}

				// FIXME: pass along the stack trace details to the feedback activity
			} catch (Exception e) {
				Log.e(getClass().getName(),"Error reading stacktrace file",e);
			}

			File screenshotFile = new File(newestCrashDir.getAbsolutePath() + "/" + ExceptionHandler.SCREENSHOT_FILENAME);
			Bitmap bitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());
			if (bitmap == null) {
				Log.w(getClass().getName(), "Couldn't load bitmap data from screenshot file " + screenshotFile.getAbsolutePath());
			}
			else {
				// FIXME: pass along the screenshot details to the feedback activity
			}
		}
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == FeedbackListener.FEEDBACK_REQUEST_CODE) {
			finish();
		}
	}



	private class ActivityQuitListener implements OnClickListener {
		Activity activity;
		public ActivityQuitListener(Activity activity) {
			this.activity = activity;
		}
		@Override
		public void onClick(View v) {
			activity.finish();
		}
	}
}