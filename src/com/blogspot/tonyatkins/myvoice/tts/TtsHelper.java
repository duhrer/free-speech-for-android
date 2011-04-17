package com.blogspot.tonyatkins.myvoice.tts;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.locale.LocaleBuilder;

public class TtsHelper {
	private final TextToSpeech tts;
	private final Context  context;
	
	public TtsHelper(Context context) {
		this.context = context;
		this.tts = new TextToSpeech(context,new SimpleTtsInitListener());
	}

	public static void destroyTts(TextToSpeech tts) {
		if (tts!= null) {
			tts.shutdown();
		}
	}
	
	private void setLocale(Context context, TextToSpeech tts) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		Locale locale = LocaleBuilder.localeFromString(preferences.getString(Constants.TTS_VOICE_PREF, "eng-USA"));
		
		int result = tts.setLanguage(locale);
		if (result == TextToSpeech.LANG_MISSING_DATA ||
				result == TextToSpeech.LANG_NOT_SUPPORTED) {
			destroyTts(tts);
		}
	}
	
	private class SimpleTtsInitListener implements OnInitListener {
		@Override
		public void onInit(int status) {
	        if (status == TextToSpeech.SUCCESS) {
	            setLocale();
	        } else {
	        	destroyTts();
	        }
		}

		private void setLocale() {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			
			Locale locale = LocaleBuilder.localeFromString(preferences.getString(Constants.TTS_VOICE_PREF, "eng-USA"));
			
			int result = tts.setLanguage(locale);
			if (result == TextToSpeech.LANG_MISSING_DATA ||
					result == TextToSpeech.LANG_NOT_SUPPORTED) {
				destroyTts();
			}
		}
		private void destroyTts() {
			if (tts!= null) {
				tts.shutdown();
			}
		}
	}

	public TextToSpeech getTts() {
		return tts;
	}
}
