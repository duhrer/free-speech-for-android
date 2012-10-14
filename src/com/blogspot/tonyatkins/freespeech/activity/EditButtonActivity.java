/**
 * Copyright 2012 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.model.FileIconListAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;
import com.blogspot.tonyatkins.freespeech.view.SoundButtonView;

public class EditButtonActivity extends FreeSpeechActivity  {
	private static final String PARAM_LABEL = "paramLabel";
	private static final String PARAM_CATEGORY_LABEL = "paramCategoryLabel";
	public static final int ADD_BUTTON = 0;
	public static final int EDIT_BUTTON = 1;
	
	private SoundButton tempButton;
	private boolean isNewButton = false;
	private DbAdapter dbAdapter;
	
	private SoundButtonView previewButton;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		dbAdapter = new DbAdapter(this);
		
		Bundle bundle = this.getIntent().getExtras();
		String tabId = null;
		String buttonId = null;
		
		if (bundle != null) {
			buttonId = bundle.getString(SoundButton.BUTTON_ID_BUNDLE);
			tabId = bundle.getString(Tab.TAB_ID_BUNDLE);
			
			tempButton = dbAdapter.fetchButtonById(buttonId);
		}
		
		if (tempButton == null) {
			if (tabId == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				builder.setMessage("Can't continue without knowing which tab to add a new button to.");
				builder.setPositiveButton("OK", new QuitActivityListener(this));
				builder.create().show();
			}
			isNewButton = true;
			tempButton = new SoundButton(0, null, null, SoundButton.NO_RESOURCE, SoundButton.NO_RESOURCE, Long.parseLong(tabId));
		}
						
		setContentView(R.layout.edit_button);
		

		// find and wire up the button categories, rows, and entries
		ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.editButtonExpandableList);
		
		String [] categories = {"Sound Settings","Look and Feel"};
		ArrayList<Map<String,String>> buttonParameterCategories = new ArrayList<Map<String,String>>();
		for (String category : categories) {
			TreeMap<String,String> buttonParameterMap = new TreeMap<String,String>();
			buttonParameterMap.put(PARAM_CATEGORY_LABEL, category);
			buttonParameterCategories.add(buttonParameterMap);
		}

		String[][] buttonsByCategory = {{"Text to Speak","Sound File", "Record Sound"},{"Label","Background Color", "Image"}};
		ArrayList parametersByCategory = new ArrayList<ArrayList<Map<String,String>>>();
		for (String[] buttons : buttonsByCategory) {
			ArrayList<Map> buttonParameterMap = new ArrayList<Map>();
			for (String button : buttons) {
				TreeMap<String,String> buttonLabelMap = new TreeMap<String,String>();
				buttonLabelMap.put(PARAM_LABEL, button);
				buttonParameterMap.add(buttonLabelMap);
			}
			parametersByCategory.add(buttonParameterMap);
		}
		
		
		SimpleExpandableListAdapter adapter = 
			new SimpleExpandableListAdapter(this, 
											buttonParameterCategories, 
											R.layout.edit_button_param_category_expanded, 
											R.layout.edit_button_param_category_closed, 
											new String[] {PARAM_CATEGORY_LABEL}, 
											new int[] {R.id.editButtonParamCategoryLabel}, 
											parametersByCategory, 
											R.layout.edit_button_param_entry, 
											new String[] {PARAM_LABEL}, 
											new int[] {R.id.editButtonParamLabel});

		expandableListView.setAdapter(adapter);
		expandableListView.setOnChildClickListener(new SimpleChildClickListener(this));
		expandableListView.expandGroup(0);
		
		// locate the preview button and hold onto its location
		previewButton = (SoundButtonView) findViewById(R.id.editButtonPreviewButton);
		previewButton.setSoundButton(tempButton);
		previewButton.reload();
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.buttonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());
		
		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.buttonPanelSaveButton);
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
		private Context context;
		
		public SaveListener(Context context) {
			super();
			this.context = context;
		}

		public void onClick(View arg0) {
			// Sanity check the data and open a dialog if there are problems
			if (tempButton.getLabel() == null || 
					tempButton.getLabel().length() <= 0 || 
					((tempButton.getSoundPath() == null || tempButton.getSoundPath().length() <= 0) && 
					 tempButton.getSoundResource() == SoundButton.NO_RESOURCE && 
					 (tempButton.getTtsText() == null || tempButton.getTtsText().length() <= 0 ))) {

				Toast.makeText(context, "You must enter a label and either a sound file, sound resource, or tts text.", Toast.LENGTH_LONG).show();
			}
			else if (tempButton.getLabel().length() > Constants.MAX_LABEL_LENGTH) {
				Toast.makeText(context, "Labels can only be 15 characters or less.", Toast.LENGTH_LONG).show();
			}
			else {
				try {
					if (tempButton.getBgColor() != null) {
						// test the color to make sure it's valid
						Color.parseColor(tempButton.getBgColor());
					}
					
					Intent returnedIntent = new Intent();
					
					if (isNewButton) {
						long id = dbAdapter.createButton(tempButton);
						tempButton.setId(id);
					}
					else {
						dbAdapter.updateButton(tempButton);
					}
					
					// If the tts text is set, render it to a file
					if (tempButton.getTtsText() != null && preferences.getBoolean(Constants.TTS_SAVE_PREF, false)) { 
						TtsCacheUtils.rebuildTtsFile(tempButton, EditButtonActivity.this);
					}
					
					setResult(RESULT_OK,returnedIntent);
					finish();
				}
				catch (IllegalArgumentException e) 
				{
					// catch an exception if we've been passed an invalid color
					Toast.makeText(context, "You chose an invalid color, can't continue.", Toast.LENGTH_LONG).show();
				}
			}	
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Bundle returnedBundle = data.getExtras();
			if (returnedBundle != null) {
				if (requestCode == RecordSoundActivity.REQUEST_CODE) {
					if (resultCode == RecordSoundActivity.SOUND_SAVED) {
						String soundFilePath = returnedBundle.getString(RecordSoundActivity.RECORDING_BUNDLE);
						File returnedSoundFile = new File(soundFilePath);
						if (returnedSoundFile.exists()) {
							tempButton.setSoundPath(soundFilePath);
							Toast.makeText(this, "Sound saved...", Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(this, "Error saving file!", Toast.LENGTH_LONG).show();
						}					
					}
				}
				else if (requestCode == FilePickerActivity.REQUEST_CODE) {
					if (resultCode == FilePickerActivity.FILE_SELECTED) {
						// figure out whether this is the image or sound
						int fileType = returnedBundle.getInt(FilePickerActivity.FILE_TYPE_BUNDLE);
						String path = returnedBundle.getString(FilePickerActivity.FILE_NAME_BUNDLE);
						if (fileType != 0) {
							if (fileType == FileIconListAdapter.SOUND_FILE_TYPE) {
								tempButton.setSoundPath(path);
								Toast.makeText(this, "Sound file selected...", Toast.LENGTH_SHORT).show();
							}
							else if (fileType == FileIconListAdapter.IMAGE_FILE_TYPE) {
								tempButton.setImagePath(path);
								Toast.makeText(this, "Image file selected...", Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
				else if (requestCode == ColorPickerActivity.REQUEST_CODE) {
					if (resultCode == ColorPickerActivity.COLOR_SELECTED) {
						String selectedColorString = returnedBundle.getString(ColorPickerActivity.COLOR_BUNDLE);
						setSelectedColor(selectedColorString);
					}
				}
				else if (requestCode == EditTextActivity.REQUEST_CODE) {
					if (resultCode == EditTextActivity.LABEL_UPDATED) {
						String newLabel = returnedBundle.getString(SoundButton.LABEL);
						if (tempButton.getTtsText() == null || tempButton.getTtsText().equals(tempButton.getLabel()))  {
							tempButton.setTtsText(newLabel);
						}
						tempButton.setLabel(newLabel);
					}
					else if (resultCode == EditTextActivity.TTS_TEXT_UPDATED) {
						String newTtsText = returnedBundle.getString(SoundButton.TTS_TEXT);
						if (tempButton.getLabel() == null || tempButton.getLabel().equals(tempButton.getTtsText())) {
							tempButton.setLabel(newTtsText);
						}
						tempButton.setTtsText(newTtsText);
						// There are no visible differences, so we don't need to update the display
					}
				}
			}
			else {
				// If no data is returned from the color picker, but the result is OK, it means the color is set to transparent (null)
				if (requestCode == ColorPickerActivity.REQUEST_CODE && resultCode == ColorPickerActivity.COLOR_SELECTED) {
					tempButton.setBgColor(null);
				}
			}
			previewButton.reload();
		}
		else {
			// data should never be null unless we've canceled, but oh well
		}
	}
	
	private void setSelectedColor(String selectedColorString) {
		if (selectedColorString != null) {
			try {
				// This will throw an exception if the color isn't valid
				Color.parseColor(selectedColorString);
				tempButton.setBgColor(selectedColorString);
			} catch (IllegalArgumentException e) {
				Toast.makeText(this, "Invalid color returned from color picker, ignoring.", Toast.LENGTH_LONG).show();
				tempButton.setBgColor(null);
			}
		}
		else {
			tempButton.setBgColor(null);
		}
		previewButton.reload();
	}

	private class QuitActivityListener implements android.content.DialogInterface.OnClickListener {
		private final Activity activity;
		
		public QuitActivityListener(Activity activity) {
			super();
			this.activity = activity;
		}

		public void onClick(DialogInterface dialog, int which) {
			activity.finish();
		}
	}
	
	private class SimpleChildClickListener implements ExpandableListView.OnChildClickListener {
		Intent intent;
		private Context context;
		
		public SimpleChildClickListener(Context context) {
			super();
			this.context = context;
		}

		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			// TODO: Find a better way of knitting together child views and destinations
			if  (v instanceof LinearLayout && ((LinearLayout) v).getChildCount() > 0) {
				View v2 = ((LinearLayout) v).getChildAt(0);
				if (v2 instanceof TextView) {
					String label = (String) ((TextView) v2).getText();
					
					// This is horribly brittle, but for now this is how I'll make different decisions based on the action
					if ("Text to Speak".equals(label)) {
						// launch a text editing activity to change the text to speak
						Intent intent = new Intent(context,EditTextActivity.class);
						intent.putExtra(EditTextActivity.TEXT_TYPE, SoundButton.TTS_TEXT_TYPE);
						intent.putExtra(SoundButton.BUTTON_BUNDLE,tempButton.getSerializable());
						int	requestCode = EditTextActivity.REQUEST_CODE;
						((Activity) context).startActivityForResult(intent, requestCode);
					}
					else if ("Record Sound".equals(label)) {
						// launch the sound recorder
						intent = new Intent(context,RecordSoundActivity.class);
						intent.putExtra(RecordSoundActivity.FILE_NAME_KEY, tempButton.getLabel() );
						int requestCode = RecordSoundActivity.REQUEST_CODE;
						((Activity) context).startActivityForResult(intent, requestCode);
					}
					else if ("Sound File".equals(label)) { 
						// launch the sound picker
						Intent intent = new Intent(context,FilePickerActivity.class);
						intent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.SOUND_FILE_TYPE);
						intent.putExtra(FilePickerActivity.CWD_BUNDLE, tempButton.getSoundPath());
						int	requestCode = FilePickerActivity.REQUEST_CODE;
						((Activity) context).startActivityForResult(intent, requestCode);
					}
					else if ("Label".equals(label)) {
						// launch a text editing activity to change the label
						Intent intent = new Intent(context,EditTextActivity.class);
						intent.putExtra(EditTextActivity.TEXT_TYPE, SoundButton.LABEL_TEXT_TYPE);
						intent.putExtra(SoundButton.BUTTON_BUNDLE,tempButton.getSerializable());
						int	requestCode = EditTextActivity.REQUEST_CODE;
						((Activity) context).startActivityForResult(intent, requestCode);
					}
					else if ("Background Color".equals(label)) {
						// launch the color picker
						intent = new Intent(context,ColorPickerActivity.class);
						intent.putExtra(SoundButton.BUTTON_BUNDLE,tempButton.getSerializable());
						int requestCode = ColorPickerActivity.REQUEST_CODE;
						((Activity) context).startActivityForResult(intent, requestCode);
					}
					else if ("Image".equals(label)) {
						// launch the image picker
						Intent intent = new Intent(context,FilePickerActivity.class);
						intent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.IMAGE_FILE_TYPE);
						intent.putExtra(FilePickerActivity.CWD_BUNDLE, tempButton.getImagePath());
						int requestCode = FilePickerActivity.REQUEST_CODE;
						((Activity) context).startActivityForResult(intent, requestCode);
					}
				}
			
//				// FIXME: Add a picker for built-in image resources
//				// FIXME: Add a picker for built-in sound resources

				return true;
			}
			
			return false;
		}
	}
}
