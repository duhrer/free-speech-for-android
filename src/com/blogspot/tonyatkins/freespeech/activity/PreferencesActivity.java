/**
 * Copyright 2012 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
package com.blogspot.tonyatkins.freespeech.activity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
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

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.locale.LocaleBuilder;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;

public class PreferencesActivity extends PreferenceActivity {
  private static final int TTS_CHECK_CODE = 777;
  public static final int EDIT_PREFERENCES = 999;
  public static final int RESULT_PREFS_CHANGED = 134;
  private DbAdapter dbAdapter;
  private PreferenceChangeListener preferenceChangeListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    // Instantiating the database should create everything
    dbAdapter = new DbAdapter(this);

    preferenceChangeListener = new PreferenceChangeListener(dbAdapter, this);
    preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

    boolean fullScreen = preferences.getBoolean(Constants.FULL_SCREEN_PREF, false);
    if (fullScreen) {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
    if (dbAdapter != null)
      dbAdapter.close();
    
    super.finish();
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == TTS_CHECK_CODE) {
      // We're going to be really lazy about checking to see that TTS is
      // installed properly, as our startup method won't let anyone near
      // here unless that's true

      // For whatever reason, the list of available voices isn't nicely
      // exposed as a constant, so we hard code it.
      if (data != null) {
    	  ListPreference voiceListPreference = (ListPreference) findPreference(Constants.TTS_VOICE_PREF);
    	  ArrayList<String> voiceArrayList = data.getStringArrayListExtra("availableVoices");
    	  if (voiceArrayList == null) {
    		  Log.e(getClass().getCanonicalName(), "Can't retrieve list of available voices.");
    	  } else {
    		  String[] voiceStringEntryValues = (String[]) Array.newInstance(String.class, voiceArrayList.size());
    		  String[] voiceStringEntries = (String[]) Array.newInstance(String.class, voiceArrayList.size());
    		  
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
  }

  private class PreferenceChangeListener implements OnSharedPreferenceChangeListener {
    private DbAdapter dbAdapter;
    private final Activity activity;

    public PreferenceChangeListener(DbAdapter dbAdapter, Activity activity) {
      this.dbAdapter = dbAdapter;
      this.activity = activity;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      setResult(RESULT_PREFS_CHANGED);

      if (Constants.COLUMNS_PREF.equals(key)) {
        // This one will be taken care of when we reopen the activity,
        // so we're just displaying a confirmation
        String columnString = sharedPreferences.getString(Constants.COLUMNS_PREF, Constants.DEFAULT_COLUMNS);
        int columns = Integer.valueOf(columnString);

        Toast toast = Toast.makeText(activity, "Switched to " + columns + "-column layout.", Toast.LENGTH_SHORT);
        toast.show();
      }

      if (Constants.DEV_OPTIONS_PREF.equals(key)) {
        String message = "";
        if (sharedPreferences.getBoolean(Constants.DEV_OPTIONS_PREF, false))
          message = "Developer options enabled.";
        else
          message = "Developer options disabled";
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();
      }
      
      if (Constants.FULL_SCREEN_PREF.equals(key)) {
        // This one will be taken care of when we reopen the activity,
        // so we're just displaying a confirmation
        String message = "";
        if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_PREF, false))
          message = "Full screen enabled.";
        else
          message = "Full screen disabled";
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();
      }

      if (Constants.HIDE_TAB_CONTROLS_PREF.equals(key)) {
        String message = "";
        if (sharedPreferences.getBoolean(Constants.HIDE_TAB_CONTROLS_PREF, false))
          message = "Tab controls will be hidden.";
        else
          message = "Tab controls will be displayed.";
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();
      }
      
      if (Constants.ORIENTATION_PREF.equals(key)) {
        String orientation = sharedPreferences.getString(Constants.ORIENTATION_PREF, Constants.DEFAULT_ORIENTATION);
        Toast toast = Toast.makeText(activity, "Screen orientation set to " + orientation + ".", Toast.LENGTH_SHORT);
        toast.show();
      }
      
      if (Constants.ROWS_PREF.equals(key)) {
        // This one will be taken care of when we reopen the activity,
        // so we're just displaying a confirmation
        String rowString = sharedPreferences.getString(Constants.COLUMNS_PREF, Constants.DEFAULT_COLUMNS);
        int rows = Integer.valueOf(rowString);

        Toast toast = Toast.makeText(activity, "Switched to " + rows + "-row layout.", Toast.LENGTH_SHORT);
        toast.show();
      }

      if (Constants.SCALE_TEXT_PREF.equals(key)) {
        String message = "";
        if (sharedPreferences.getBoolean(Constants.SCALE_TEXT_PREF, false))
          message = "Text scaling enabled.";
        else
          message = "Text scaling disabled.";
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();
      }

      if (Constants.SWIPE_TAB_PREF.equals(key)) {
        String message = "";
        if (sharedPreferences.getBoolean(Constants.SWIPE_TAB_PREF, false))
          message = "Swiping to change tabs is enabled.";
        else
          message = "Swiping to change tabs is disabled.";
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();
      }
      
      if (Constants.TTS_SAVE_PREF.equals(key)) {
        String message = "";
        if (sharedPreferences.getBoolean(Constants.TTS_SAVE_PREF, false)) {
        	message = "TTS caching enabled.";
        	TtsCacheUtils.rebuildTtsFiles(activity);
        }
        else {
        	message = "TTS caching disabled.";
        	TtsCacheUtils.stopService(activity);
			TtsCacheUtils.deleteTtsFiles();
        }
        
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();
      }

      if (Constants.TTS_VOICE_PREF.equals(key)) {
        TtsCacheUtils.rebuildTtsFiles(activity);
        Toast toast = Toast.makeText(activity,
            "Voice changed to '" + sharedPreferences.getString(Constants.TTS_VOICE_PREF, "eng-USA") + "'",
            Toast.LENGTH_SHORT);
        toast.show();
      }
    }
  }
}
