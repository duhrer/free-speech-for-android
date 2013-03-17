/**
 * Copyright 2012-2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.activity;

import java.util.Set;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.db.HistoryEntryDbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespeech.model.HistoryEntry;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class KeyboardActivity extends FreeSpeechActivity {
	public static final int REQUEST_CODE = 7419;
	private EditText editText;
	private Button sayItButton;
	private TextToSpeech tts;
	private ListView historyListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.keyboard);
		
		editText = (EditText) findViewById(R.id.keyboardActivityTextEdit);

		sayItButton = (Button) findViewById(R.id.keyboardActivitySayButton);
		sayItButton.setEnabled(false);
		
		// Instantiate text to speech
		tts = new TextToSpeech(this,new TtsReadyListener());
		
		// Wire up the history listview
		historyListView = (ListView) findViewById(R.id.keyboardHistoryListView);
		loadHistoryEntryData();
		
		if (!preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true)) {
			TextView historyInstructions = (TextView) findViewById(R.id.keyboardHistoryInstructions);
			historyInstructions.setText(R.string.keyboard_history_instructions_no_edit);
			historyInstructions.invalidate();
		}
		
		// Wire up the exit button
		Button exitButton = (Button) findViewById(R.id.keyboardExitButton);
		exitButton.setOnClickListener(new ActivityQuitListener(this));
	}

	private void loadHistoryEntryData() {
		DbAdapter dbAdapter = new DbAdapter(this);
		Set<HistoryEntry> historyEntries = HistoryEntryDbAdapter.fetchAllHistoryEntries(dbAdapter.getDb());
		dbAdapter.close();

		if (historyEntries.size() == 0) {
			historyListView.setVisibility(View.GONE);
			historyListView.setAdapter(null);
		}
		else {
			historyListView.setVisibility(View.VISIBLE);
			historyListView.setAdapter(new HistoryEntrySetAdapter(historyEntries));
			historyListView.invalidate();
		}
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
				saveHistoryEntry(textToSpeak);
				loadHistoryEntryData();
			}
		}
	}
	
	private class HistoryEntrySetAdapter implements ListAdapter {
		private static final int MAX_STRING_LENGTH = 20;
		private final Set<HistoryEntry> historyEntries;
		
		private HistoryEntrySetAdapter(Set<HistoryEntry> historyEntries) {
			this.historyEntries = historyEntries;
		}

		@Override
		public int getCount() {
			return historyEntries.size();
		}

		@Override
		public Object getItem(int position) {
			return historyEntries.toArray()[position];
		}

		@Override
		public long getItemId(int position) {
			HistoryEntry historyEntry = (HistoryEntry) getItem(position);
			return historyEntry.getID();
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = new TextView(parent.getContext());
			view.setTextSize(20);
			view.setPadding(5, 10, 5, 10);

			HistoryEntry historyEntry = (HistoryEntry) getItem(position);
			String displayText = historyEntry.getTtsText();
			if (displayText.length() > MAX_STRING_LENGTH) {
				displayText = displayText.substring(0, MAX_STRING_LENGTH - 3) + "...";
			}
			view.setText(displayText);
			
			view.setOnClickListener(new SwapTextClickListener(historyEntry));
			
			// FIXME: Wire up the long click to pop up a dialog to save the text as a button
			
			return view;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}
	}
	
	private class SwapTextClickListener implements OnClickListener {
		private final HistoryEntry historyEntry;
		
		private SwapTextClickListener(HistoryEntry historyEntry) {
			this.historyEntry = historyEntry;
		}

		@Override
		public void onClick(View v) {
			// TODO:  Add some kind of limiter to only display the most recent instance of the same text or otherwise limit the results.
			String currentTtsText = editText.getText().toString();
			if (!historyEntry.getTtsText().equals(currentTtsText)) {
				saveHistoryEntry(currentTtsText);
				
				editText.setText(historyEntry.getTtsText());
				editText.invalidate();
				
				loadHistoryEntryData();
			}
		}

	}
	
	private void saveHistoryEntry(String currentTtsText) {
		if (currentTtsText != null && currentTtsText.trim().length() > 0) {
			DbAdapter dbAdapter = new DbAdapter(KeyboardActivity.this);
			HistoryEntryDbAdapter.createHistoryEntry(currentTtsText, dbAdapter.getDb());
			dbAdapter.close();
		}
	}
}
