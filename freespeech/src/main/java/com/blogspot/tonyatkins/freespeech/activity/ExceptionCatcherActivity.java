/**
 * Copyright 2012-2015 Upright Software <info@uprightsoftware.com>. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Upright Software ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Upright Software OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityLaunchListener;
import com.blogspot.tonyatkins.freespeech.listeners.FeedbackListener;

public class ExceptionCatcherActivity extends Activity {
	public static final String STACK_TRACE_KEY = "stacktrace";
	private String stackTrace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Read the exception information from the incoming bundle
		Bundle data = getIntent().getExtras();
		if (data != null) {
			stackTrace = data.getString(STACK_TRACE_KEY);
		}

		setContentView(R.layout.exception_catcher);
		
		Button closeButton = (Button) findViewById(R.id.exception_catcher_close_button);
		closeButton.setOnClickListener(new ActivityQuitListener());
		
		Button feedbackButton = (Button) findViewById(R.id.exception_catcher_feedback_button);
		Intent feedbackIntent = new Intent(this, FeedbackActivity.class);
		if (stackTrace != null) {
			feedbackIntent.putExtra(FeedbackActivity.STACK_TRACE_KEY, stackTrace);
		}
		ActivityLaunchListener activityLaunchListener = new ActivityLaunchListener(this, FeedbackActivity.REQUEST_CODE, feedbackIntent);
		feedbackButton.setOnClickListener(activityLaunchListener);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == FeedbackListener.FEEDBACK_REQUEST_CODE) {
			finish();
		}
	}

	private class ActivityQuitListener implements OnClickListener {
		public void onClick(View v) {
			ExceptionCatcherActivity.this.finish();
		}
	}
}
