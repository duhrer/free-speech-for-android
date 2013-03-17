package com.blogspot.tonyatkins.freespeech.activity;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class KeyboardActivity extends FreeSpeechActivity {
	public static final int REQUEST_CODE = 7419;
	private EditText editText;
	private Button sayItButton;
	private TextToSpeech tts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.keyboard);
		
		editText = (EditText) findViewById(R.id.keyboardActivityTextEdit);

		sayItButton = (Button) findViewById(R.id.keyboardActivitySayButton);
		sayItButton.setEnabled(false);
		
		// FIXME:  Instantiate text to speech
		tts = new TextToSpeech(this,new TtsReadyListener());
		
		// FIXME:  Wire up the text editing?
		
		// FIXME:  Wire up the history listview
		// FIXME:  Only allow saving as buttons if editing is enabled?

		if (!preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true)) {
			TextView historyInstructions = (TextView) findViewById(R.id.keyboardHistoryInstructions);
			historyInstructions.setText(R.string.keyboard_history_instructions_no_edit);
			historyInstructions.invalidate();
		}
		
		// Wire up the exit button
		Button exitButton = (Button) findViewById(R.id.keyboardExitButton);
		exitButton.setOnClickListener(new ActivityQuitListener(this));
	}
	
	private class TtsReadyListener implements OnInitListener {
		@Override
		public void onInit(int status) {
			if (status == TextToSpeech.ERROR) {
				Toast.makeText(KeyboardActivity.this, "TTS error, can't continue.", Toast.LENGTH_LONG).show();
				finish();
			}
			
			sayItButton.setEnabled(true);
			sayItButton.setOnClickListener(new SayItClickListener()) ;
		}
	}
	
	private class SayItClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (tts.isSpeaking()) {
				tts.stop();
			}
			
			String textToSpeak = editText.getText().toString();
			if (textToSpeak.length() > 0)  {
				tts.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null);
			}
			
			// FIXME:  Add the text to the history
		}
	}
}
