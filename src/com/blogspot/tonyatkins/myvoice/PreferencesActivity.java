package com.blogspot.tonyatkins.myvoice;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.speech.tts.TextToSpeech;

// FIXME:  Convert to preferences activity

public class PreferencesActivity extends PreferenceActivity {
	private static final int TTS_CHECK_CODE = 777;
	public static final int EDIT_PREFERENCES = 999;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_CHECK_CODE);	
	}

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_CODE) {
        	// We're going to be really lazy about checking to see that TTS is installed properly, as our startup method won't let anyone near here unless that's true

        	// For whatever reason, the list of available voices isn't nicely exposed as a constant, so we hard code it.
        	ListPreference voiceListPreference = (ListPreference) findPreference("columns");
        	voiceListPreference.setEntryValues(data.getStringArrayExtra("availableVoices"));
        }
    }
}
