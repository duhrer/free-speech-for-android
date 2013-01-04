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
 */
package com.blogspot.tonyatkins.freespeech.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.model.FileIconListAdapter;

public class FilePickerActivity extends FreeSpeechActivity {
	public final static String FILE_TYPE_BUNDLE = "fileType";
	public final static String CWD_BUNDLE = "workingDir";
	public static final int REQUEST_CODE = 567;
	public static final String FILE_NAME_BUNDLE = "fileName";
	public static final int FILE_SELECTED = 678;
	private ListView fileListView;
	private FileIconListAdapter fileIconListAdapter;
	int fileType = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_picker);
		
		fileListView = (ListView) findViewById(R.id.file_picker_list);
		
		String workingDir = Constants.HOME_DIRECTORY;
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			// We have to retrieve the type of file before we can instantiate the ListAdapter
			fileType = bundle.getInt(FILE_TYPE_BUNDLE);
			if (bundle.getString(CWD_BUNDLE) != null) workingDir = bundle.getString(CWD_BUNDLE);
		}

		if (fileType == 0) {
			fileType = FileIconListAdapter.SOUND_FILE_TYPE;
		}
		
		fileIconListAdapter = new FileIconListAdapter(this, fileType);
		fileListView.setAdapter(fileIconListAdapter);

		// We have to do this after the fileIconListAdapter is created
		setCwd(workingDir);
		
		// Wire up a link to clear the current value
		TextView clearValue = (TextView) findViewById(R.id.file_picker_set_to_null);
		clearValue.setOnClickListener(new NullPickedListener());
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.file_picker_cancel);
		cancelButton.setOnClickListener(new ActivityCancelListener());
	}
	
	private class ActivityCancelListener implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}
	
	public void setCwd(String cwd) {
		if (cwd == null) { return; }
		
		TextView cwdLabel = (TextView) findViewById(R.id.file_picker_cwd);
		cwdLabel.setText(cwd);
		cwdLabel.invalidate();
		fileIconListAdapter.setCwd(cwd);
		fileListView.invalidate();
		fileListView.invalidateViews();
	}
	
	private class NullPickedListener implements OnClickListener {
		public void onClick(View v) {
			Intent returnedIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString(FilePickerActivity.FILE_NAME_BUNDLE, null);
			bundle.putInt(FilePickerActivity.FILE_TYPE_BUNDLE, fileType);
			returnedIntent.putExtras(bundle);
			setResult(FilePickerActivity.FILE_SELECTED, returnedIntent);
			finish();
		}
	}
}
