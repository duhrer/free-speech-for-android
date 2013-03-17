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
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.view.ColorSwatch;
import com.blogspot.tonyatkins.freespeech.watchers.TabLabelTextUpdateWatcher;
import com.blogspot.tonyatkins.picker.activity.FilePickerActivity;
import com.blogspot.tonyatkins.recorder.activity.RecordSoundActivity;

public class EditTabActivity extends FreeSpeechActivity {
	public static final int ADD_TAB = 6;
	public static final int EDIT_TAB = 7;
	
	private Tab tempTab;
	private DbAdapter dbAdapter;
	private boolean isNewTab = false;
	private ColorSwatch colorSwatch;
	
	public void onCreate(Bundle icicle) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean fullScreen = preferences.getBoolean(Constants.FULL_SCREEN_PREF, false);
		if (fullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		super.onCreate(icicle);
		dbAdapter = new DbAdapter(this);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String existingTabId = bundle.getString(Tab.TAB_ID_BUNDLE);
			
			// create a temporary button that we will only return on a successful save.
			if (existingTabId != null && existingTabId.length() > 0) {
				tempTab = TabDbAdapter.fetchTabById(existingTabId,dbAdapter.getDb());
			}
		}
		
		if (tempTab == null) {
			isNewTab = true;
			tempTab = new Tab(Tab.NO_ID, "");
		}
				
		setContentView(R.layout.edit_tab);
		
		// wire up the label editing
		EditText labelEditText = (EditText) findViewById(R.id.tabLabelEditText);
		labelEditText.setText(tempTab.getLabel());
		labelEditText.addTextChangedListener(new TabLabelTextUpdateWatcher(tempTab));
		
		// wire up the background color editing
		colorSwatch = (ColorSwatch) findViewById(R.id.tabBgColorColorSwatch);
		colorSwatch.setBackgroundColor(Color.TRANSPARENT);
		colorSwatch.setBackgroundColor(tempTab.getBgColor());
		
		// launch a color picker activity when this view is clicked
		Bundle pickColorBundle = new Bundle();
		pickColorBundle.putInt(ColorPickerActivity.COLOR_BUNDLE, tempTab.getBgColor());
		colorSwatch.setOnClickListener(new LaunchIntentListener(this, ColorPickerActivity.class, pickColorBundle));

		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.tabButtonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());
		
		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.tabButtonPanelSaveButton);
		saveButton.setOnClickListener(new SaveListener(this));
	}

	@Override
	public void finish() {
		if (dbAdapter != null) dbAdapter.close();
		super.finish();
	}

	private class CancelListener implements OnClickListener {
		public void onClick(View arg0) {
			finish();
		}
	}
	
	private class SaveListener implements OnClickListener {
		private final Context context;
		
		public SaveListener(Context context) {
			this.context = context;
		}
		public void onClick(View arg0) {
			// Sanity check the data and open a dialog if there are problems
			if (tempTab.getLabel() == null || tempTab.getLabel().length() <= 0) 
			{
					Toast.makeText(context, "Can't continue without a tab label", Toast.LENGTH_LONG).show();
			}
			else 
			{
				Intent returnedIntent = new Intent();
				boolean saveSuccessful;
				if (isNewTab) {
					Long tabId = TabDbAdapter.createTab(tempTab,dbAdapter.getDb());
					saveSuccessful = tabId != -1;
					Bundle bundle = new Bundle();
					bundle.putString(Tab.TAB_ID_BUNDLE, String.valueOf(tabId));
					returnedIntent.putExtras(bundle);
				}
				else {
					saveSuccessful = TabDbAdapter.updateTab(tempTab,dbAdapter.getDb());
				}
				
				if (saveSuccessful) {
					setResult(RESULT_OK,returnedIntent);
					finish();
				}
				else {
					Toast.makeText(context, "There was an error saving this tab.", Toast.LENGTH_LONG).show();
				}
			}	
		}
	}
	
	
	
	private class LaunchIntentListener implements OnClickListener {
		private Context context;
		private Class launchActivityClass;
		private Bundle bundle;
		
		public LaunchIntentListener(Context context, Class launchActivityClass, Bundle bundle) {
			this.context = context;
			this.launchActivityClass = launchActivityClass;
			this.bundle = bundle;
		}

		public void onClick(View v) {
			Intent intent = new Intent(context,launchActivityClass);
			intent.putExtras(bundle);
			int requestCode = 0;
			
			if (launchActivityClass.equals(RecordSoundActivity.class)) {
				requestCode = RecordSoundActivity.REQUEST_CODE;
			}
			else if (launchActivityClass.equals(FilePickerActivity.class)) {
				requestCode = FilePickerActivity.REQUEST_CODE;
			}
			else if (launchActivityClass.equals(ColorPickerActivity.class)) {
				requestCode = ColorPickerActivity.REQUEST_CODE;
			}
			
			startActivityForResult(intent, requestCode);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Bundle returnedBundle = data.getExtras();
			if (returnedBundle != null) {
				if (requestCode == ColorPickerActivity.REQUEST_CODE && resultCode == ColorPickerActivity.COLOR_SELECTED) {
						int selectedColor = returnedBundle.getInt(ColorPickerActivity.COLOR_BUNDLE);
						colorSwatch.setBackgroundColor(selectedColor);
						tempTab.setBgColor(selectedColor);
				}
			}
			else {
				// If no data is returned from the color picker, but the result is OK, it means the color is set to transparent (null)
				if (requestCode == ColorPickerActivity.REQUEST_CODE && resultCode == ColorPickerActivity.COLOR_SELECTED) {
					colorSwatch.setBackgroundColor(Color.TRANSPARENT);
					tempTab.setBgColor(Color.TRANSPARENT);
				}
			}
		}
	}

}
