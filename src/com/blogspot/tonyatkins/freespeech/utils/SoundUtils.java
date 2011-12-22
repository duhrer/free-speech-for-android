/**
 * Copyright 2011 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Tony Atkins ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Tony Atkins OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 */
package com.blogspot.tonyatkins.freespeech.utils;

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

import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;

public class SoundUtils {
	
	public static void rebuildTtsFiles(Context context, DbAdapter dbAdapter) {
		ArrayList<String> errors = new ArrayList<String>();
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean saveTTS = preferences.getBoolean(Constants.TTS_SAVE_PREF, false);

		// Always remove existing content to avoid stale pre-rendered content.
		File ttsOutputDirectory = new File(Constants.TTS_OUTPUT_DIRECTORY);
		FileUtils.recursivelyDelete(ttsOutputDirectory);

		if (saveTTS) {
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
			
			// Render sounds for all buttons
			for (long buttonId : buttonIds) {
				File buttonTtsOutputDir = new File( Constants.TTS_OUTPUT_DIRECTORY + "/" + buttonId);
				SoundButton button = dbAdapter.fetchButtonById(String.valueOf(buttonId));
				if (button.getTtsText() != null && button.getTtsText().length() > 0) {
					File file = new File(button.getTtsOutputFile());
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
	
	public static void deleteTtsFiles() {
		File ttsOutputDirectory = new File(Constants.TTS_OUTPUT_DIRECTORY);
		FileUtils.recursivelyDelete(ttsOutputDirectory);
	}
}


