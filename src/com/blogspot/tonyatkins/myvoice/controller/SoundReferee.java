package com.blogspot.tonyatkins.myvoice.controller;

import java.io.Serializable;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.blogspot.tonyatkins.myvoice.locale.LocaleBuilder;
import com.blogspot.tonyatkins.myvoice.tts.TtsHelper;
import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

public class SoundReferee implements Serializable {
	private TextToSpeech tts;
	private SoundButtonView activeButton;
	private Context context; 
	private SharedPreferences preferences;
	
	public SoundReferee(Context context) {
		this.context = context;
		TtsHelper ttsHelper = new TtsHelper(context);
		tts = ttsHelper.getTts();
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void start() {
		if (activeButton != null) {
			if (activeButton.getMediaPlayer() != null && !activeButton.getMediaPlayer().isPlaying()) {
				try {
					activeButton.getMediaPlayer().start();
				} catch (Exception e) {
					Log.e(getClass().toString(), "Error loading file", e);
				} 
			}
			else if (activeButton.getTtsText() != null && !tts.isSpeaking()) {
				if (preferences.getBoolean("saveTTS", false) && activeButton.getSoundButton().hasTtsOutput()) {
					// associate the saved output with the TTS text
					tts.addSpeech(activeButton.getTtsText(),activeButton.getSoundButton().getTtsOutputFile());
				}
				
				tts.speak(activeButton.getTtsText(), TextToSpeech.QUEUE_FLUSH, null);
			}
			else {
				Log.e(getClass().toString(), "No sound or speech data for button ( id " + activeButton.getId() + ")");
			}
		}
	}
	
	public void stop() {
		if (tts != null && tts.isSpeaking()) {
			tts.stop();
		}
		if (activeButton != null && activeButton.getMediaPlayer() != null && activeButton.getMediaPlayer().isPlaying()) {
			// We pause and rewind the media player because stop requires reinitialization
			activeButton.getMediaPlayer().pause();
			activeButton.getMediaPlayer().seekTo(0);
		}
	}

	
	public boolean isPlaying() {
		if (tts != null && tts.isSpeaking()) {
			return true;
		}
		else if (activeButton != null && activeButton.getMediaPlayer() != null && activeButton.getMediaPlayer().isPlaying()) {
			return true;
		}
		
		return false;
	}
	
	public void setActiveSoundButton(SoundButtonView activeButton) {
		stop();
		this.activeButton = activeButton;
		start();
}

	public SoundButtonView getActiveSoundButton() {
		return activeButton;
	}


	@Override
	protected void finalize() throws Throwable {
		tts.shutdown();
		super.finalize();
	}
	
	public void setLocale() {
		Locale locale = LocaleBuilder.localeFromString(preferences.getString("tts_voice", "eng-USA"));
		
		int result = tts.setLanguage(locale);
		if (result == TextToSpeech.LANG_MISSING_DATA ||
				result == TextToSpeech.LANG_NOT_SUPPORTED) {
			destroyTts();
		}
	}
	public void destroyTts() {
		if (tts!= null) {
			tts.shutdown();
		}
	}

	public TextToSpeech getTts() {
		return tts;
	}

	public Context getContext() {
		return context;
	}
}
