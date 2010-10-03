package com.blogspot.tonyatkins.myvoice;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.storage.StorageUnavailableFilter;
import com.blogspot.tonyatkins.myvoice.storage.StorageUnavailableReceiver;

public class StartupActivity extends Activity {
	private StorageUnavailableReceiver storageUnavailableReceiver = new StorageUnavailableReceiver();
	private Map<String,String> errorMessages = new HashMap<String,String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.startup);

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
			
			// Set the defaults, including whether or not we have TTS (so that we don't have to check every time)

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

	@Override
	public void finish() {
		// Stop listening for storage errors
		unregisterReceiver(storageUnavailableReceiver);
		super.finish();
	}
	
	
}
