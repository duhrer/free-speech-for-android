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

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.BackupUtils;
import com.blogspot.tonyatkins.picker.activity.FilePickerActivity;
import com.blogspot.tonyatkins.picker.adapter.FileIconListAdapter;

public class ToolsActivity extends FreeSpeechActivity {
	public static final int TOOLS_REQUEST = 759;
	public static final int TOOLS_DATA_CHANGED = 957;
	
	private static final int FILE_PICKER_REQUEST = 975;
	
	private DbAdapter dbAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tools);

		// wire up the export button
		Button exportButton = (Button) findViewById(R.id.toolsExportButton);
		exportButton.setOnClickListener(new ExportClickListener(this));
		
		// wire up the import button
		Button importButton = (Button) findViewById(R.id.toolsImportButton);
		importButton.setOnClickListener(new ImportClickListener(this));
		
		// wire up the demo data button
		Button demoButton = (Button) findViewById(R.id.toolsDemoButton);
		demoButton.setOnClickListener(new LoadDemoDataListener(this));
		
		// wire up the delete all button
		Button deleteButton = (Button) findViewById(R.id.toolsDeleteButton);
		deleteButton.setOnClickListener(new DeleteDataListener(this));
		
		// wire up the TTS refresh button
		Button ttsButton = (Button) findViewById(R.id.toolsManageTts);
		ttsButton.setOnClickListener(new TtsControlLaunchListener(this));
		
		// wire up the quit button
		Button exitButton = (Button) findViewById(R.id.toolsExitButton);
		exitButton.setOnClickListener(new ActivityQuitListener(this));
		
		dbAdapter = new DbAdapter(this);
	}
	
	private class ExportClickListener implements OnClickListener {
		private Context context;
		
		public ExportClickListener(Context context) {
			this.context = context;
		}

		public void onClick(View v) {
			ProgressDialog progressDialog = ProgressDialog.show(context, "Exporting Data", "", false, false);
			BackupUtils.exportData(context, dbAdapter,progressDialog);
			progressDialog.dismiss();
		}

	}

	private class ImportClickListener implements OnClickListener {
		private Context context;
		
		public ImportClickListener(Context context) {
			this.context = context;
		}

		public void onClick(View v) {
			promptToPickBackupAndContinue(context);
		}
	}
	
	private class TtsControlLaunchListener implements OnClickListener {
		private Activity activity;
		
		public TtsControlLaunchListener(Activity activity) {
			this.activity = activity;
		}

		public void onClick(View v) {
			activity.startActivity(new Intent(activity,CacheControllerActivity.class));
		}
	}
	
	private void promptToRetainDataAndContinue(String path) {
		// ask whether to replace existing data
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Delete existing data?");
		builder.setPositiveButton("Yes", new RestoreChoiceListener(this,path,true));
		builder.setNegativeButton("No", new RestoreChoiceListener(this,path,false));
		Dialog dialog = builder.create();
		dialog.show();
	}
	
	private class LoadDemoDataListener implements OnClickListener {
		private Context context;
		
		public LoadDemoDataListener(Context context) {
			super();
			this.context = context;
		}

		public void onClick(View v) {
			// ask whether to replace existing data
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("Delete existing data and load demo data?");
			builder.setPositiveButton("Yes", new LoadDemoChoiceListener(context,true));
			builder.setNegativeButton("No", new LoadDemoChoiceListener(context,false));
			Dialog dialog = builder.create();
			dialog.show();
		}
	}
	
	private class DeleteDataListener implements OnClickListener {
		private Context context;
		
		public DeleteDataListener(Context context) {
			super();
			this.context = context;
		}

		public void onClick(View v) {
			// ask whether to replace existing data
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("Delete existing data?");
			builder.setPositiveButton("Yes", new DeleteDataChoiceListener(context,true));
			builder.setNegativeButton("No", new DeleteDataChoiceListener(context,false));
			Dialog dialog = builder.create();
			dialog.show();
		}
	}

	private class DeleteDataChoiceListener implements Dialog.OnClickListener {
		private Context context;
		boolean deleteData=false;
		
		public DeleteDataChoiceListener(Context context, boolean deleteData) {
			super();
			this.context = context;
			this.deleteData = deleteData;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (deleteData) {
				ProgressDialog progressDialog = ProgressDialog.show(context, "Exporting Data", "", false, false);
				BackupUtils.exportData(context, dbAdapter, progressDialog);
				progressDialog.dismiss();
				
				DbAdapter dbAdapter = new DbAdapter(context);
				dbAdapter.deleteAllButtons();
				dbAdapter.deleteAllTabs();
				dbAdapter.createTab("default", null, Tab.NO_RESOURCE, Color.TRANSPARENT, 0);
				dbAdapter.close();
				
				Toast.makeText(context, "All data deleted.", Toast.LENGTH_LONG).show();
				setResult(TOOLS_DATA_CHANGED);
			}
		}
	}
	
	private class LoadDemoChoiceListener implements Dialog.OnClickListener {
		private Context context;
		boolean loadData=false;
		
		public LoadDemoChoiceListener(Context context, boolean loadData) {
			super();
			this.context = context;
			this.loadData = loadData;
		}
		
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (loadData) {
				ProgressDialog progressDialog = ProgressDialog.show(context, "Exporting Data", "", false, false);
				BackupUtils.exportData(context, dbAdapter, progressDialog);
				progressDialog.dismiss();
				
				try {
					DbAdapter dbAdapter = new DbAdapter(context);
					dbAdapter.deleteAllButtons();
					dbAdapter.deleteAllTabs();
					dbAdapter.loadDemoData();
					dbAdapter.close();
					
					setResult(TOOLS_DATA_CHANGED);
					Toast.makeText(context, "Demo data loaded.", Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Log.e(getClass().getCanonicalName(), "Can't load demo data", e);
					Toast.makeText(context, "Error loading demo data, check logs for details.", Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	private class RestoreChoiceListener implements Dialog.OnClickListener {
		private Activity activity;
		
		private String path;
		private boolean result;
		
		/**
		 * @param activity The Context in which to display subsequent dialogs, et cetera.
		 * @param result Whether to preserve data in the resulting restore launched by the dialog.
		 */
		public RestoreChoiceListener(Activity activity, String path, boolean result) {
			this.activity = activity;
			this.path = path;
			this.result = result;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			setResult(TOOLS_DATA_CHANGED);
			
			ProgressDialog progressDialog = ProgressDialog.show(activity, "Restoring Data", "", false, false);
			BackupUtils.loadXMLFromZip(activity, dbAdapter, path, result, progressDialog);
			progressDialog.dismiss();
		}
	}

	public void promptToPickBackupAndContinue(Context context) {
		// prompt for backup location (using file picker)
		Intent intent = new Intent(context,FilePickerActivity.class);
		intent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.BACKUP_FILE_TYPE);
		intent.putExtra(FilePickerActivity.CWD_BUNDLE, Constants.EXPORT_DIRECTORY);
		int	requestCode = FilePickerActivity.REQUEST_CODE;
		((Activity) context).startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null) {
			Uri fileUri = data.getData();
			if (requestCode == FilePickerActivity.REQUEST_CODE) {
				if (resultCode == FilePickerActivity.FILE_SELECTED) {
					String path = fileUri.getPath();
					promptToRetainDataAndContinue(path);
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		if (dbAdapter != null) {
			dbAdapter.close();
		}
		
		super.onDestroy();
	}
}
