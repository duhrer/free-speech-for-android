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

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.GridView;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.adapter.SortButtonListAdapter;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class SortButtonsActivity extends FreeSpeechActivity {
	public static int REQUEST_CODE = 9174;
	private DbAdapter dbAdapter;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setResult(RESULT_OK);
    	
    	setContentView(R.layout.sort_buttons);
    	
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			dbAdapter = new DbAdapter(this);
			String existingTabId = bundle.getString(Tab.TAB_ID_BUNDLE);
			Cursor buttonCursor = dbAdapter.fetchButtonsByTabId(existingTabId);
			
			GridView gridView = (GridView) findViewById(R.id.sortButtonGridView);
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			gridView.setNumColumns(Integer.parseInt(preferences.getString(Constants.COLUMNS_PREF, Constants.DEFAULT_COLUMNS)));
			gridView.setAdapter(new SortButtonListAdapter(this, buttonCursor, dbAdapter));
		}
        
        // Find and wire up the "Done" button
		Button doneButton = (Button) findViewById(R.id.sortButtonsDoneButton);
		doneButton.setOnClickListener(new ActivityQuitListener(this));
    }
}