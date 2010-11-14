package com.blogspot.tonyatkins.myvoice.activity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.speech.tts.TextToSpeech;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.locale.LocaleBuilder;

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
        	ListPreference voiceListPreference = (ListPreference) findPreference("tts_voice");
        	ArrayList<String> voiceArrayList=  data.getStringArrayListExtra("availableVoices");
        	String [] voiceStringEntryValues = (String[]) Array.newInstance(String.class, voiceArrayList.size());
        	String [] voiceStringEntries = (String[]) Array.newInstance(String.class, voiceArrayList.size());

        	// Apparently we have to hand hold the ickle baby through the conversion from Object[] to String[]
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
