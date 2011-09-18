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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

public class EditTextActivity extends Activity {
	public static final String TEXT_TYPE = "textType";
	public static final int REQUEST_CODE = 975;
	public static final int LABEL_UPDATED = 579;
	public  static final int TTS_TEXT_UPDATED = 357;
	private SoundButtonView soundButtonView;
	private DbAdapter dbAdapter;
	private SoundButton tempButton;
	private int textType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = this.getIntent().getExtras();
		String buttonId = null;
		
		String label = "";
		String heading = "Edit Text Field";
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean fullScreen = preferences.getBoolean(Constants.FULL_SCREEN_PREF, false);
		if (fullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.edit_text);
		
		if (bundle == null) {
			Toast.makeText(this, "Can't edit text without at least some basic data", Toast.LENGTH_LONG).show();
			finish();
		}
		else {
			
			// Wire up the data entry field
			EditText editText = (EditText) findViewById(R.id.editTextEditTextField);

			SoundButton.SerializableSoundButton tempSerializableSoundButton = (SoundButton.SerializableSoundButton) bundle.get(SoundButton.BUTTON_BUNDLE);
			tempButton = tempSerializableSoundButton.getSoundButton();
			textType = bundle.getInt(TEXT_TYPE);
			
			// Wire up the preview button
			soundButtonView = (SoundButtonView) findViewById(R.id.editTextPreviewButton);
			soundButtonView.setSoundButton(tempButton);
			soundButtonView.invalidate();
			
			if (SoundButton.LABEL_TEXT_TYPE == textType) {
				heading = "Edit Button Label";
				label = "Enter the text that will appear on the button. (Must be between 1 and " + Constants.MAX_LABEL_LENGTH + " characters long.)";
				editText.setText(tempButton.getLabel());
				editText.addTextChangedListener(new UpdateButtonTextWatcher(SoundButton.LABEL_TEXT_TYPE));
			}
			else if (SoundButton.TTS_TEXT_TYPE == textType) {
				tempButton.setSaveTtsToFile(false);
				heading = "Edit Text to Speak";
				label = "Enter the text that will be spoken when the button is pressed.";
				editText.setText(tempButton.getTtsText());
				editText.addTextChangedListener(new UpdateButtonTextWatcher(SoundButton.TTS_TEXT_TYPE));
			}
			else {
				Toast.makeText(this, "Can't edit text without at least some basic data", Toast.LENGTH_LONG).show();
				finish();
			}
			
			// Wire up the screen heading
			TextView editTextHeading = (TextView) findViewById(R.id.editTextHeading);
			editTextHeading.setText(heading);
			
			// Wire up the label
			TextView editTextLabel = (TextView) findViewById(R.id.editTextLabel);
			editTextLabel.setText(label);
			
			
			// Wire up the cancel button
			Button cancelButton = (Button) findViewById(R.id.CancelEdit);
			cancelButton.setOnClickListener(new ActivityCancelListener());
			
			// Wire up the save button
			Button saveButton = (Button) findViewById(R.id.SaveEdit);
			saveButton.setOnClickListener(new SaveEditListener(this));
		}
	}

	private class ActivityCancelListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
		}
	}
	
	private class SaveEditListener implements OnClickListener {
		private Activity activity;
		
		public SaveEditListener(Activity activity) {
			super();
			this.activity = activity;
		}

		@Override
		public void onClick(View v) {
			// We can't just save here, as this may be a new button.  We have to pass the updated information back to the calling activity
			Intent returnedIntent = new Intent();
			if (SoundButton.TTS_TEXT_TYPE == textType) {
				returnedIntent.putExtra(SoundButton.TTS_TEXT, tempButton.getTtsText());
				setResult(TTS_TEXT_UPDATED,returnedIntent);
			}
			else {
				returnedIntent.putExtra(SoundButton.LABEL, tempButton.getLabel());
				setResult(LABEL_UPDATED,returnedIntent);
				
			}
			activity.finish();
		}
	}
	
	private class UpdateButtonTextWatcher implements TextWatcher {
		private int textType;
		
		public UpdateButtonTextWatcher(int textType) {
			this.textType = textType;
		}

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (SoundButton.TTS_TEXT_TYPE == textType) {
				tempButton.setTtsText(String.valueOf(s));
			}
			else if (SoundButton.LABEL_TEXT_TYPE == textType) {
				tempButton.setLabel(String.valueOf(s));
			}
			soundButtonView.reload();
		}
	}
}
