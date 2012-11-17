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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityLaunchListener;
import com.blogspot.tonyatkins.freespeech.model.FileIconListAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.FileUtils;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;
import com.blogspot.tonyatkins.freespeech.view.ColorSwatch;
import com.blogspot.tonyatkins.freespeech.view.SoundButtonView;
import com.google.common.io.Files;

public class EditButtonActivity extends FreeSpeechActivity {
	public static final int ADD_BUTTON = 0;
	public static final int EDIT_BUTTON = 1;
	private static final int MICROPHONE_REQUEST = 1234;
	private static final int CAMERA_REQUEST = 2345;
	private static final int GALLERY_REQUEST = 3456;


	private SoundButton tempButton;
	private boolean isNewButton = false;
	private DbAdapter dbAdapter;

	private SoundButtonView previewButton;
	private ColorSwatch colorPickerButton;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		dbAdapter = new DbAdapter(this);

		Bundle bundle = this.getIntent().getExtras();
		String tabId = null;
		String buttonId = null;

		if (bundle != null)
		{
			buttonId = bundle.getString(SoundButton.BUTTON_ID_BUNDLE);
			tabId = bundle.getString(Tab.TAB_ID_BUNDLE);

			tempButton = dbAdapter.fetchButtonById(buttonId);
		}

		if (tempButton == null)
		{
			if (tabId == null)
			{
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

		// Wire up the text editing button
		Intent editIntent = new Intent(this, EditTextActivity.class);
		editIntent.putExtra(EditTextActivity.TEXT_TYPE, SoundButton.TTS_TEXT_TYPE);
		editIntent.putExtra(SoundButton.BUTTON_BUNDLE, tempButton.getSerializable());
		
		Button editTextButton = (Button) findViewById(R.id.editButtonTtsText);
		editTextButton.setOnClickListener(new ActivityLaunchListener(this, EditTextActivity.REQUEST_CODE, editIntent));
		
		// Wire up the label editing button
		Intent editLabelIntent = new Intent(this, EditTextActivity.class);
		editLabelIntent.putExtra(EditTextActivity.TEXT_TYPE, SoundButton.LABEL_TEXT_TYPE);
		editLabelIntent.putExtra(SoundButton.BUTTON_BUNDLE, tempButton.getSerializable());

		Button editLabelButton = (Button) findViewById(R.id.editButtonLabelText);
		editLabelButton.setOnClickListener(new ActivityLaunchListener(this, EditTextActivity.REQUEST_CODE, editLabelIntent));
		
		// Wire up the image file picker button
		Intent imagePickerIntent = new Intent(this, FilePickerActivity.class);
		imagePickerIntent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.IMAGE_FILE_TYPE);
		imagePickerIntent.putExtra(FilePickerActivity.CWD_BUNDLE, tempButton.getImagePath());

		Button imageFilePickerButton = (Button) findViewById(R.id.editButtonImageFileButton);
		imageFilePickerButton.setOnClickListener(new ActivityLaunchListener(this, FilePickerActivity.REQUEST_CODE, imagePickerIntent));
		
		
		// Wire up the image gallery button
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		ImageButton galleryButton = (ImageButton) findViewById(R.id.editButtonGalleryButton);
		galleryButton.setOnClickListener(new ActivityLaunchListener(this, GALLERY_REQUEST, galleryIntent));
		
		// Wire up the camera button
		ImageButton cameraButton = (ImageButton) findViewById(R.id.editButtonCameraButton);
		cameraButton.setOnClickListener(new ActivityLaunchListener(this,CAMERA_REQUEST,new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)));
		
		// Wire up the sound file picker button
		Intent soundPickerIntent = new Intent(this, FilePickerActivity.class);
		soundPickerIntent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.SOUND_FILE_TYPE);
		soundPickerIntent.putExtra(FilePickerActivity.CWD_BUNDLE, tempButton.getSoundPath());

		Button soundPickerButton = (Button) findViewById(R.id.editButtonSoundFileButton);
		soundPickerButton.setOnClickListener(new ActivityLaunchListener(this, FilePickerActivity.REQUEST_CODE, soundPickerIntent));
		
		//wire up the recorder button
		Intent recordSoundIntent = new Intent(this,RecordSoundActivity.class);
		recordSoundIntent.putExtra(RecordSoundActivity.FILE_NAME_KEY, tempButton.getLabel() );
		ImageButton recordSoundButton = (ImageButton) findViewById(R.id.editButtonMicrophoneButton);
		recordSoundButton.setOnClickListener(new ActivityLaunchListener(this, RecordSoundActivity.REQUEST_CODE, recordSoundIntent));

		// Wire up the color picker and display the selected color
		Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
		colorPickerIntent.putExtra(SoundButton.BUTTON_BUNDLE, tempButton.getSerializable());

		colorPickerButton = (ColorSwatch) findViewById(R.id.buttonBgColorColorSwatch);
		colorPickerButton.setOnClickListener(new ActivityLaunchListener(this, ColorPickerActivity.REQUEST_CODE, colorPickerIntent));
		updateColorSwatch();
		
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

	private void updateColorSwatch() {
		colorPickerButton.setBackgroundColor(Color.TRANSPARENT);
		try {
			if (tempButton.getBgColor() != null) {
				colorPickerButton.setBackgroundColor(Color.parseColor(tempButton.getBgColor()));
			}
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, "The current color is invalid and will not be displayed.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void finish() {
		if (dbAdapter != null)
			dbAdapter.close();
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
			if (tempButton.getLabel() == null || tempButton.getLabel().length() <= 0 || ((tempButton.getSoundPath() == null || tempButton.getSoundPath().length() <= 0) && tempButton.getSoundResource() == SoundButton.NO_RESOURCE && (tempButton.getTtsText() == null || tempButton.getTtsText().length() <= 0)))
			{

				Toast.makeText(context, "You must enter a label and either a sound file, sound resource, or tts text.", Toast.LENGTH_LONG).show();
			}
			else if (tempButton.getLabel().length() > Constants.MAX_LABEL_LENGTH)
			{
				Toast.makeText(context, "Labels can only be 15 characters or less.", Toast.LENGTH_LONG).show();
			}
			else
			{
				try
				{
					if (tempButton.getBgColor() != null)
					{
						// test the color to make sure it's valid
						Color.parseColor(tempButton.getBgColor());
					}

					Intent returnedIntent = new Intent();

					if (isNewButton)
					{
						long id = dbAdapter.createButton(tempButton);
						tempButton.setId(id);
					}
					else
					{
						dbAdapter.updateButton(tempButton);
					}

					// If the tts text is set, render it to a file
					if (tempButton.getTtsText() != null && preferences.getBoolean(Constants.TTS_SAVE_PREF, false))
					{
						TtsCacheUtils.rebuildTtsFile(tempButton, EditButtonActivity.this);
					}

					setResult(RESULT_OK, returnedIntent);
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
		if (data != null)
		{
			Bundle returnedBundle = data.getExtras();
			if (returnedBundle != null)
			{
				if (requestCode == RecordSoundActivity.REQUEST_CODE)
				{
					if (resultCode == RecordSoundActivity.SOUND_SAVED)
					{
						String soundFilePath = returnedBundle.getString(RecordSoundActivity.RECORDING_BUNDLE);
						File returnedSoundFile = new File(soundFilePath);
						if (returnedSoundFile.exists())
						{
							tempButton.setSoundPath(soundFilePath);
							Toast.makeText(this, "Sound saved...", Toast.LENGTH_SHORT).show();
						}
						else
						{
							Toast.makeText(this, "Error saving file!", Toast.LENGTH_LONG).show();
						}
					}
				}
				else if (requestCode == FilePickerActivity.REQUEST_CODE)
				{
					if (resultCode == FilePickerActivity.FILE_SELECTED)
					{
						// figure out whether this is the image or sound
						int fileType = returnedBundle.getInt(FilePickerActivity.FILE_TYPE_BUNDLE);
						String path = returnedBundle.getString(FilePickerActivity.FILE_NAME_BUNDLE);
						if (fileType != 0)
						{
							if (fileType == FileIconListAdapter.SOUND_FILE_TYPE)
							{
								tempButton.setSoundPath(path);
								Toast.makeText(this, "Sound file selected...", Toast.LENGTH_SHORT).show();
							}
							else if (fileType == FileIconListAdapter.IMAGE_FILE_TYPE)
							{
								tempButton.setImagePath(path);
								Toast.makeText(this, "Image file selected...", Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
				else if (requestCode == ColorPickerActivity.REQUEST_CODE)
				{
					if (resultCode == ColorPickerActivity.COLOR_SELECTED)
					{
						String selectedColorString = returnedBundle.getString(ColorPickerActivity.COLOR_BUNDLE);
						setSelectedColor(selectedColorString);
						updateColorSwatch();
					}
				}
				else if (requestCode == EditTextActivity.REQUEST_CODE)
				{
					if (resultCode == EditTextActivity.LABEL_UPDATED)
					{
						String newLabel = returnedBundle.getString(SoundButton.LABEL);
						if ((tempButton.getTtsText() == null || tempButton.getTtsText().equals(tempButton.getLabel())) && tempButton.getSoundPath() == null)
						{
							tempButton.setTtsText(newLabel);
						}
						tempButton.setLabel(newLabel);
					}
					else if (resultCode == EditTextActivity.TTS_TEXT_UPDATED)
					{
						String newTtsText = returnedBundle.getString(SoundButton.TTS_TEXT);
						if (tempButton.getLabel() == null || tempButton.getLabel().equals(tempButton.getTtsText()))
						{
							tempButton.setLabel(newTtsText);
						}
						tempButton.setTtsText(newTtsText);
						// There are no visible differences, so we don't need to
						// update the display
					}
				}
				else if (requestCode == CAMERA_REQUEST) {
					Bitmap thumbnail = (Bitmap) returnedBundle.get("data");	
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					thumbnail.compress(CompressFormat.PNG, 100, output);
	
					File bitmapFile = new File(Constants.IMAGE_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + ".png");
					FileOutputStream bitmapOutput;
					try {
						bitmapOutput = new FileOutputStream(bitmapFile);
						bitmapOutput.write(output.toByteArray());
						bitmapOutput.close();
						tempButton.setImagePath(bitmapFile.getAbsolutePath());
						Log.i(Constants.TAG, "Saved bitmap to file:" + bitmapFile.getAbsolutePath());
					} catch (Exception e) {
						Log.e(Constants.TAG, "Error saving picture to file:", e);
					}
				}
			}
			else
			{
				// FIXME:  This doesn't seem to work, at least with the HTC voice recorder.
				if (requestCode == MICROPHONE_REQUEST)
				{
					Uri audioUri = null;
					audioUri = data.getData();
					if (audioUri != null)
					{
						File sourceFile = new File(audioUri.getPath());
						String destFilePath = Constants.SOUND_DIRECTORY + "/" + sourceFile.getName();
						File destFile = new File(destFilePath);
						try
						{
							Files.copy(sourceFile, destFile);
							tempButton.setSoundPath(destFile.getAbsolutePath());
						}
						catch (IOException e)
						{
							String message = "Can't copy recording to working directory.";
							Log.e(Constants.TAG, message, e);
							Toast.makeText(this, message, Toast.LENGTH_LONG);
						}
					}
					else
					{
						Toast.makeText(this, "No data returned or sound recorder failed to launch.", Toast.LENGTH_LONG).show();
					}
				}
				else if (requestCode == GALLERY_REQUEST) {
					Uri _uri = data.getData();
					
					if (_uri != null) {
						Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
						cursor.moveToFirst();
						final String imageFilePath = cursor.getString(0);
						cursor.close();

						File bitmapFile = new File(Constants.IMAGE_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + ".png");

						File originalFile = new File(imageFilePath);
						if (originalFile.exists()) {
							try {
								BufferedInputStream bis = new BufferedInputStream(new FileInputStream(originalFile));
								BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(bitmapFile));
								
								int bytes = 0;
								byte[] buffer = new byte[Constants.BUFFER_SIZE];
								while ((bytes = bis.read(buffer)) != -1) {
									bos.write(buffer);
								}
								bis.close();
								bos.close();

								tempButton.setImagePath(bitmapFile.getAbsolutePath());
								Log.d(Constants.TAG, "Copied gallery file '" + imageFilePath + "' to '" + bitmapFile.getAbsolutePath() + "'.");
							} catch (Exception e) {
								Log.e(Constants.TAG, "Can't copy gallery file to final location.", e);
							}
						}
						else {
							Log.e(Constants.TAG, "Can't copy gallery file '" + imageFilePath + "' to our directory because it doesn't exist.");
						}
					}
				}

				// If no data is returned from the color picker, but the result
				// is OK, it means the color is set to transparent (null)
				else if (requestCode == ColorPickerActivity.REQUEST_CODE && resultCode == ColorPickerActivity.COLOR_SELECTED)
				{
					tempButton.setBgColor(null);
				}
			}
			previewButton.reload();
		}
		else
		{
			// data should never be null unless we've canceled, but oh well
		}
	}

	private void setSelectedColor(String selectedColorString) {
		if (selectedColorString != null)
		{
			try
			{
				// This will throw an exception if the color isn't valid
				Color.parseColor(selectedColorString);
				tempButton.setBgColor(selectedColorString);
			}
			catch (IllegalArgumentException e)
			{
				Toast.makeText(this, "Invalid color returned from color picker, ignoring.", Toast.LENGTH_LONG).show();
				tempButton.setBgColor(null);
			}
		}
		else
		{
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

}
