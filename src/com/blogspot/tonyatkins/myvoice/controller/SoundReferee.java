package com.blogspot.tonyatkins.myvoice.controller;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.blogspot.tonyatkins.myvoice.model.SoundButton;

public class SoundReferee {
	private TextToSpeech textToSpeech;
	private SoundButton activeButton;
	private Context context; 
	
	public SoundReferee(Context context) {
		this.context = context;
		textToSpeech = new TextToSpeech(context,new TtsInitListener());
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
			else if (activeButton.getTtsText() != null && !textToSpeech.isSpeaking()) {
				textToSpeech.speak(activeButton.getTtsText(), TextToSpeech.QUEUE_FLUSH, null);
			}
			else {
				Log.e(getClass().toString(), "No sound or speech data for button " + activeButton.getLabel() + "( id " + activeButton.getId() + ")");
			}
		}
	}
	
	public void stop() {
		if (textToSpeech != null && textToSpeech.isSpeaking()) {
			textToSpeech.stop();
		}
		if (activeButton != null && activeButton.getMediaPlayer() != null && activeButton.getMediaPlayer().isPlaying()) {
			// We pause and rewind the media player because stop requires reinitialization
			activeButton.getMediaPlayer().pause();
			activeButton.getMediaPlayer().seekTo(0);
		}
	}

	
	public boolean isPlaying() {
		if (textToSpeech != null && textToSpeech.isSpeaking()) {
			return true;
		}
		else if (activeButton != null && activeButton.getMediaPlayer() != null && activeButton.getMediaPlayer().isPlaying()) {
			return true;
		}
		
		return false;
	}
	
	public void setActiveSoundButton(SoundButton activeButton) {
		stop();
		this.activeButton = activeButton;
		start();
}

	public SoundButton getActiveSoundButton() {
		return activeButton;
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

	@Override
	protected void finalize() throws Throwable {
		textToSpeech.shutdown();
		super.finalize();
	}
}
