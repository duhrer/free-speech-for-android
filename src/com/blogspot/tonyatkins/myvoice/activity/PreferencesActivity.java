package com.blogspot.tonyatkins.myvoice.activity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.locale.LocaleBuilder;
import com.blogspot.tonyatkins.myvoice.utils.SoundUtils;

public class PreferencesActivity extends PreferenceActivity {
	private static final int TTS_CHECK_CODE = 777;
	public static final int EDIT_PREFERENCES = 999;
	public static final int RESULT_PREFS_CHANGED = 134;
	private DbAdapter dbAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		// Instantiating the database should create everything
		dbAdapter = new DbAdapter(this, new SoundReferee(this));

		// register for preference changes
		preferences
				.registerOnSharedPreferenceChangeListener(new PreferenceChangeListener(
						dbAdapter, this));

		boolean fullScreen = preferences.getBoolean(Constants.FULL_SCREEN_PREF,
				false);
		if (fullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_CHECK_CODE);
		
		setResult(RESULT_OK);
	}

	@Override
	public void finish() {
		if (dbAdapter != null) dbAdapter.close();
		super.finish();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_CHECK_CODE) {
			// We're going to be really lazy about checking to see that TTS is
			// installed properly, as our startup method won't let anyone near
			// here unless that's true

			// For whatever reason, the list of available voices isn't nicely
			// exposed as a constant, so we hard code it.
			ListPreference voiceListPreference = (ListPreference) findPreference(Constants.TTS_VOICE_PREF);
			ArrayList<String> voiceArrayList = data
					.getStringArrayListExtra("availableVoices");
			if (voiceArrayList == null) {
				Log.e(getClass().getCanonicalName(),
						"Can't retrieve list of available voices.");
			} else {
				String[] voiceStringEntryValues = (String[]) Array.newInstance(
						String.class, voiceArrayList.size());
				String[] voiceStringEntries = (String[]) Array.newInstance(
						String.class, voiceArrayList.size());

				int i = 0;
				for (String voice : voiceArrayList) {
					voiceStringEntryValues[i] = voice;
					Locale locale = LocaleBuilder.localeFromString(voice);
					voiceStringEntries[i] = locale.getDisplayName();

					i++;
				}

				voiceListPreference.setEntryValues(voiceStringEntryValues);
				voiceListPreference.setEntries(voiceStringEntries);
			}
		}
	}

	private class PreferenceChangeListener implements OnSharedPreferenceChangeListener {
		private DbAdapter dbAdapter;
		private Context context;

		public PreferenceChangeListener(DbAdapter dbAdapter, Context context) {
			this.dbAdapter = dbAdapter;
			this.context = context;
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			setResult(RESULT_PREFS_CHANGED);
			
			Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
			if (Constants.COLUMNS_PREF.equals(key)) {
				// This one will be taken care of when we reopen the activity,
				// so we're just displaying a confirmation
				String columnString = sharedPreferences.getString(Constants.COLUMNS_PREF,Constants.DEFAULT_COLUMNS);
				int columns = Integer.valueOf(columnString);
				
				toast.setText("Switched to " + columns + "-column layout.");
			} else if (Constants.FULL_SCREEN_PREF.equals(key)) {
				// This one will be taken care of when we reopen the activity,
				// so we're just displaying a confirmation
				String message = "";
				if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_PREF,
						false))
					message = "Full screen enabled.";
				else
					message = "Full screen disabled";
				toast.setText(message);
			} else if (Constants.TTS_SAVE_PREF.equals(key)) {
				String message = "";
				if (sharedPreferences
						.getBoolean(Constants.TTS_SAVE_PREF, false))
					message = "TTS caching enabled.";
				else
					message = "TTS caching disabled.";
				toast.setText(message);
				SoundUtils.checkTtsFiles(context, dbAdapter, false);
			} else if (Constants.TTS_VOICE_PREF.equals(key)) {
				toast.setText("Voice changed to '"
						+ sharedPreferences.getString(Constants.TTS_VOICE_PREF,
								"eng-USA") + "'");
				SoundUtils.checkTtsFiles(context, dbAdapter, false);
			} else if (Constants.SCALE_TEXT_PREF.equals(key)) {
				String message = "";
				if (sharedPreferences
						.getBoolean(Constants.SCALE_TEXT_PREF, false))
					message = "Text scaling enabled.";
				else
					message = "Text scaling disabled.";
				toast.setText(message);
			}

			toast.show();
		}

	}
}
