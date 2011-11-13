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
package com.blogspot.tonyatkins.freespeech.controller;

import java.io.Serializable;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.blogspot.tonyatkins.freespech.locale.LocaleBuilder;
import com.blogspot.tonyatkins.freespech.tts.TtsHelper;
import com.blogspot.tonyatkins.freespech.view.SoundButtonView;
import com.blogspot.tonyatkins.freespeech.Constants;

public class SoundReferee implements Serializable {
	private static final long serialVersionUID = -5115585751204204132L;
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
				if (preferences.getBoolean(Constants.TTS_SAVE_PREF, false) && activeButton.getSoundButton().hasTtsOutput()) {
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
		Locale locale = LocaleBuilder.localeFromString(preferences.getString(Constants.TTS_VOICE_PREF, "eng-USA"));
		
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