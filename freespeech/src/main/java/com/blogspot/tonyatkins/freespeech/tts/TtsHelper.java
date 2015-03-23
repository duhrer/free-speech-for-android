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
package com.blogspot.tonyatkins.freespeech.tts;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.locale.LocaleBuilder;

public class TtsHelper {
	private final TextToSpeech tts;
	private final Context  context;
	private boolean isTtsReady = false;
	
	public boolean isTtsReady() {
		return isTtsReady;
	}

	public TtsHelper(Context context) {
		this.context = context;
		this.tts = new TextToSpeech(context,new SimpleTtsInitListener());
	}

	public static void destroyTts(TextToSpeech tts) {
		try {
      if (tts != null) {
      	tts.shutdown();
      }
    } catch (IllegalArgumentException e) {
      Log.e(Constants.TAG, "Error shutting down TTS from TtsHelper.");
    }
	}
	
	protected boolean setLocale(Context context, TextToSpeech tts) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		Locale locale = LocaleBuilder.localeFromString(preferences.getString(Constants.TTS_VOICE_PREF, "eng-USA"));
		
		int result = tts.setLanguage(locale);


        return !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED);
	}
	
	private class SimpleTtsInitListener implements OnInitListener {
		public void onInit(int status) {
	        if (status == TextToSpeech.SUCCESS) {
	            isTtsReady = setLocale(context,tts);
	        } else {
	        	destroyTts(tts);
	        }
		}
	}

	public TextToSpeech getTts() {
		return tts;
	}
}
