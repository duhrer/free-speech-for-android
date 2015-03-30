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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.handler.ExceptionHandler;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespeech.model.Tab;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class StartupActivity extends FreeSpeechActivity {
    private static final int TTS_CHECK_CODE = 777;
	private static final int VIEW_BOARD_CODE = 241;
	private Map<String, String> errorMessages = new HashMap<String, String>();
	private TextToSpeech tts;
	private ProgressDialog dialog;
	private Intent mainIntent;

	private boolean isBoardRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.startup);

		// Wire up the exception handling
		UncaughtExceptionHandler handler = new ExceptionHandler(this, ExceptionCatcherActivity.class);
		Thread.setDefaultUncaughtExceptionHandler(handler);

		// Do we have TTS and the language pack?
		// Offer to let the user download the pack, disable TTS until we have it
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_CHECK_CODE);
	}

	@Override
	protected void onStart() {
		super.onStart();

		dialog = new ProgressDialog(this);
		dialog.setMessage("Starting up, please stand by...");
		dialog.setCancelable(false);
		dialog.show();

		// Start monitoring for changes in the SD card state and quit with an
		// error if the card is removed or damaged.
		// We depend heavily on storage, so this must be implemented in each of
		// our activities.
		// FIXME: This currently doesn't work and causes problems on application
		// finish.
		// registerReceiver(storageUnavailableReceiver, new
		// StorageUnavailableFilter());

		// Is there an sdcard to store things on?
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			// See if we have a home directory on the SD card
			File homeDirectory = new File(Constants.HOME_DIRECTORY);
			if (!homeDirectory.exists())
			{
				// make our home directory if it doesn't exist
				if (homeDirectory.mkdirs())
				{
					dialog.setMessage("Created home directory");
				}
				else
				{
					errorMessages.put("Can't create home directory", "I wasn't able to create a home directory to store my settings.  Unable to continue.");
				}
			}

			File soundDirectory = new File(Constants.SOUND_DIRECTORY);
			if (!soundDirectory.exists())
			{
				if (soundDirectory.mkdir())
				{
					dialog.setMessage("Created sound directory");
				}
				else
				{
					errorMessages.put("Can't create sound directory", "I wasn't able to create a directory to store my sounds.  Unable to continue.");
				}
			}

			File imageDirectory = new File(Constants.IMAGE_DIRECTORY);
			if (!imageDirectory.exists())
			{
				if (imageDirectory.mkdir())
				{
					dialog.setMessage("Created image directory");
				}
				else
				{
					errorMessages.put("Can't create sound directory", "I wasn't able to create a directory to store my sounds.  Unable to continue.");
				}
			}

            DbOpenHelper helper = new DbOpenHelper(this){
                public void onOpen(SQLiteDatabase db) {
                    // Sanity check that we have at least one tab.
                    Collection<Tab> tabs = TabDbAdapter.fetchAllTabs(db);

                    if (tabs == null)
                    {
                        errorMessages.put("Error querying database", "I wasn't able to verify the database.  Unable to continue.");
                    }
                    else if (tabs.size() == 0)
                    {
                        Log.e(Constants.TAG, "I wasn't able to find any tabs in the database.  Creating a tab to allow us to continue.");
                        TabDbAdapter.createTab(new Tab(Tab.NO_ID,"Home"), db);
                    }

                    db.close();
                }
            };
		}
		else
		{
			errorMessages.put("No SD card found", "This application must be able to write to an SD card.  Please provide one and restart.");
		}

		TtsInitListener ttsInitListener = new TtsInitListener();
		tts = new TextToSpeech(this, ttsInitListener);
	}

	private void launchOrDie() {
		if (errorMessages.size() > 0)
		{
			Builder alertDialogBuilder = new AlertDialog.Builder(this);
			Iterator<String> keyIterator = errorMessages.keySet().iterator();
			if (errorMessages.size() == 1)
			{
				String key = keyIterator.next();
				alertDialogBuilder.setTitle(key);
				alertDialogBuilder.setMessage(errorMessages.get(key));
			}
			else
			{
				alertDialogBuilder.setTitle("Multiple Errors on Startup");

				LinearLayout errorList = new LinearLayout(this);
				alertDialogBuilder.setView(errorList);

				while (keyIterator.hasNext())
				{
					String key = keyIterator.next();
					TextView errorTextView = new TextView(this);
					errorTextView.setText(key + ": " + errorMessages.get(key));
					errorList.addView(errorTextView);
				}
			}

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Exit", new ActivityQuitListener(this));
			alertDialog.setOnCancelListener(new ActivityQuitListener(this));
			alertDialog.show();
		}
		else
		{
			if (!isBoardRunning)
			{
				isBoardRunning = true;

				// Start the main activity
				if (mainIntent == null)
				{
					mainIntent = new Intent(this, ViewBoardActivity.class);
				}

				dialog.dismiss();
				startActivityIfNeeded(mainIntent, VIEW_BOARD_CODE);
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_CHECK_CODE)
		{
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA)
			{
				Log.w(Constants.TAG, "The TTS engine indicates that you are missing data.  This may simply indicate that you have not installed unneeded languages, or it may indicate a problem.");
			}
			else if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
			{
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);

				errorMessages.put("Error Initializing TTS", "Could not verify that TTS is available.");
			}
		}
		else if (requestCode == VIEW_BOARD_CODE)
		{
			if (resultCode == ViewBoardActivity.RESULT_RESTART_REQUIRED)
			{
				// quick hack to restart the main activity if the preferences
				// have changed
				if (mainIntent != null)
					startActivityIfNeeded(mainIntent, VIEW_BOARD_CODE);
				else
					finish();
			}
			else
			{
				// this should avoid the double restarts I've seen previously.
				finish();
			}
		}
	}

	private class TtsInitListener implements OnInitListener {
		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS)
			{
				int result = tts.setLanguage(Locale.US);
				if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				{
					errorMessages.put("Error Initializing TTS", "Language is not available.");
				}
			}
			else
			{
				errorMessages.put("Error Initializing TTS", "Could not initialize TextToSpeech engine.");
			}

			destroyTts();
			launchOrDie();
		}

	}

	private void destroyTts() {
		if (tts != null)
			tts.shutdown();
	}

	@Override
	protected void onDestroy() {
		destroyTts();
		super.onDestroy();
		if (dialog != null)
		{
			dialog.dismiss();
		}
	}
}
