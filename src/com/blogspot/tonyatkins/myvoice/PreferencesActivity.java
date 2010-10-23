package com.blogspot.tonyatkins.myvoice;

import java.util.ArrayList;
import java.util.List;

import com.blogspot.tonyatkins.myvoice.listeners.ActivityQuitListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class PreferencesActivity extends Activity {
	private static final int TTS_CHECK_CODE = 777;
	public static final int EDIT_PREFERENCES = 999;
	
	private List<String> availableVoices = new ArrayList<String>();
	private String[] sizeOptions = {"3","4","5"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_CHECK_CODE);	
		
		Spinner voiceSpinner = (Spinner) findViewById(R.id.preferences_voice_picker);
		ArrayAdapter<String> voiceSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableVoices);
		voiceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voiceSpinner.setAdapter(voiceSpinnerAdapter);
        
		Spinner sizeSpinner = (Spinner) findViewById(R.id.preferences_size_picker);
		ArrayAdapter<String> sizeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sizeOptions);
		sizeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sizeSpinner.setAdapter(sizeSpinnerAdapter);
		
		Button cancelButton = (Button) findViewById(R.id.preferences_cancel_button);
		cancelButton.setOnClickListener(new ActivityQuitListener(this));
		
		Button saveButton = (Button) findViewById(R.id.preferences_save_button);
		// FIXME: Implement a listener that actually saves the data
		saveButton.setOnClickListener(new ActivityQuitListener(this));
		
	}

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_CODE) {
        	// We're going to be really lazy about checking to see that TTS is installed properly, as our startup method won't let anyone near here unless that's true
        	availableVoices = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO);
        }
    }

}
