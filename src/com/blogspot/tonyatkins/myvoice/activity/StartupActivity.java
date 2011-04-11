package com.blogspot.tonyatkins.myvoice.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.storage.StorageUnavailableFilter;
import com.blogspot.tonyatkins.myvoice.storage.StorageUnavailableReceiver;
import com.blogspot.tonyatkins.myvoice.utils.SoundUtils;

public class StartupActivity extends Activity {
	private static final int TTS_CHECK_CODE = 777;
	private StorageUnavailableReceiver storageUnavailableReceiver = new StorageUnavailableReceiver();
	private Map<String,String> errorMessages = new HashMap<String,String>();
	private TextToSpeech tts;
	private ProgressDialog progressDialog;
	private DbAdapter dbAdapter;
	private Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.startup);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Starting up, please stand by...");
		progressDialog.setCancelable(false);
		progressDialog.show();
		
		// Start monitoring for changes in the SD card state and quit with an error if the card is removed or damaged.
		// We depend heavily on storage, so this must be implemented in each of our activities.
		// FIXME:  This currently doesn't work and causes problems on application finish.
//		registerReceiver(storageUnavailableReceiver, new StorageUnavailableFilter());
		
		// Is there an sdcard to store things on?
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// Do we have TTS and the language pack?			
			// Offer to let the user download the pack, disable TTS until we have it
	        Intent checkIntent = new Intent();
	        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	        startActivityForResult(checkIntent, TTS_CHECK_CODE);			
	        
	        TtsInitListener ttsInitListener = new TtsInitListener();
	        tts = new TextToSpeech(this,ttsInitListener);
			
			// See if we have a home directory on the SD card
			File homeDirectory = new File(Constants.HOME_DIRECTORY);
			if (!homeDirectory.exists()) {
				// make our home directory if it doesn't exist
				if (homeDirectory.mkdirs()) {
					Toast.makeText(this, "Created home directory", Toast.LENGTH_SHORT).show();					
				}
				else {
					errorMessages.put("Can't create home directory", "I wasn't able to create a home directory to store my settings.  Unable to continue.");
				}
			}

			File soundDirectory = new File(Constants.SOUND_DIRECTORY);
			if (!soundDirectory.exists()) {
				if (soundDirectory.mkdir()) {
					Toast.makeText(this, "Created sound directory", Toast.LENGTH_SHORT).show();
				}
				else {
					errorMessages.put("Can't create sound directory", "I wasn't able to create a directory to store my sounds.  Unable to continue.");
				}
			}
			
			File imageDirectory = new File(Constants.IMAGE_DIRECTORY);
			if (!imageDirectory.exists()) {
				if (imageDirectory.mkdir()) {
					Toast.makeText(this, "Created image directory", Toast.LENGTH_SHORT).show();
				}
				else {
					errorMessages.put("Can't create sound directory", "I wasn't able to create a directory to store my sounds.  Unable to continue.");
				}
			}


			// Instantiating the database should create everything
			dbAdapter = new DbAdapter(this, new SoundReferee(this));
			
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
		}
		else {
			errorMessages.put("No SD card found", "This application must be able to write to an SD card.  Please provide one and restart.");
		}

		// If we get to this point, we don't have an SD card and need to throw up an error and die
		launchOrDie();
	}

	private void launchOrDie() {
		progressDialog.dismiss();
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
			alertDialog.setButton("Exit", new ActivityQuitListener(this));
			alertDialog.setOnCancelListener(new ActivityQuitListener(this));
			alertDialog.show();
		}
		else {
			// Start the main activity
			Intent mainIntent = new Intent(this, ViewBoardActivity.class);
			startActivity(mainIntent);
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
	public void finish() {
		// Stop listening for storage errors
		// FIXME Make this work before uncommenting
//		if (storageUnavailableReceiver != null) unregisterReceiver(storageUnavailableReceiver);
		
		if (tts != null)  tts.shutdown();
		super.finish();
	}
private class TtsInitListener implements OnInitListener {
		@Override
		public void onInit(int status) {
	        if (status == TextToSpeech.SUCCESS) {
	            int result = tts.setLanguage(Locale.US);
	            if (result == TextToSpeech.LANG_MISSING_DATA ||
	                result == TextToSpeech.LANG_NOT_SUPPORTED) {
	            	errorMessages.put("Error Initializing TTS","Language is not available.");
	            	destroyTts();
	            }
	            else {
	            	// If TTS has started up successfully, go ahead and organize our TTS storage (if we have any)
	            	SoundUtils.checkTtsFiles(context,dbAdapter);
	            }
	        } else {
	        	errorMessages.put("Error Initializing TTS","Could not initialize TextToSpeech.");
	        	destroyTts();
	        }
	        
	        launchOrDie();
		}

		private void destroyTts() {
			if (tts != null) {
				tts.shutdown();
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (tts != null) { tts.shutdown(); }
		super.onDestroy();
	}
}
