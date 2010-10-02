package org.blogspot.tonyatkins.pictureboard;

import java.io.File;

import org.blogspot.tonyatkins.pictureboard.db.DbAdapter;
import org.blogspot.tonyatkins.pictureboard.storage.StorageUnavailableFilter;
import org.blogspot.tonyatkins.pictureboard.storage.StorageUnavailableReceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class StartupActivity extends Activity {
	private StorageUnavailableReceiver storageUnavailableReceiver = new StorageUnavailableReceiver();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.startup);

		// Is there an sdcard to store things on?
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// See if we have a home directory on the SD card
			File root = Environment.getExternalStorageDirectory();
			File homeDirectory = new File(root.getAbsolutePath() + "/org.blogspot.tonyatkins.pictureboard");
			if (!homeDirectory.exists()) {
				// make our home directory if it doesn't exist
				if (homeDirectory.mkdir()) {
					Toast mkdirToast = Toast.makeText(this, "Created home directory", Toast.LENGTH_SHORT);
					mkdirToast.show();
				}
				else {
					Builder alertDialogBuilder = new AlertDialog.Builder(this);
					alertDialogBuilder.setTitle("Can't create home directory");
					alertDialogBuilder.setMessage("I wasn't able to create a home directory to store my settings.  Unable to continue.");
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.setOnCancelListener(new QuitDialogListener());
					alertDialog.show();
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
				Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle("Error querying database");
				alertDialogBuilder.setMessage("I wasn't able to verify the database.  Unable to continue.");
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.setOnCancelListener(new QuitDialogListener());
				alertDialog.show();
			}
			// It's normal to have no buttons (if the user deletes them all), but we should always have at least one tab
			else if (tabCursor.getCount() == 0) {
				Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle("No tab data found");
				alertDialogBuilder.setMessage("I wasn't able to find any tab data in the database.  Unable to continue.");
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.setOnCancelListener(new QuitDialogListener());
				alertDialog.show();
			}
			
			
			// Check to see if we have preferences already
			
			// Create our preferences if not
			
			// Do we have TTS and the language pack?
			
			// Offer to let the user download the pack, disable TTS until we have it
			
			// Set the defaults, including whether or not we have TTS (so that we don't have to check every time)
			
			// Start the main activity
			Intent mainIntent = new Intent(this, ViewBoardActivity.class);
			startActivity(mainIntent);
			finish();
		}
		else {
			// If we have no SD Card, display a warning and exit once the user acknowledges
			Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("No SD card found");
			alertDialogBuilder.setMessage("This application must be able to write to an SD card.  Please provide one and restart.");
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.setOnCancelListener(new QuitDialogListener());
			alertDialog.show();
		}
	}

	public class QuitDialogListener implements OnCancelListener {
		@Override
		public void onCancel(DialogInterface dialog) {
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
