package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.listeners.ActivityQuitListener;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		// Wire up the "More Info" button
		Button moreInfoButton = (Button) findViewById(R.id.aboutMoreInfoButton);
		moreInfoButton.setOnClickListener(new LaunchUrlListener());
		
		Button closeButton = (Button) findViewById(R.id.aboutCloseButton);
		closeButton.setOnClickListener(new ActivityQuitListener(this));
	}

	private class LaunchUrlListener implements android.view.View.OnClickListener {
		@Override
		public void onClick(View v) {
			Uri uri = Uri.parse("https://bitbucket.org/duhrer/my-voice-for-android");
			Intent intent = new Intent(Intent.ACTION_VIEW,uri);
			startActivity(intent);
		}
	}
}
