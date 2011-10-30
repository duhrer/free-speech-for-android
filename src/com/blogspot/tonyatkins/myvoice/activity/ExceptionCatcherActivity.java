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
