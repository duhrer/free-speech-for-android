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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;

public class CacheControllerActivity extends FreeSpeechActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.cache_service_controller);

		Button exitButton = (Button) findViewById(R.id.serviceControllerExitButton);
		exitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean saveTTS = preferences.getBoolean(Constants.TTS_SAVE_PREF, false);

		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.serviceControllerToggle);
		TextView statusText = (TextView) findViewById(R.id.serviceControllerStatusText);
		Button startServiceButton = (Button) findViewById(R.id.serviceControllerStartServiceButton);
		Button stopServiceButton = (Button) findViewById(R.id.serviceControllerStopServiceButton);

		if (saveTTS) {
			statusText.setText(R.string.cache_control_enabled);
			toggleButton.setChecked(true);
			startServiceButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TtsCacheUtils.rebuildTtsFiles(CacheControllerActivity.this);
					Toast.makeText(CacheControllerActivity.this, "Updating TTS cache data, check notification bar for details.", Toast.LENGTH_LONG).show();
				}
			});
			
			stopServiceButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TtsCacheUtils.stopService(CacheControllerActivity.this);
				}
			});
		}
		else {
			statusText.setText(R.string.cache_control_disabled);
			toggleButton.setChecked(false);
			startServiceButton.setClickable(false);
			stopServiceButton.setClickable(false);
		}
	}
}
