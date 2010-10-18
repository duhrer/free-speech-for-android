package com.blogspot.tonyatkins.myvoice;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.storage.StorageUnavailableFilter;
import com.blogspot.tonyatkins.myvoice.storage.StorageUnavailableReceiver;

public class StartupActivity extends Activity implements OnInitListener {
	private static final int TTS_CHECK_CODE = 777;
	private StorageUnavailableReceiver storageUnavailableReceiver = new StorageUnavailableReceiver();
	private Map<String,String> errorMessages = new HashMap<String,String>();
	private TextToSpeech tts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.startup);

		// FIXME:  Add progress dialog, since startup takes a few seconds
		
		// Is there an sdcard to store things on?
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// See if we have a home directory on the SD card
			File homeDirectory = new File(Constants.HOME_DIRECTORY);
			if (!homeDirectory.exists()) {
				// make our home directory if it doesn't exist
				if (homeDirectory.mkdirs()) {
					Toast mkdirToast = Toast.makeText(this, "Created home directory", Toast.LENGTH_SHORT);
					mkdirToast.show();					
				}
				else {
					errorMessages.put("Can't create home directory", "I wasn't able to create a home directory to store my settings.  Unable to continue.");
				}
			}

			File soundDirectory = new File(Constants.SOUND_DIRECTORY);
			if (!soundDirectory.exists()) {
				if (soundDirectory.mkdir()) {
					Toast soundDirToast = Toast.makeText(this, "Created sound directory", Toast.LENGTH_SHORT);
					soundDirToast.show();
				}
				else {
					errorMessages.put("Can't create sound directory", "I wasn't able to create a directory to store my sounds.  Unable to continue.");
				}
			}
			
			File imageDirectory = new File(Constants.IMAGE_DIRECTORY);
			if (!imageDirectory.exists()) {
				if (imageDirectory.mkdir()) {
					Toast imageDirToast = Toast.makeText(this, "Created image directory", Toast.LENGTH_SHORT);
					imageDirToast.show();
				}
				else {
					errorMessages.put("Can't create sound directory", "I wasn't able to create a directory to store my sounds.  Unable to continue.");
				}
			}

			// Start monitoring for changes in the SD card state and quit with an error if the card is removed or damaged.
			// We depend heavily on storage, so this must be implemented in each of our activities.
			registerReceiver(storageUnavailableReceiver, new StorageUnavailableFilter());

			// Instantiating the database should create everything
			DbAdapter dbAdapter = new DbAdapter(this);
			
			// Sanity check that we have data
			Cursor buttonCursor =  dbAdapter.fetchAllButtons();
			Cursor tabCursor = dbAdapter.fetchAllTabs();
			if (buttonCursor == null || tabCursor == null) {
				errorMessages.put("Error querying database", "I wasn't able to verify the database.  Unable to continue.");
			}
			// It's normal to have no buttons (if the user deletes them all), but we should always have at least one tab
			else if (tabCursor.getCount() == 0) {
				errorMessages.put("No tab data found", "I wasn't able to find any tab data in the database.  Unable to continue.");
			}
			
			// Check to see if we have preferences already
			
			// Create our preferences if not
			
			// Do we have TTS and the language pack?			
			// Offer to let the user download the pack, disable TTS until we have it
	        Intent checkIntent = new Intent();
	        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	        startActivityForResult(checkIntent, TTS_CHECK_CODE);			
	        
	        tts = new TextToSpeech(this,this);
		}
		else {
			errorMessages.put("No SD card found", "This application must be able to write to an SD card.  Please provide one and restart.");
		}
		
		if (errorMessages.size() > 0) {
			Builder alertDialogBuilder = new AlertDialog.Builder(this);
			Iterator<String> keyIterator = errorMessages.keySet().iterator();	
			if (errorMessages.size() == 1) {
				String key = keyIterator.next();
				alertDialogBuilder.setTitle(key);
				alertDialogBuilder.setMessage(errorMessages.get(key));
			}
			else {
				alertDialogBuilder.setTitle("Multiple Errors on Startup");
				
				LinearLayout errorList = new LinearLayout(this);
				alertDialogBuilder.setView(errorList);
				
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					TextView errorTextView = new TextView(this);
					errorTextView.setText(key + ": " + errorMessages.get(key));
					errorList.addView(errorTextView);
				}
			}
			
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.setButton("Exit", new QuitListener());
			alertDialog.setOnCancelListener(new QuitListener());
			alertDialog.show();
		}
		else {
			// Start the main activity
			Intent mainIntent = new Intent(this, ViewBoardActivity.class);
			startActivity(mainIntent);
			finish();
		}
	}

	public class QuitListener implements OnCancelListener, OnClickListener, android.content.DialogInterface.OnClickListener {
		@Override
		public void onCancel(DialogInterface dialog) {
			finish();
		}

		@Override
		public void onClick(View v) {
			finish();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
	}

    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Intent installIntent = new Intent();
                installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
                
                errorMessages.put("Error Initializing TTS", "Could not verify that TTS is available.");
            }
        }
    }
	@Override
	public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
            	errorMessages.put("Error Initializing TTS","Language is not available.");
            }
        } else {
        	errorMessages.put("Error Initializing TTS","Could not initialize TextToSpeech.");
        }
	}

	
	@Override
	public void finish() {
		// Stop listening for storage errors
		unregisterReceiver(storageUnavailableReceiver);
		
		tts.shutdown();
		super.finish();
	}
	
	
}
