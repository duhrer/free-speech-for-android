package org.blogspot.tonyatkins.pictureboard;

import java.io.File;

import org.blogspot.tonyatkins.pictureboard.model.SoundButton;
import org.blogspot.tonyatkins.pictureboard.view.FilePickerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditButtonActivity extends Activity {
	public static final int ADD_BUTTON = 0;
	public static final int EDIT_BUTTON = 1;
	
	private SoundButton tempButton;
	private AlertDialog alertDialog;
	
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setMessage("You must enter a label and either a sound file, sound resource, or tts text.");
		alertDialogBuilder.setTitle("Required Information Missing or Incorrect");
		alertDialog = alertDialogBuilder.create();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String existingButtonString = bundle.getString(SoundButton.BUTTON_BUNDLE);
			
			// create a temporary button that we will only return on a successful save.
			if (existingButtonString != null && existingButtonString.length() > 0) {
				tempButton = new SoundButton(existingButtonString);
			}
		}
		
		if (tempButton == null) {
			tempButton = new SoundButton(0, null, null, SoundButton.NO_RESOURCE, SoundButton.NO_RESOURCE, -1);
		}
				
		setContentView(R.layout.edit_button);

		// wire up the label editing
		EditText labelEditText = (EditText) findViewById(R.id.labelEditText);
		labelEditText.setText(tempButton.getLabel());
		labelEditText.addTextChangedListener(new TextUpdateWatcher(tempButton, SoundButton.LABEL_TEXT_TYPE));
		
		// wire up the tts text editing
		EditText ttsEditText = (EditText) findViewById(R.id.ttsEditText);
		ttsEditText.setText(tempButton.getTtsText());
		ttsEditText.addTextChangedListener(new TextUpdateWatcher(tempButton, SoundButton.TTS_TEXT_TYPE));

		
		// A file picker dialog for the sound
		Dialog soundFilePickerDialog = new Dialog(this);
		soundFilePickerDialog.setTitle("Choose Sound File");
		FilePickerView soundFilePickerView = new FilePickerView(this, tempButton, soundFilePickerDialog, FilePickerView.SOUND_FILE);
		soundFilePickerDialog.setContentView(soundFilePickerView);
		
		// wire up the sound file picker
		TextView soundFileName = (TextView) findViewById(R.id.soundFileName);
		// TODO: Display just the filename
		soundFileName.setText(tempButton.getSoundPath());
		Button soundFileButton = (Button) findViewById(R.id.soundFileButton);
		soundFileButton.setOnClickListener(new LaunchDialogListener(soundFilePickerDialog));
		
		// Wire up the sound recording screen
		Button recordSoundButton = (Button) findViewById(R.id.recordSoundButton);
		recordSoundButton.setOnClickListener(new LaunchIntentListener(this, RecordSoundActivity.class));
		
		// FIXME: Add a picker for built-in sound resources
		// wire up the sound resource picker
		TextView soundResourceName = (TextView) findViewById(R.id.soundResourceName);
		// TODO: This should be a name rather than a raw ID
		soundResourceName.setText(Integer.toString(tempButton.getSoundResource()));
		// TODO: Make a sound resource picker and wire it up to this button
		Button soundResourceButton = (Button) findViewById(R.id.soundResourceButton);
		
		// A file picker dialog for the image file
		Dialog imageFilePickerDialog = new Dialog(this);
		imageFilePickerDialog.setTitle("Choose Image");
		FilePickerView imageFilePickerView = new FilePickerView(this, tempButton, imageFilePickerDialog, FilePickerView.SOUND_FILE);
		imageFilePickerDialog.setContentView(imageFilePickerView);
		
		// wire up the image file picker
		TextView imageFileName = (TextView) findViewById(R.id.imageFileName);
		// TODO: Display just the filename
		imageFileName.setText(tempButton.getImagePath());
		Button imageButton = (Button) findViewById(R.id.imageButton);
		imageButton.setOnClickListener(new LaunchDialogListener(imageFilePickerDialog));
		
		// FIXME: Add a picker for built-in image resources
		// wire up the image resource picker
		TextView imageResourceName = (TextView) findViewById(R.id.imageResourceName);
		// TODO: This should be a name rather than a raw ID
		imageResourceName.setText(Integer.toString(tempButton.getImageResource()));
		// TODO: Make an image resource picker and wire it up
		Button imageResourceButton = (Button) findViewById(R.id.imageResourceButton);
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.buttonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());
		
		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.buttonPanelSaveButton);
		saveButton.setOnClickListener(new SaveListener());
	}

	private class CancelListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			finish();
		}
	}
	
	private class SaveListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			// Sanity check the data and open a dialog if there are problems
			if (tempButton.getLabel() == null || 
					tempButton.getLabel().length() <= 0 || 
					((tempButton.getSoundPath() == null || tempButton.getSoundPath().length() <= 0) && 
					 tempButton.getSoundResource() == SoundButton.NO_RESOURCE && 
					 (tempButton.getTtsText() == null || tempButton.getTtsText().length() <= 0 ))) {

					alertDialog.show();
			}
			else {
				Intent returnedIntent = new Intent();
				Bundle returnedBundle = new Bundle();
				returnedBundle.putString(SoundButton.BUTTON_BUNDLE, tempButton.getStringBundle());
				returnedIntent.putExtras(returnedBundle);
				
				setResult(RESULT_OK,returnedIntent);
				finish();
			}	
		}
	}
	
	private class LaunchDialogListener implements OnClickListener {
		private Dialog dialog;
		public LaunchDialogListener(Dialog dialog) {
			super();
			this.dialog = dialog;
		}

		@Override
		public void onClick(View v) {
			dialog.show();
		}
	}
	
	private class LaunchIntentListener implements OnClickListener {
		private Context context;
		private Class activityClass;
		
		public LaunchIntentListener(Context context, Class activityClass) {
			this.context = context;
			this.activityClass = activityClass;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, activityClass);
			startActivityForResult(intent, RecordSoundActivity.REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RecordSoundActivity.REQUEST_CODE && resultCode == RecordSoundActivity.SOUND_SAVED) {
				Bundle returnedBundle = data.getExtras();
				String soundFilePath = returnedBundle.getString(RecordSoundActivity.RECORDING_BUNDLE);
				File returnedSoundFile = new File(soundFilePath);
				if (returnedSoundFile.exists() && returnedSoundFile.length() <= 0) {
					tempButton.setSoundPath(soundFilePath);
					Toast.makeText(this, "Sound saved...", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(this, "Error saving file!", Toast.LENGTH_LONG).show();
				}
				
		}
	}
}
