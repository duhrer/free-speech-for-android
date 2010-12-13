package com.blogspot.tonyatkins.myvoice.activity;

import java.io.File;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.FileIconListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;
import com.blogspot.tonyatkins.myvoice.view.ColorSwatch;

public class EditButtonActivity extends Activity {
	public static final int ADD_BUTTON = 0;
	public static final int EDIT_BUTTON = 1;
	
	private SoundButton tempButton;
	private boolean isNewButton = false;
	private DbAdapter dbAdapter;
	
	private ColorSwatch colorSwatch;
	
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		dbAdapter = new DbAdapter(this);
		
		Bundle bundle = this.getIntent().getExtras();
		String tabId = null;
		String buttonId = null;
		
		if (bundle != null) {
			// FIXME: Pull the button data from the database instead of the bundle
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

		// wire up the label editing
		EditText labelEditText = (EditText) findViewById(R.id.labelEditText);
		labelEditText.setText(tempButton.getLabel());
		labelEditText.addTextChangedListener(new ButtonLabelTextUpdateWatcher(tempButton, SoundButton.LABEL_TEXT_TYPE));
		
		// wire up the tts text editing
		EditText ttsEditText = (EditText) findViewById(R.id.ttsEditText);
		ttsEditText.setText(tempButton.getTtsText());
		ttsEditText.addTextChangedListener(new ButtonLabelTextUpdateWatcher(tempButton, SoundButton.TTS_TEXT_TYPE));

		
		// wire up the sound file picker
		TextView soundFileName = (TextView) findViewById(R.id.soundFileName);
		soundFileName.setText(tempButton.getSoundFileName());
		Button soundFileButton = (Button) findViewById(R.id.soundFileButton);
		Bundle pickSoundBundle = new Bundle();
		pickSoundBundle.putInt(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.SOUND_FILE_TYPE);
		pickSoundBundle.putString(FilePickerActivity.CWD_BUNDLE, tempButton.getSoundPath());
		soundFileButton.setOnClickListener(new LaunchIntentListener(this, FilePickerActivity.class, pickSoundBundle));
		
		// Wire up the sound recording screen
		Button recordSoundButton = (Button) findViewById(R.id.recordSoundButton);
		Bundle recordSoundBundle = new Bundle();
		recordSoundBundle.putString(RecordSoundActivity.FILE_NAME_KEY, tempButton.getLabel() );
		recordSoundButton.setOnClickListener(new LaunchIntentListener(this, RecordSoundActivity.class, recordSoundBundle));
		
		// FIXME: Add a picker for built-in sound resources
		// wire up the sound resource picker
//		TextView soundResourceName = (TextView) findViewById(R.id.soundResourceName);
		// TODO: This should be a name rather than a raw ID
//		soundResourceName.setText(Integer.toString(tempButton.getSoundResource()));
		// TODO: Make a sound resource picker and wire it up to this button
//		Button soundResourceButton = (Button) findViewById(R.id.soundResourceButton);
		
		// wire up the image file picker
		TextView imageFileName = (TextView) findViewById(R.id.imageFileName);
		// TODO: Display just the filename
		imageFileName.setText(tempButton.getImageFileName());
		Button imageButton = (Button) findViewById(R.id.imageButton);
		Bundle pickImageBundle = new Bundle();
		pickImageBundle.putInt(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.IMAGE_FILE_TYPE);
		pickImageBundle.putString(FilePickerActivity.CWD_BUNDLE, tempButton.getSoundPath());
		imageButton.setOnClickListener(new LaunchIntentListener(this, FilePickerActivity.class, pickImageBundle));
		
		// FIXME: Add a picker for built-in image resources
//		// wire up the image resource picker
//		TextView imageResourceName = (TextView) findViewById(R.id.imageResourceName);
//		// TODO: This should be a name rather than a raw ID
//		imageResourceName.setText(Integer.toString(tempButton.getImageResource()));
//		// TODO: Make an image resource picker and wire it up
//		Button imageResourceButton = (Button) findViewById(R.id.imageResourceButton);
		
		// FIXME: create a color picker and wire it up to this instead of text editing
		// wire up the background color editing
		colorSwatch = (ColorSwatch) findViewById(R.id.bgColorColorSwatch);
		colorSwatch.setBackgroundColor(Color.TRANSPARENT);
		try {
			if (tempButton.getBgColor() != null) {
 				colorSwatch.setBackgroundColor(Color.parseColor(tempButton.getBgColor()));
			}
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, "The current color is invalid and will not be displayed.", Toast.LENGTH_LONG);
		}
		
		// launch a color picker activity when this view is clicked
		Bundle pickColorBundle = new Bundle();
		pickColorBundle.putString(ColorPickerActivity.COLOR_BUNDLE, tempButton.getBgColor());
		colorSwatch.setOnClickListener(new LaunchIntentListener(this, ColorPickerActivity.class, pickColorBundle));
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.buttonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());
		
		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.buttonPanelSaveButton);
		saveButton.setOnClickListener(new SaveListener(this));
	}

	private class CancelListener implements OnClickListener {
		@Override
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

		@Override
		public void onClick(View arg0) {
			// Sanity check the data and open a dialog if there are problems
			if (tempButton.getLabel() == null || 
					tempButton.getLabel().length() <= 0 || 
					((tempButton.getSoundPath() == null || tempButton.getSoundPath().length() <= 0) && 
					 tempButton.getSoundResource() == SoundButton.NO_RESOURCE && 
					 (tempButton.getTtsText() == null || tempButton.getTtsText().length() <= 0 ))) {

				Toast.makeText(context, "You must enter a label and either a sound file, sound resource, or tts text.", Toast.LENGTH_LONG);
			}
			else {
				try {
					
					if (tempButton.getBgColor() != null) {
						// test the color to make sure it's valid
						Color.parseColor(tempButton.getBgColor());
					}
					
					Intent returnedIntent = new Intent();
					
					if (isNewButton) {
						dbAdapter.createButton(tempButton);
					}
					else {
						dbAdapter.updateButton(tempButton);
					}
					
					setResult(RESULT_OK,returnedIntent);
					finish();
				}
				catch (IllegalArgumentException e) 
				{
					// catch an exception if we've been passed an invalid color
					Toast.makeText(context, "You chose an invalid color, can't continue.", Toast.LENGTH_LONG);
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

		@Override
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
				if (requestCode == RecordSoundActivity.REQUEST_CODE) {
					if (resultCode == RecordSoundActivity.SOUND_SAVED) {
						String soundFilePath = returnedBundle.getString(RecordSoundActivity.RECORDING_BUNDLE);
						File returnedSoundFile = new File(soundFilePath);
						if (returnedSoundFile.exists()) {
							tempButton.setSoundPath(soundFilePath);
							
							TextView soundFileName = (TextView) findViewById(R.id.soundFileName);
							soundFileName.setText(tempButton.getSoundFileName());
							
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
						if (fileType != 0 && path != null) {
							if (fileType == FileIconListAdapter.SOUND_FILE_TYPE) {
								tempButton.setSoundPath(path);
								TextView soundFileName = (TextView) findViewById(R.id.soundFileName);
								soundFileName.setText(tempButton.getSoundFileName());
								
								Toast.makeText(this, "Sound file selected...", Toast.LENGTH_SHORT).show();
							}
							else if (fileType == FileIconListAdapter.IMAGE_FILE_TYPE) {
								tempButton.setImagePath(path);
								TextView imageFileName = (TextView) findViewById(R.id.imageFileName);
								imageFileName.setText(tempButton.getSoundFileName());
								Toast.makeText(this, "Image file selected...", Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
				else if (requestCode == ColorPickerActivity.REQUEST_CODE) {
					if (resultCode == ColorPickerActivity.COLOR_SELECTED) {
						String selectedColorString = returnedBundle.getString(ColorPickerActivity.COLOR_BUNDLE);
						try {
							// This will throw an exception if the color isn't valid
							int selectedColor = Color.parseColor(selectedColorString);
							colorSwatch.setBackgroundColor(selectedColor);
							tempButton.setBgColor(selectedColorString);
						} catch (IllegalArgumentException e) {
							Toast.makeText(this, "Invalid color returned from color picker, ignoring.", Toast.LENGTH_LONG);
						}
					}
				}
			}
			else {
				// If no data is returned from the color picker, but the result is OK, it means the color is set to transparent (null)
				if (requestCode == ColorPickerActivity.REQUEST_CODE && resultCode == ColorPickerActivity.COLOR_SELECTED) {
					colorSwatch.setBackgroundColor(Color.TRANSPARENT);
					colorSwatch.invalidate();
					tempButton.setBgColor(null);
				}
			}
		}
		else {
			// data should never be null unless we've cancelled, but oh well
		}
	}
	
	private class QuitActivityListener implements android.content.DialogInterface.OnClickListener {
		private final Activity activity;
		
		public QuitActivityListener(Activity activity) {
			super();
			this.activity = activity;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			activity.finish();
		}
	}
}
