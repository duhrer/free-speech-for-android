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
package com.blogspot.tonyatkins.freespeech.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.blogspot.tonyatkins.freespech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespech.model.SoundButton;
import com.blogspot.tonyatkins.freespech.model.Tab;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;

public class MoveButtonActivity extends Activity {
	public static final int MOVE_BUTTON = 795;
	private Spinner tabSpinner;
	private DbAdapter dbAdapter;
	private SoundButton soundButton;
	private long currentTabId;
	private Cursor tabCursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_button);

		Bundle bundle = this.getIntent().getExtras();
		String buttonId = null;
		
		// We need to know which button and tab we're working with, quit if we don't have those.
		if (bundle != null) {
			buttonId = bundle.getString(SoundButton.BUTTON_ID_BUNDLE);
			currentTabId = Long.valueOf(bundle.getString(Tab.TAB_ID_BUNDLE));
		}
		
		if (buttonId != null && currentTabId != 0) {
			dbAdapter = new DbAdapter(this);
			tabCursor = dbAdapter.fetchAllTabs();
			int numTabs = tabCursor.getCount();
			int selectedTabPosition = 0;
			
			if (numTabs > 0) {
				for (int position = 0; position < tabCursor.getCount(); position++) {
					tabCursor.moveToPosition(position);
					long nextTabId = tabCursor.getLong(tabCursor.getColumnIndex(Tab._ID));
					if (nextTabId == currentTabId) {
						selectedTabPosition = position;
						break;
					}
				}
			}
			
			soundButton = dbAdapter.fetchButtonById(buttonId);
			String[] columns = {Tab.LABEL};
			int[] destinationViews = {R.id.move_button_tab_list_entry};
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.move_button_list_entry, tabCursor, columns, destinationViews);
			adapter.setDropDownViewResource(R.layout.move_button_list_entry);
			tabSpinner = (Spinner) findViewById(R.id.moveButtonTabSpinner);
			tabSpinner.setAdapter(adapter);

			if (selectedTabPosition != 0) { tabSpinner.setSelection(selectedTabPosition); }
			
			Button cancelButton = (Button) findViewById(R.id.moveButtonCancel);
			cancelButton.setOnClickListener(new ActivityQuitListener(this));
			
			Button moveButton = (Button) findViewById(R.id.moveButtonMove);
			moveButton.setOnClickListener(new ReturnSelectionListener());
		}
		else {
			Log.e(getClass().getCanonicalName(), "MoveButtonActivity launched with no tab or button ID.  Can't continue.");
			finish();
		}
	}
	
	@Override
	public void finish() {
		if (tabCursor != null) tabCursor.close();
		if (dbAdapter != null) dbAdapter.close();
		super.finish();
	}
	
	private class ReturnSelectionListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Long tabId = tabSpinner.getSelectedItemId();
			if (tabId != AdapterView.INVALID_ROW_ID && tabId != currentTabId) {
				soundButton.setTabId(tabId);
				dbAdapter.updateButton(soundButton);
			}
			
			// set the return code to indicate that we have made a change
			setResult(Activity.RESULT_OK);
			
			finish();
		}
	}
}