package com.blogspot.tonyatkins.myvoice.controller;

import java.util.Locale;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

public class SoundReferee {
	private TextToSpeech textToSpeech;
	private SoundButtonView activeButtonView;
	
	public SoundReferee(Context context) {
		textToSpeech = new TextToSpeech(context,new TtsInitListener());
	}

	public void start() {
		if (activeButtonView != null && activeButtonView.getMediaPlayer() != null && !activeButtonView.getMediaPlayer().isPlaying()) {
			try {
				activeButtonView.getMediaPlayer().start();
			} catch (Exception e) {
				Log.e(getClass().toString(), "Error loading file", e);
			} 
		}
		if (textToSpeech != null && !textToSpeech.isSpeaking()) {
			textToSpeech.speak(activeButtonView.getSoundButton().getTtsText(), TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	public void stop() {
		if (textToSpeech != null && textToSpeech.isSpeaking()) {
			textToSpeech.stop();
		}
		if (activeButtonView != null && activeButtonView.getMediaPlayer() != null && activeButtonView.getMediaPlayer().isPlaying()) {
			// We pause and rewind the media player because stop requires reinitialization
			activeButtonView.getMediaPlayer().pause();
			activeButtonView.getMediaPlayer().seekTo(0);
		}
	}

	
	public boolean isPlaying() {
		if (textToSpeech != null && textToSpeech.isSpeaking()) {
			return true;
		}
		else if (activeButtonView != null && activeButtonView.getMediaPlayer() != null && activeButtonView.getMediaPlayer().isPlaying()) {
			return true;
		}
		
		return false;
	}
	
	public void setActiveSoundButtonView(SoundButtonView activeButtonView) {
		stop();
		
		this.activeButtonView = activeButtonView;
}

	public SoundButtonView getActiveSoundButtonView() {
		return activeButtonView;
	}

	private class TtsInitListener implements OnInitListener  {
		@Override
		public void onInit(int status) {
	        if (status == TextToSpeech.SUCCESS) {
	            int result = textToSpeech.setLanguage(Locale.US);
	            if (result == TextToSpeech.LANG_MISSING_DATA ||
	                    result == TextToSpeech.LANG_NOT_SUPPORTED) 
	            {
	            	destroyTts();
                } 
	        }
	        else {
	        	destroyTts();
	        }
		}

		private void destroyTts() {
			if (textToSpeech != null) {
				textToSpeech.shutdown();
				textToSpeech = null;
			}
		}
	}
}
