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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.adapter.TabSpinnerAdapter;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityLaunchListener;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.FileUtils;
import com.blogspot.tonyatkins.freespeech.utils.I18nUtils;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;
import com.blogspot.tonyatkins.freespeech.view.ColorSwatch;
import com.blogspot.tonyatkins.picker.activity.FilePickerActivity;
import com.blogspot.tonyatkins.picker.adapter.FileIconListAdapter;
import com.blogspot.tonyatkins.recorder.activity.RecordSoundActivity;

import org.apache.commons.lang.StringUtils;

public class EditButtonActivity extends FreeSpeechActivity {
	public static final int ADD_BUTTON = 0;
	public static final int EDIT_BUTTON = 1;
	private static final int MICROPHONE_REQUEST = 1234;
	private static final int CAMERA_REQUEST = 2345;
	private static final int GALLERY_REQUEST = 3456;
	private static final int CROP_REQUEST = 6543;
    private static final int KITKAT_GALLERY_REQUEST = 6453;

    private SoundButton tempButton;
	private boolean isNewButton = false;

	private ColorSwatch colorPickerButton;
	private SoundReferee soundReferee;
	private ImageView buttonImageView;
	private TextView buttonSoundFileTextView;
	private EditText buttonLabelEdit;
	private EditText buttonTtsEdit;
	private final LabelChangedListener labelWatcher = new LabelChangedListener();
	private final TtsChangedListener ttsWatcher  = new TtsChangedListener();
    private RadioButton speakRadioButton;
    private RadioButton soundRadioButton;
    private View speakControls;
    private View soundControls;

    public void onCreate(Bundle icicle) {
		super.onCreate(icicle);


		soundReferee = new SoundReferee(this);

		Bundle bundle = this.getIntent().getExtras();
		String tabId = null;
		String buttonId;

		if (bundle != null)
		{
			buttonId = bundle.getString(SoundButton.BUTTON_ID_BUNDLE);
			tabId = bundle.getString(Tab.TAB_ID_BUNDLE);

            DbOpenHelper helper = new DbOpenHelper(this);
            SQLiteDatabase db = helper.getReadableDatabase();
			tempButton = SoundButtonDbAdapter.fetchButtonById(buttonId, db);
            db.close();
		}

		if (tempButton == null)
		{
			if (tabId == null)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				builder.setMessage("Can't continue without knowing which tab to add a new button to.");
				builder.setPositiveButton("OK", new QuitActivityListener());
				builder.create().show();
			}
			isNewButton = true;
			tempButton = new SoundButton(0L, null, null, SoundButton.NO_RESOURCE, SoundButton.NO_RESOURCE, Long.parseLong(tabId));
		}

		setContentView(R.layout.edit_button);

		// Wire up the image gallery button
		ImageButton galleryButton = (ImageButton) findViewById(R.id.editButtonGalleryButton);

        // pre-KitKat
        if (Build.VERSION.SDK_INT < 19) {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            galleryButton.setOnClickListener(new ActivityLaunchListener(this, GALLERY_REQUEST, galleryIntent));
        }
        // KitKat or higher, we can't currently handle what their gallery returns
        else {
            Intent galleryIntent = new Intent(com.blogspot.tonyatkins.picker.Constants.ACTION_PICK_FILE);
            galleryIntent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.IMAGE_FILE_TYPE);
            galleryButton.setOnClickListener(new ActivityLaunchListener(this, KITKAT_GALLERY_REQUEST, galleryIntent));
        }

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
		if (tempButton.getSoundPath() != null) {
			File soundFile = new File(tempButton.getSoundPath());
			soundPickerIntent.putExtra(FilePickerActivity.CWD_BUNDLE, soundFile.getAbsolutePath());
		}
		else {
			soundPickerIntent.putExtra(FilePickerActivity.CWD_BUNDLE, Environment.getExternalStorageDirectory());
		}

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

        // Wire up the radio button for speaking text
        speakRadioButton = (RadioButton) findViewById(R.id.editButtonSpeakTextButton);
        speakRadioButton.setOnClickListener(new ClickSpeakButtonListener());
        speakControls = buttonTtsEdit;

        // Wire up the radio button for playing a sound
        soundRadioButton = (RadioButton) findViewById(R.id.editButtonPlaySoundButton);
        soundRadioButton.setOnClickListener(new ClickSoundButtonListener());
        soundControls = findViewById(R.id.editButtonSoundControls);

		reloadButtonData();
		
		loadTabSpinner();
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.buttonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());

		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.buttonPanelSaveButton);
		saveButton.setOnClickListener(new SaveListener(this));
	}

    private void chooseSpeakButton() {
        speakRadioButton.setChecked(true);
        speakControls.setVisibility(View.VISIBLE);
        soundRadioButton.setChecked(false);
        soundControls.setVisibility(View.GONE);
    }


    private void chooseSoundButton() {
        soundRadioButton.setChecked(true);
        soundControls.setVisibility(View.VISIBLE);
        speakRadioButton.setChecked(false);
        speakControls.setVisibility(View.GONE);
    }

    private class ClickSpeakButtonListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            chooseSpeakButton();
        }
    }

    private class ClickSoundButtonListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            chooseSoundButton();
        }
    }

	private void loadTabSpinner() {
        DbOpenHelper helper = new DbOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
		Set<Tab> tabs = TabDbAdapter.fetchAllTabs(db);
        db.close();

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

		buttonTtsEdit.setText(I18nUtils.getText(this,tempButton.getTtsText()));
		buttonLabelEdit.setText(I18nUtils.getText(this,tempButton.getLabel()));

		boolean hasImage = false;
		if (tempButton.getImageResource() != SoundButton.NO_RESOURCE) {
			buttonImageView.setBackgroundResource(tempButton.getImageResource());
			hasImage =true;
		}
		else if (tempButton.getImagePath() != null){
			buttonImageView.setImageBitmap(BitmapFactory.decodeFile(tempButton.getImagePath()));
			hasImage =true;
		}
		
		View imageAdjustmentControlView = findViewById(R.id.editButtonImageAdjustmentControls);
		imageAdjustmentControlView.setVisibility(hasImage ? View.VISIBLE : View.INVISIBLE);
		
		colorPickerButton.setBackgroundColor(tempButton.getBgColor());
		
		buttonLabelEdit.addTextChangedListener(labelWatcher);
		buttonTtsEdit.addTextChangedListener(ttsWatcher);

        if (StringUtils.isEmpty(tempButton.getTtsText()) && StringUtils.isEmpty(tempButton.getSoundPath())) {
            soundControls.setVisibility(View.GONE);
            speakControls.setVisibility(View.GONE);
        }
        else if (!StringUtils.isEmpty(tempButton.getSoundPath())) {
            chooseSoundButton();
        }
        else if (!StringUtils.isEmpty(tempButton.getTtsText())) {
            chooseSpeakButton();
        }
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

                DbOpenHelper helper = new DbOpenHelper(EditButtonActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
				if (isNewButton)
				{
					long id = SoundButtonDbAdapter.createButton(tempButton,db);
					tempButton.setId(id);
				}
				else
				{
					SoundButtonDbAdapter.updateButton(tempButton, db);
				}
                db.close();

				// If the tts text is set and caching is enabled, create a cached sound file
				if (tempButton.getTtsText() != null && preferences.getBoolean(Constants.TTS_SAVE_PREF, false))
				{
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

                        chooseSoundButton();
                    }
				}
				else if (requestCode == FilePickerActivity.REQUEST_CODE)
				{
					if (resultCode == FilePickerActivity.FILE_SELECTED)
					{
						File localFile = saveSoundLocally(uri.getPath());
						tempButton.setSoundPath(localFile.getPath());
						Toast.makeText(this, "Sound file selected...", Toast.LENGTH_SHORT).show();

                        chooseSoundButton();
                    }
				}
                else if (requestCode == KITKAT_GALLERY_REQUEST)
                {
                    if (resultCode == FilePickerActivity.FILE_SELECTED)
                    {
                        File localFile = saveBitmapLocally(new File(uri.getPath()));
                        if (localFile != null) {
                            tempButton.setImagePath(localFile.getAbsolutePath());
                        }
                    }
                }
				else if (requestCode == GALLERY_REQUEST)
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
					Uri audioUri;
					audioUri = data.getData();
					if (audioUri != null)
					{
						File sourceFile = new File(Environment.getExternalStorageDirectory(), audioUri.getPath());
						String destFilePath = Constants.SOUND_DIRECTORY + "/" + sourceFile.getName();
						File destFile = new File(Environment.getExternalStorageDirectory(), destFilePath);
						try
						{
							FileUtils.copy(sourceFile, destFile);
							tempButton.setSoundPath(destFile.getAbsolutePath());

                            chooseSoundButton();
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
		File localFile = new File(Environment.getExternalStorageDirectory(), Constants.IMAGE_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + ".png");
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
		File localFile = new File(Environment.getExternalStorageDirectory(), Constants.IMAGE_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + ".png");

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
		File localFile = new File(Environment.getExternalStorageDirectory(), Constants.SOUND_DIRECTORY + "/" + FileUtils.generateUniqueFilename() + extension);
		
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
		public void onClick(DialogInterface dialog, int which) {
			EditButtonActivity.this.finish();
		}
	}

	private class CropClickListener implements OnClickListener {
		public void onClick(View v) {
			cropFile(new File(Environment.getExternalStorageDirectory(), tempButton.getImagePath()));
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
			Bitmap rotatedBitmap = Bitmap.createBitmap(newEdgeWidth, newEdgeWidth, bitmap.getConfig());
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
			
			Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, left, top, originalWidth, originalHeight);
						
			File outputFile = new File(Environment.getExternalStorageDirectory(), tempButton.getImagePath());
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
		// Cropped images will need to be constrained to avoid crashes when using the built-in cropping activity
		intent.putExtra("outputX", 256);
		intent.putExtra("outputY", 256);
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

    @Override
    public void finish() {
        soundReferee.destroyTts();
        super.finish();
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
            // TODO:  Select the "speak text" radio button
		}
	}
}
