/**
 * Copyright 2012-2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.activity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;

public class PreferencesActivity extends PreferenceActivity {
	private static final int TTS_CHECK_CODE = 777;
	public static final int EDIT_PREFERENCES = 999;
	public static final int RESULT_PREFS_CHANGED = 134;
	private DbAdapter dbAdapter;
	private PreferenceChangeListener preferenceChangeListener;
	private TextToSpeech tts;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		// Instantiating the database should create everything
		dbAdapter = new DbAdapter(this);

		preferenceChangeListener = new PreferenceChangeListener(dbAdapter, this);
		preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

		boolean fullScreen = preferences.getBoolean(Constants.FULL_SCREEN_PREF, false);
		if (fullScreen)
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_CHECK_CODE);

		setResult(RESULT_OK);
	}

	private void setPreferencesFromIntent() {
		Intent intent = getIntent();
		if (intent == null || intent.getExtras() == null) {
			Log.d(Constants.TAG, "No preferences data included with intent.");
			return;
		}
		
		Editor editor = preferences.edit();
		String[] integerPrefKeys = {Constants.COLUMNS_PREF,Constants.ROWS_PREF};
		String[] booleanPrefKeys = 
			{	Constants.DEV_OPTIONS_PREF,
				Constants.FULL_SCREEN_PREF,
				Constants.HIDE_TAB_CONTROLS_PREF,
				Constants.SCALE_TEXT_PREF,
				Constants.SWIPE_TAB_PREF,
				Constants.ALLOW_EDITING_PREF,
				Constants.TTS_SAVE_PREF
			};
		
		for (String integerPrefKey : integerPrefKeys) {
			if (intent.hasExtra(integerPrefKey)) {
				int prefIntValue = intent.getIntExtra(integerPrefKey, -1);
				if (prefIntValue != -1) {
					Log.d(Constants.TAG, "Set preference '" + integerPrefKey + "' to '" + prefIntValue + "' based on intent.");
					editor.putString(integerPrefKey, String.valueOf(prefIntValue));
				}
			}
		}
		
		for (String booleanPrefKey : booleanPrefKeys) {
			if (intent.hasExtra(booleanPrefKey)) {
				boolean prefBooleanValue = intent.getBooleanExtra(booleanPrefKey, false);
				Log.d(Constants.TAG, "Set preference '" + booleanPrefKey + "' to '" + prefBooleanValue + "' based on intent.");
				editor.putBoolean(booleanPrefKey, prefBooleanValue);
			}
		}
		
		if (intent.hasExtra(Constants.ORIENTATION_PREF)) {
			String orientationString = preferences.getString(Constants.ORIENTATION_PREF, null);
			if (orientationString != null) {
				ListPreference orientationPref = (ListPreference) findPreference(Constants.ORIENTATION_PREF);
				boolean isValidPreference = false;
				for (CharSequence validValue : orientationPref.getEntryValues()) {
					if (orientationString.equals(validValue)) isValidPreference = true;
				}
				
				if (isValidPreference) {
					editor.putString(Constants.ORIENTATION_PREF, orientationString);
				}
			}
		}
		
		if (intent.hasExtra(Constants.TTS_VOICE_PREF)) {
			String ttsVoiceString = preferences.getString(Constants.TTS_VOICE_PREF, null);
			if (ttsVoiceString != null) {
				ListPreference voiceListPreference = (ListPreference) findPreference(Constants.TTS_VOICE_PREF);
				boolean isValidPreference = false;
				for (CharSequence validValue : voiceListPreference.getEntryValues()) {
					if (ttsVoiceString.equals(validValue)) isValidPreference = true;
				}
				
				if (isValidPreference) {
					editor.putString(Constants.TTS_VOICE_PREF, ttsVoiceString);
				}
			}
		}
		
		editor.apply();
		
		Log.d(Constants.TAG, "Updated preferences.  Quitting preferences activity.");
		finish();
	}

	@Override
	public void finish() {
		if (dbAdapter != null) dbAdapter.close();

		if (tts != null) tts.shutdown();
		
		super.finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_CHECK_CODE)
		{
			if (resultCode != TextToSpeech.ERROR)
			{
				tts = new TextToSpeech(this, new TtsPreferenceInitListener());
			}
		}
	}

	private class TtsPreferenceInitListener implements OnInitListener {

		@Override
		public void onInit(int status) {
			ListPreference voiceListPreference = (ListPreference) findPreference(Constants.TTS_VOICE_PREF);

			List<Locale> ttsLocales = new ArrayList<Locale>();
			for (Locale loc : Locale.getAvailableLocales())
			{
				int languageAvailableCode = tts.isLanguageAvailable(loc);
				if (languageAvailableCode == TextToSpeech.LANG_AVAILABLE || languageAvailableCode == TextToSpeech.LANG_COUNTRY_AVAILABLE || languageAvailableCode == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE)
				{
					ttsLocales.add(loc);
				}
				else
				{
					Log.d(Constants.TAG, "Locale '" + loc.getDisplayName() + "' is not available for TTS (check returned '" + languageAvailableCode + "'.  Skipping.");
				}
			}

			String[] voiceStringEntryValues = (String[]) Array.newInstance(String.class, ttsLocales.size());
			String[] voiceStringEntries = (String[]) Array.newInstance(String.class, ttsLocales.size());

			Collections.sort(ttsLocales, new Comparator<Locale>() {
				@Override
				public int compare(Locale lhs, Locale rhs) {
					return lhs.getDisplayName().compareTo(rhs.getDisplayName());
				}});
			
			int i = 0;
			for (Locale ttsLocale : ttsLocales)
			{
				voiceStringEntryValues[i] = ttsLocale.toString();
				voiceStringEntries[i] = ttsLocale.getDisplayName();

				i++;
			}

			voiceListPreference.setEntryValues(voiceStringEntryValues);
			voiceListPreference.setEntries(voiceStringEntries);
			
			setPreferencesFromIntent();
		}

	}

	private class PreferenceChangeListener implements OnSharedPreferenceChangeListener {
		private DbAdapter dbAdapter;
		private final Activity activity;

		public PreferenceChangeListener(DbAdapter dbAdapter, Activity activity) {
			this.dbAdapter = dbAdapter;
			this.activity = activity;
		}

		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			setResult(RESULT_PREFS_CHANGED);

			if (Constants.COLUMNS_PREF.equals(key))
			{
				// This one will be taken care of when we reopen the activity,
				// so we're just displaying a confirmation
				int columns = Integer.valueOf(sharedPreferences.getString(Constants.COLUMNS_PREF, Constants.DEFAULT_COLUMNS));

				Toast toast = Toast.makeText(activity, "Switched to " + columns + "-column layout.", Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.DEV_OPTIONS_PREF.equals(key))
			{
				String message = "";
				if (sharedPreferences.getBoolean(Constants.DEV_OPTIONS_PREF, false))
					message = "Developer options enabled.";
				else
					message = "Developer options disabled";
				Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.FULL_SCREEN_PREF.equals(key))
			{
				// This one will be taken care of when we reopen the activity,
				// so we're just displaying a confirmation
				String message = "";
				if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_PREF, false))
					message = "Full screen enabled.";
				else
					message = "Full screen disabled";
				Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.HIDE_TAB_CONTROLS_PREF.equals(key))
			{
				String message = "";
				if (sharedPreferences.getBoolean(Constants.HIDE_TAB_CONTROLS_PREF, false))
					message = "Tab controls will be hidden.";
				else
					message = "Tab controls will be displayed.";
				Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.ORIENTATION_PREF.equals(key))
			{
				String orientation = sharedPreferences.getString(Constants.ORIENTATION_PREF, Constants.DEFAULT_ORIENTATION);
				Toast toast = Toast.makeText(activity, "Screen orientation set to " + orientation + ".", Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.ROWS_PREF.equals(key))
			{
				// This one will be taken care of when we reopen the activity,
				// so we're just displaying a confirmation
				int rows = Integer.valueOf(sharedPreferences.getString(Constants.COLUMNS_PREF, Constants.DEFAULT_COLUMNS));

				Toast toast = Toast.makeText(activity, "Switched to " + rows + "-row layout.", Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.SCALE_TEXT_PREF.equals(key))
			{
				String message = "";
				if (sharedPreferences.getBoolean(Constants.SCALE_TEXT_PREF, false))
					message = "Text scaling enabled.";
				else
					message = "Text scaling disabled.";
				Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.SWIPE_TAB_PREF.equals(key))
			{
				String message = "";
				if (sharedPreferences.getBoolean(Constants.SWIPE_TAB_PREF, false))
					message = "Swiping to change tabs is enabled.";
				else
					message = "Swiping to change tabs is disabled.";
				Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.TTS_SAVE_PREF.equals(key))
			{
				String message = "";
				if (sharedPreferences.getBoolean(Constants.TTS_SAVE_PREF, false))
				{
					message = "TTS caching enabled.";
					TtsCacheUtils.rebuildTtsFiles(activity);
				}
				else
				{
					message = "TTS caching disabled.";
					TtsCacheUtils.stopService(activity);
					TtsCacheUtils.deleteTtsFiles();
				}

				Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				toast.show();
			}

			if (Constants.TTS_VOICE_PREF.equals(key))
			{
				boolean saveTts = sharedPreferences.getBoolean(Constants.TTS_SAVE_PREF, false);
				if (saveTts)
				{
					TtsCacheUtils.rebuildTtsFiles(activity);
				}
				Toast toast = Toast.makeText(activity, "Voice changed to '" + sharedPreferences.getString(Constants.TTS_VOICE_PREF, "eng-USA") + "'", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}
}
