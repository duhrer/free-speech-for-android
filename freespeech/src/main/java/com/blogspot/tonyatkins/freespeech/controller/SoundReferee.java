/**
 * Copyright 2012-2015 Upright Software <info@uprightsoftware.com>. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Upright Software ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Upright Software OR
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
package com.blogspot.tonyatkins.freespeech.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.tts.TtsHelper;
import com.blogspot.tonyatkins.freespeech.utils.I18nUtils;
import com.blogspot.tonyatkins.freespeech.view.SoundButtonView;

import java.io.Serializable;

public class SoundReferee implements Serializable {
	private static final long serialVersionUID = -5115585751204204132L;
	private TextToSpeech tts;
	private SoundButtonView activeButton;
	private SharedPreferences preferences;
	private Context context;
	private final TtsHelper ttsHelper;
	private MediaPlayer mediaPlayer = new MediaPlayer();

	public SoundReferee(Context context) {
		ttsHelper = new TtsHelper(context);
		this.context = context;
		tts = ttsHelper.getTts();
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	private void start() {
		if (activeButton != null)
		{
			if (activeButton.getSoundButton().hasSound())
			{
				Log.d(Constants.TAG, "Playing audio file '" + activeButton.getSoundButton().getLabel() + "'.");
				try
				{
					mediaPlayer.start();
				}
				catch (Exception e)
				{
					Log.e(Constants.TAG, "Error loading file", e);
				}

			}
			else if (activeButton.getTtsText() != null)
			{
				String ttsString = I18nUtils.getText(context,activeButton.getTtsText());

				if (ttsHelper.isTtsReady())
				{
					Log.d(Constants.TAG, "Playing TTS utterance for button '" + activeButton.getSoundButton().getLabel() + "'.");
					if (preferences.getBoolean(Constants.TTS_SAVE_PREF, false) && activeButton.getSoundButton().hasTtsOutput())
					{
						// associate the saved output with the TTS text
						int cacheReturnCode = tts.addSpeech(ttsString, activeButton.getSoundButton().getTtsOutputFile());
						if (cacheReturnCode == TextToSpeech.SUCCESS) {
							Log.d(Constants.TAG, "Associated cached sound file '" + activeButton.getSoundButton().getTtsOutputFile() + "' with TTS utterance '" + ttsString + "'.");
						}
						else {
							Log.e(Constants.TAG, "Unable to associate cached sound file '" + activeButton.getSoundButton().getTtsOutputFile() + "' with TTS utterance '" + ttsString + "'.");
						}
					}

					tts.speak(ttsString, TextToSpeech.QUEUE_FLUSH, null);
				}
				else
				{
					Log.e(Constants.TAG, "Can't speak text because TTS was not properly initialized.");
				}
			}
			else
			{
				Log.e(Constants.TAG, "No sound or speech data for button '" + activeButton.getSoundButton().getLabel() + "')");
			}
		}
	}

	public TextToSpeech getTts() {
		return tts;
	}

	private void stop() {
		if (!isPlaying())
			return;

		if (activeButton != null && activeButton.getSoundButton() != null && activeButton.getSoundButton().hasSound())
		{
			// We pause and rewind the media player because stop requires
			// reinitialization
			mediaPlayer.pause();
			mediaPlayer.seekTo(0);
		}
		if (tts != null && ttsHelper.isTtsReady())
		{
			tts.stop();
		}
	}

	private boolean isPlaying() {
		if (mediaPlayer.isPlaying())
		{
			return true;
		}
		else if (tts != null && tts.isSpeaking())
		{
			return true;
		}
		return false;
	}

	public void playSoundButton(SoundButtonView buttonToPlay) {
		if (isPlaying())
		{
			stop();
			if (activeButton == null || !activeButton.equals(buttonToPlay))
			{
				this.activeButton = buttonToPlay;
				loadSound();
				start();
			}
		}
		else {
			if (activeButton == null || !activeButton.equals(buttonToPlay))
			{
				this.activeButton = buttonToPlay;
				loadSound();
			}
			start();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		destroyTts();
		super.finalize();
	}

	public void destroyTts() {
		if (tts != null)
			TtsHelper.destroyTts(tts);
	}

	private void loadSoundFromPath(String path) {
		try
		{
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		}
		catch (Exception e)
		{
			Log.e(getClass().toString(), "Error loading file", e);
		}
	}

	private void loadSound() {
		if (activeButton.getSoundButton().getSoundPath() != null)
		{
			loadSoundFromPath(activeButton.getSoundButton().getSoundPath());
		}
	}

    public boolean isTtsReady() {
        return ttsHelper.isTtsReady();
    }
}
