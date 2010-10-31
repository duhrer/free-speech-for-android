package com.blogspot.tonyatkins.myvoice.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;
import com.blogspot.tonyatkins.myvoice.view.FilePickerView;

public class EditButtonActivity extends Activity {
	public static final int ADD_BUTTON = 0;
	public static final int EDIT_BUTTON = 1;
	
	private SoundButton tempButton;
	private AlertDialog alertDialog;
	private boolean isNewButton = false;
	private DbAdapter dbAdapter;
	
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		dbAdapter = new DbAdapter(this);
		
		Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setMessage("You must enter a label and either a sound file, sound resource, or tts text.");
		alertDialogBuilder.setTitle("Required Information Missing or Incorrect");
		alertDialog = alertDialogBuilder.create();
		
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

		
		// A file picker dialog for the sound
		Dialog soundFilePickerDialog = new Dialog(this);
		soundFilePickerDialog.setTitle("Choose Sound File");
		FilePickerView soundFilePickerView = new FilePickerView(this, tempButton, soundFilePickerDialog, FilePickerView.SOUND_FILE);
		soundFilePickerDialog.setContentView(soundFilePickerView);
		soundFilePickerDialog.setOnDismissListener(new FilePickedListener());
		
		// wire up the sound file picker
		TextView soundFileName = (TextView) findViewById(R.id.soundFileName);
		soundFileName.setText(tempButton.getSoundFileName());
		Button soundFileButton = (Button) findViewById(R.id.soundFileButton);
		soundFileButton.setOnClickListener(new LaunchDialogListener(soundFilePickerDialog));
		
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
//		// wire up the image resource picker
//		TextView imageResourceName = (TextView) findViewById(R.id.imageResourceName);
//		// TODO: This should be a name rather than a raw ID
//		imageResourceName.setText(Integer.toString(tempButton.getImageResource()));
//		// TODO: Make an image resource picker and wire it up
//		Button imageResourceButton = (Button) findViewById(R.id.imageResourceButton);
		
		// FIXME: create a color picker and wire it up to this instead of text editing
		// wire up the background color editing
		EditText bgColorEditText = (EditText) findViewById(R.id.bgColorEditText);
		bgColorEditText.setText(tempButton.getBgColor());
		bgColorEditText.addTextChangedListener(new ButtonLabelTextUpdateWatcher(tempButton, SoundButton.BG_COLOR_TEXT_TYPE));
		
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
				
				if (isNewButton) {
					dbAdapter.createButton(tempButton);
				}
				else {
					dbAdapter.updateButton(tempButton);
				}
				
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
			startActivityForResult(intent, RecordSoundActivity.REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RecordSoundActivity.REQUEST_CODE && resultCode == RecordSoundActivity.SOUND_SAVED) {
				Bundle returnedBundle = data.getExtras();
				
				if (returnedBundle != null) {
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
				else {
					Toast.makeText(this, "No sound data to save.", Toast.LENGTH_LONG).show();
				}					
				
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
	
	private class FilePickedListener implements OnDismissListener {
		@Override
		public void onDismiss(DialogInterface dialog) {
			TextView soundFileName = (TextView) findViewById(R.id.soundFileName);
			soundFileName.setText(tempButton.getSoundFileName());
		}
	}
}
