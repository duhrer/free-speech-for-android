/**
 * Copyright (C) 2011 Tony Atkins <duhrer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blogspot.tonyatkins.myvoice.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;

public class SoundUtils {
	
	public static void checkTtsFiles(Context context, DbAdapter dbAdapter) {
		checkTtsFiles(context, dbAdapter, true);
	}
	
	public static void checkTtsFiles(Context context, DbAdapter dbAdapter, boolean preserveExistingFiles) {
		ArrayList<String> errors = new ArrayList<String>();
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean saveTTS = preferences.getBoolean(Constants.TTS_SAVE_PREF, false);
		// If we're not saving TTS utterances as sound files, remove any content in that directory
		if (!saveTTS) {
			File ttsOutputDirectory = new File(Constants.TTS_OUTPUT_DIRECTORY);
			FileUtils.recursivelyDelete(ttsOutputDirectory);
		}
		else {
			Cursor buttonCursor =  dbAdapter.fetchAllButtons();

			List<Long> buttonIds = new ArrayList<Long>();
			
			// build the list of used IDs
			if (buttonCursor != null) {
				if (buttonCursor.getCount() > 0) {
					buttonCursor.moveToFirst();
					for (int a = 0; a < buttonCursor.getCount(); a++) {
						buttonIds.add(buttonCursor.getLong(buttonCursor.getColumnIndex(SoundButton._ID)));
						buttonCursor.moveToNext();
					}
				}
				buttonCursor.close();
			}
			
			// Clean up unused sound files rendered from TTS utterances
			File ttsOutputDirectory = new File(Constants.TTS_OUTPUT_DIRECTORY);
			if (ttsOutputDirectory.exists() && ttsOutputDirectory.isDirectory()) {
				for (File file : ttsOutputDirectory.listFiles()) {
					// All sounds should be saved to TTS_OUTPUT_DIR/BUTTON_ID/FILE
					if (!buttonIds.contains(file.getName())) {
						file.delete();
					}
				}
			}
			
			// Render sounds for any buttons that don't already have them
			for (long buttonId : buttonIds) {
				File buttonTtsOutputDir = new File( Constants.TTS_OUTPUT_DIRECTORY + "/" + buttonId);
				if (!buttonTtsOutputDir.exists()) {
					SoundButton button = dbAdapter.fetchButtonById(String.valueOf(buttonId));
					if (button.getTtsText() != null && button.getTtsText().length() > 0) {
						File file = new File(button.getTtsOutputFile());
						// We're going to avoid removing existing files for now
						if (!file.exists() || !preserveExistingFiles) {
							boolean buttonSaved = button.saveTtsToFile();
							if (buttonSaved) {
								Log.d("SoundUtils", "TTS output saved for button '" + button.getLabel() + "'.");
							}
							else {
								String message = "Unable to save TTS output for button '" + button.getLabel() + "'.";
								errors.add(message);
								Log.d("SoundUtils", message);
							}
						}
					}
				}
			}
		}
		
		if (errors.size() > 0) {
			StringBuffer errorContent = new StringBuffer();
			for (String error: errors) {
				errorContent.append(error + "\n");
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Error(s) Rendering TTS to file:");
			builder.setMessage(errorContent.toString());
			builder.setCancelable(true);
			builder.setIcon(R.drawable.ic_dialog_alert);
			builder.create().show();
		}
		else {
			Toast.makeText(context, "Saved TTS output for all buttons.", Toast.LENGTH_LONG).show();
		}
	}
}
