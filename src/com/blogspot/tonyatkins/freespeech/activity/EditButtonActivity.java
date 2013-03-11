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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.adapter.TabSpinnerAdapter;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityLaunchListener;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.FileUtils;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;
import com.blogspot.tonyatkins.freespeech.view.ColorSwatch;
import com.blogspot.tonyatkins.picker.activity.FilePickerActivity;
import com.blogspot.tonyatkins.picker.adapter.FileIconListAdapter;
import com.blogspot.tonyatkins.recorder.activity.RecordSoundActivity;

public class EditButtonActivity extends FreeSpeechActivity {
	public static final int ADD_BUTTON = 0;
	public static final int EDIT_BUTTON = 1;
	private static final int MICROPHONE_REQUEST = 1234;
	private static final int CAMERA_REQUEST = 2345;
	private static final int GALLERY_REQUEST = 3456;
	private static final int CROP_REQUEST = 6543;

	private SoundButton tempButton;
	private boolean isNewButton = false;
	private DbAdapter dbAdapter;

	private ColorSwatch colorPickerButton;
	private SoundReferee soundReferee;
	private ImageView buttonImageView;
	private TextView buttonSoundFileTextView;
	private EditText buttonLabelEdit;
	private EditText buttonTtsEdit;
	private final LabelChangedListener labelWatcher = new LabelChangedListener();
	private final TtsChangedListener ttsWatcher  = new TtsChangedListener();

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		dbAdapter = new DbAdapter(this);
		
		soundReferee = new SoundReferee(this);

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

		// Wire up the image gallery button
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		ImageButton galleryButton = (ImageButton) findViewById(R.id.editButtonGalleryButton);
		galleryButton.setOnClickListener(new ActivityLaunchListener(this, GALLERY_REQUEST, galleryIntent));

		// Wire up the camera button
		ImageButton cameraButton = (ImageButton) findViewById(R.id.editButtonCameraButton);
		cameraButton.setOnClickListener(new ActivityLaunchListener(this, CAMERA_REQUEST, new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)));

		// Wire up the crop button for existing images
		ImageButton cropButton = (ImageButton) findViewById(R.id.editButtonCropButton);
		cropButton.setOnClickListener(new CropClickListener());
		
		// Wire up the image rotation buttons
		ImageButton rotateCwButton = (ImageButton) findViewById(R.id.editButtonRotateClockwiseButton);
		rotateCwButton.setOnClickListener(new RotateOnClickListener(true));
		
		ImageButton rotateCcwButton = (ImageButton) findViewById(R.id.editButtonRotateCounterClockwiseButton);
		rotateCcwButton.setOnClickListener(new RotateOnClickListener(false));

		
		// Wire up the sound file picker button
		Intent soundPickerIntent = new Intent(this, FilePickerActivity.class);
		soundPickerIntent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.SOUND_FILE_TYPE);
		soundPickerIntent.putExtra(FilePickerActivity.CWD_BUNDLE, tempButton.getSoundPath());

		Button soundPickerButton = (Button) findViewById(R.id.editButtonSoundFileButton);
		soundPickerButton.setOnClickListener(new ActivityLaunchListener(this, FilePickerActivity.REQUEST_CODE, soundPickerIntent));

		// wire up the recorder button
		Intent recordSoundIntent = new Intent(this, RecordSoundActivity.class);
		recordSoundIntent.putExtra(RecordSoundActivity.FILE_NAME_KEY, tempButton.getLabel());
		ImageButton recordSoundButton = (ImageButton) findViewById(R.id.editButtonMicrophoneButton);
		recordSoundButton.setOnClickListener(new ActivityLaunchListener(this, RecordSoundActivity.REQUEST_CODE, recordSoundIntent));

		// Wire up the color picker and display the selected color
		Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
		colorPickerIntent.putExtra(SoundButton.BUTTON_BUNDLE, tempButton.getSerializable());

		colorPickerButton = (ColorSwatch) findViewById(R.id.buttonBgColorColorSwatch);
		colorPickerButton.setOnClickListener(new ActivityLaunchListener(this, ColorPickerActivity.REQUEST_CODE, colorPickerIntent));

		// Wire up label editing
		buttonLabelEdit = (EditText) findViewById(R.id.editButtonEditLabel);
		
		// Wire up the button image
		buttonImageView = (ImageView) findViewById(R.id.editButtonImage);
		
		// Wire up TTS editing
		buttonTtsEdit = (EditText) findViewById(R.id.editButtonEditTtsText);
		
		// Wire up the Sound file label
		buttonSoundFileTextView = (TextView) findViewById(R.id.editButtonCurrentSound);

		reloadButtonData();
		
		loadTabSpinner();
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.buttonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());

		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.buttonPanelSaveButton);
		saveButton.setOnClickListener(new SaveListener(this));
	}

	private void loadTabSpinner() {
		Set<Tab> tabs = dbAdapter.fetchAllTabs();

		// Add a "none" tab that appears before all of the rest
		Tab dummyTab = new Tab(Tab.NO_ID,"Do not change tabs");
		dummyTab.setSortOrder(-999);
		tabs.add(dummyTab);
		
		TabSpinnerAdapter adapter = new TabSpinnerAdapter(this,tabs);
		Spinner tabSpinner = (Spinner) findViewById(R.id.editButtonTabSpinner);
		tabSpinner.setAdapter(adapter);

		tabSpinner.setOnItemSelectedListener(new LinkedTabSelectedListener());
		
		if (tempButton.getLinkedTabId() != Tab.NO_ID) {
			Object[] tabArray = tabs.toArray();
			int dropDownPosition = 0;
			for (int position = 0; position < tabArray.length; position ++) {
				Tab tab = (Tab) tabArray[position];
				if (tab.getId() == tempButton.getLinkedTabId()) {
					dropDownPosition = position;
					break;
				}
			}
			tabSpinner.setSelection(dropDownPosition);
		}
	}

	private class LinkedTabSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> spinner, View selectedView, int position, long tabId) {
			tempButton.setLinkedTabId(tabId);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			tempButton.setLinkedTabId(Tab.NO_ID);
		}
	}
	
	private void reloadButtonData() {
		buttonLabelEdit.removeTextChangedListener(labelWatcher);
		buttonTtsEdit.removeTextChangedListener(ttsWatcher);

		if (tempButton.getSoundPath() == null) {
			buttonSoundFileTextView.setText(R.string.edit_button_say_sound_current_sound);
		}
		else {
			buttonSoundFileTextView.setText(tempButton.getSoundFileName());
		}

		buttonTtsEdit.setText(tempButton.getTtsText());
		buttonLabelEdit.setText(tempButton.getLabel());

		boolean hasImage = false;
		if (tempButton.getImageResource() != SoundButton.NO_RESOURCE) {
			buttonImageView.setBackgroundResource(tempButton.getImageResource());
			hasImage =true;
		}
		else if (tempButton.getImagePath() != null){
			buttonImageView.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeFile(tempButton.getImagePath())));
			hasImage =true;
		}
		
		View imageAdjustmentControlView = findViewById(R.id.editButtonImageAdjustmentControls);
		imageAdjustmentControlView.setVisibility(hasImage ? View.VISIBLE : View.INVISIBLE);
		
		colorPickerButton.setBackgroundColor(tempButton.getBgColor());
		
		buttonLabelEdit.addTextChangedListener(labelWatcher);
		buttonTtsEdit.addTextChangedListener(ttsWatcher);
	}

	@Override
	public void finish() {
		if (dbAdapter != null) dbAdapter.close();
		soundReferee.destroyTts();
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
			if (tempButton.getLabel() == null || tempButton.getLabel().length() <= 0)
			{

				Toast.makeText(context, "You must enter a label and either a sound file, sound resource, or tts text.", Toast.LENGTH_LONG).show();
			}
			else if (tempButton.getLabel().length() > Constants.MAX_LABEL_LENGTH)
			{
				Toast.makeText(context, "Labels can only be 15 characters or less.", Toast.LENGTH_LONG).show();
			}
			else
			{
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
					// FIXME:  This does not seem to be working
					TtsCacheUtils.rebuildTtsFile(tempButton, EditButtonActivity.this);
				}

				setResult(RESULT_OK, returnedIntent);
				finish();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null)
		{			
			Uri uri = data.getData();
			Bundle returnedBundle = data.getExtras();
			if (uri != null)
			{
				if (requestCode == RecordSoundActivity.REQUEST_CODE)
				{
					if (resultCode == Activity.RESULT_OK)
					{
						File localFile = saveSoundLocally(uri.getPath());
						tempButton.setSoundPath(localFile.getPath());
						Toast.makeText(this, "Recording created...", Toast.LENGTH_SHORT).show();
					}
				}
				else if (requestCode == FilePickerActivity.REQUEST_CODE)
				{
					if (resultCode == FilePickerActivity.FILE_SELECTED)
					{
						File localFile = saveSoundLocally(uri.getPath());
						tempButton.setSoundPath(localFile.getPath());
						Toast.makeText(this, "Sound file selected...", Toast.LENGTH_SHORT).show();
					}
				}
				else if (requestCode == GALLERY_REQUEST)
				{
					if (uri != null)
					{
						Cursor cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
						cursor.moveToFirst();
						final String imageFilePath = cursor.getString(0);
						cursor.close();

						File originalFile = new File(imageFilePath);
						File localFile = saveBitmapLocally(originalFile);
						if (localFile != null) {
							tempButton.setImagePath(localFile.getAbsolutePath());
						}
					}
				}
			}
			else
			{
				if (requestCode == CAMERA_REQUEST)
				{
					Bitmap bitmap = (Bitmap) returnedBundle.getParcelable("data");
					File localFile = saveBitmapLocally(bitmap);
					if (localFile != null) {
						tempButton.setImagePath(localFile.getAbsolutePath());
					}
				}
				else if (requestCode == CROP_REQUEST) {
					Bitmap thumbnail = (Bitmap) returnedBundle.getParcelable("data");
					File localFile = saveBitmapLocally(thumbnail);
					if (localFile != null) {
						tempButton.setImagePath(localFile.getAbsolutePath());
					}
				}				
				else if (requestCode == ColorPickerActivity.REQUEST_CODE)
				{
					if (resultCode == ColorPickerActivity.COLOR_SELECTED)
					{
						int selectedColor = returnedBundle.getInt(ColorPickerActivity.COLOR_BUNDLE);
						tempButton.setBgColor(selectedColor);						
					}
				}
				else if (requestCode == MICROPHONE_REQUEST)
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
							FileUtils.copy(sourceFile, destFile);
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
			}
			reloadButtonData();
		}
		else
		{
			// data should never be null unless we've canceled, but oh well
		}
	}

	
	private File saveBitmapLocally(Bitmap bitmap) {
		File localFile = new File(Constants.IMAGE_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + ".png");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, output);
		
		FileOutputStream bitmapOutput;
		try
		{
			bitmapOutput = new FileOutputStream(localFile);
			bitmapOutput.write(output.toByteArray());
			bitmapOutput.close();
			Log.i(Constants.TAG, "Saved bitmap to file:" + localFile.getAbsolutePath());
			
			return localFile;
		}
		catch (Exception e)
		{
			Log.e(Constants.TAG, "Error saving picture to file:", e);
		}

		return null;
	}

	private File saveBitmapLocally(File originalFile) {
		File localFile = new File(Constants.IMAGE_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + ".png");

		if (originalFile.exists())
		{
			try
			{
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(originalFile));
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile));

				int bytes = 0;
				byte[] buffer = new byte[Constants.BUFFER_SIZE];
				while ((bytes = bis.read(buffer)) != -1)
				{
					bos.write(buffer);
				}
				bis.close();
				bos.close();

				Log.d(Constants.TAG, "Copied image file '" + originalFile.getName() + "' to '" + localFile.getAbsolutePath() + "'.");
				return localFile;
			}
			catch (Exception e)
			{
				Log.e(Constants.TAG, "Can't copy image file to final location.", e);
			}
		}
		else
		{
			Log.e(Constants.TAG, "Can't copy image file '" + originalFile.getName() + "' to our directory because it doesn't exist.");
		}
		
		return null;
	}
	
	private File saveSoundLocally(String originalFilePath) {
		File originalFile = new File(originalFilePath);
		String extension = originalFile.getName().substring(originalFile.getName().lastIndexOf("."));
		File localFile = new File(Constants.SOUND_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + extension);
		
		if (originalFile.exists())
		{
			try
			{
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(originalFile));
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile));
				
				int bytes = 0;
				byte[] buffer = new byte[Constants.BUFFER_SIZE];
				while ((bytes = bis.read(buffer)) != -1)
				{
					bos.write(buffer);
				}
				bis.close();
				bos.close();
				
				Log.d(Constants.TAG, "Copied sound file '" + originalFile.getName() + "' to '" + localFile.getAbsolutePath() + "'.");
				return localFile;
			}
			catch (Exception e)
			{
				Log.e(Constants.TAG, "Can't copy sound file to final location.", e);
			}
		}
		else
		{
			Log.e(Constants.TAG, "Can't copy sound file '" + originalFile.getName() + "' to our directory because it doesn't exist.");
		}
		
		return null;
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

	private class CropClickListener implements OnClickListener {
		public void onClick(View v) {
			cropFile(new File(tempButton.getImagePath()));
		}
	}
	
	private void rotateBitmap(boolean clockwise) {
		Bitmap bitmap = BitmapFactory.decodeFile(tempButton.getImagePath());
		if (bitmap == null) {
			Log.w(Constants.TAG, "Can't rotate, card doesn't have any image data.");
		}
		else {
			int originalWidth = bitmap.getWidth();
			int originalHeight = bitmap.getHeight();
			int newEdgeWidth = Math.max(originalWidth, originalHeight);
			
			int left = 0;
			int top = 0;
			
			if (originalHeight >= originalWidth) {
				if (!clockwise) {
					top = newEdgeWidth - originalWidth;
				}
			}
			else {
				if (clockwise) {
					left = newEdgeWidth - originalHeight;
				}
			}
			Bitmap rotatedBitmap = Bitmap.createBitmap(newEdgeWidth,newEdgeWidth,bitmap.getConfig());
			rotatedBitmap.setDensity(bitmap.getDensity());
			
			Paint paint = new Paint();
			paint.setAntiAlias(false);
			paint.setFilterBitmap(false);
			paint.setDither(false);

			Canvas canvas = new Canvas(rotatedBitmap);
			Matrix m = new Matrix();
			m.setRotate(clockwise ? 90 : -90, newEdgeWidth/2, newEdgeWidth/2);
			canvas.setDensity(bitmap.getDensity());
			canvas.setMatrix(m);
			canvas.drawBitmap(bitmap, 0,0, paint);
			canvas.setMatrix(null);
			
			Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, left, top, originalHeight, originalWidth);
						
			File outputFile = new File(tempButton.getImagePath());
			try
			{
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile), Constants.BUFFER_SIZE);
				croppedBitmap.compress(CompressFormat.PNG, 0, bos);
				bos.close();
				
				tempButton.setImagePath(outputFile.getAbsolutePath());
			}
			catch (Exception e)
			{
				Log.e(Constants.TAG, "Error rotating file '" + outputFile.getAbsolutePath() + "':", e);
			}
		}
	}
	
	private class RotateOnClickListener implements OnClickListener {
		private final boolean clockwise;
		
		public RotateOnClickListener(boolean clockwise) {
			this.clockwise = clockwise;
		}

		public void onClick(View v) {
			rotateBitmap(clockwise);
			reloadButtonData();
		}
	}
	
	private void cropFile(File originalFile) {
		final Intent intent = new Intent("com.android.camera.action.CROP");
		Uri rawFileUri = Uri.fromFile(originalFile);
		intent.setDataAndType(rawFileUri, "image/*");
		intent.putExtra("return-data", true);
//		intent.putExtra("scale", true);
		intent.putExtra("noFaceDetection", true);

		if (hasIntentActivity(intent))
		{
			Log.d(Constants.TAG, "Launching image cropping activity...");
			startActivityForResult(intent, CROP_REQUEST);
		}
		else
		{
			Log.d(Constants.TAG, "No image cropping activity available...");
		}
	}
	private boolean hasIntentActivity(Intent intent) {
		final PackageManager packageManager = EditButtonActivity.this.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	private class LabelChangedListener implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			tempButton.setLabel(s.toString());
		}
	}
	
	private class TtsChangedListener implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			tempButton.setTtsText(s.toString());
		}
	}
}
