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
package com.blogspot.tonyatkins.freespeech.activity;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.BackupUtils;
import com.blogspot.tonyatkins.picker.activity.FilePickerActivity;
import com.blogspot.tonyatkins.picker.adapter.FileIconListAdapter;

public class ToolsActivity extends FreeSpeechActivity {
	public static final int TOOLS_REQUEST = 759;
	public static final int TOOLS_DATA_CHANGED = 957;

	private DbAdapter dbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tools);
		
		// wire up the export button
		Button exportButton = (Button) findViewById(R.id.toolsExportButton);
		exportButton.setOnClickListener(new ExportClickListener(this));

		Button importButton = (Button) findViewById(R.id.toolsImportButton);

		Button demoButton = (Button) findViewById(R.id.toolsDemoButton);
		
		Button defaultDataButton = (Button) findViewById(R.id.toolsRestoreDefaultButton);

		Button deleteButton = (Button) findViewById(R.id.toolsDeleteButton);

		boolean allowEditing = preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true);
		if (allowEditing) {
			importButton.setOnClickListener(new ImportClickListener(this));
			demoButton.setOnClickListener(new LoadDataListener(this,DbAdapter.Data.DEMO));
			defaultDataButton.setOnClickListener(new LoadDataListener(this,DbAdapter.Data.DEFAULT));
			deleteButton.setOnClickListener(new DeleteDataListener(this));
		}
		else {
			importButton.setVisibility(View.GONE);
			demoButton.setVisibility(View.GONE);
			deleteButton.setVisibility(View.GONE);
		}

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
			Toast.makeText(context,"Exporting Data...",Toast.LENGTH_SHORT).show();
			BackupUtils.exportData(context, dbAdapter);
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
			activity.startActivity(new Intent(activity, CacheControllerActivity.class));
		}
	}

	private void promptToRetainDataAndContinue(String path) {
		// ask whether to replace existing data
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Delete existing data?");
		builder.setPositiveButton("Yes", new RestoreChoiceListener(this, path, true));
		builder.setNegativeButton("No", new RestoreChoiceListener(this, path, false));
		Dialog dialog = builder.create();
		dialog.show();
	}

	private class LoadDataListener implements OnClickListener {
		private Context context;
		private final DbAdapter.Data data;
		public LoadDataListener(Context context,DbAdapter.Data data) {
			super();
			this.context = context;
			this.data = data;
		}

		public void onClick(View v) {
			// ask whether to replace existing data
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("Delete existing data and load demo data?");
			builder.setPositiveButton("Yes", new LoadDataChoiceListener(context, data, true));
			builder.setNegativeButton("No", new LoadDataChoiceListener(context, data, false));
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
			builder.setPositiveButton("Yes", new DeleteDataChoiceListener(context, true));
			builder.setNegativeButton("No", new DeleteDataChoiceListener(context, false));
			Dialog dialog = builder.create();
			dialog.show();
		}
	}

	private class DeleteDataChoiceListener implements Dialog.OnClickListener {
		private Context context;
		boolean deleteData = false;

		public DeleteDataChoiceListener(Context context, boolean deleteData) {
			super();
			this.context = context;
			this.deleteData = deleteData;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (deleteData)
			{
				Toast.makeText(context,"Exporting Data...",Toast.LENGTH_SHORT).show();
				BackupUtils.exportData(context, dbAdapter);
				
				Toast.makeText(context,"Deleting Data...",Toast.LENGTH_SHORT).show();
				DbAdapter dbAdapter = new DbAdapter(context);
				SoundButtonDbAdapter.deleteAllButtons(dbAdapter.getDb());
				TabDbAdapter.deleteAllTabs(dbAdapter.getDb());
				TabDbAdapter.createTab("default", null, Tab.NO_RESOURCE, Color.TRANSPARENT, 0,dbAdapter.getDb());
				dbAdapter.close();

				Toast.makeText(context, "All data deleted.", Toast.LENGTH_LONG).show();
				setResult(TOOLS_DATA_CHANGED);
			}
		}
	}

	private class LoadDataChoiceListener implements Dialog.OnClickListener {
		private final Context context;
		private final DbAdapter.Data data;
		boolean loadData = false;

		public LoadDataChoiceListener(Context context, DbAdapter.Data data, boolean loadData) {
			super();
			this.context = context;
			this.data = data;
			this.loadData = loadData;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (loadData)
			{
				Toast.makeText(context,"Exporting Data...",Toast.LENGTH_SHORT).show();
				BackupUtils.exportData(context, dbAdapter);
				
				try
				{
					DbAdapter dbAdapter = new DbAdapter(context);
					SoundButtonDbAdapter.deleteAllButtons(dbAdapter.getDb());
					TabDbAdapter.deleteAllTabs(dbAdapter.getDb());
					dbAdapter.loadDemoData(data);
					dbAdapter.close();

					setResult(TOOLS_DATA_CHANGED);
					Toast.makeText(context, "Data loaded.", Toast.LENGTH_LONG).show();
				}
				catch (IOException e)
				{
					Log.e(Constants.TAG, "Can't load data", e);
					Toast.makeText(context, "Error loading data, check logs for details.", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	private class RestoreChoiceListener implements Dialog.OnClickListener {
		private Activity activity;

		private String path;
		private boolean result;

		/**
		 * @param activity
		 *            The Context in which to display subsequent dialogs, et
		 *            cetera.
		 * @param result
		 *            Whether to preserve data in the resulting restore launched
		 *            by the dialog.
		 */
		public RestoreChoiceListener(Activity activity, String path, boolean result) {
			this.activity = activity;
			this.path = path;
			this.result = result;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			setResult(TOOLS_DATA_CHANGED);
			Toast.makeText(activity,"Restoring Data...",Toast.LENGTH_SHORT).show();
			BackupUtils.loadXMLFromZip(activity, dbAdapter, path, result);
			Toast.makeText(activity,"Finished restoring data...",Toast.LENGTH_SHORT).show();
		}
	}

	public void promptToPickBackupAndContinue(Context context) {
		// prompt for backup location (using file picker)
		Intent intent = new Intent(context, FilePickerActivity.class);
		intent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.BACKUP_FILE_TYPE);
		intent.putExtra(FilePickerActivity.CWD_BUNDLE, Constants.EXPORT_DIRECTORY);
		int requestCode = FilePickerActivity.REQUEST_CODE;
		((Activity) context).startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null)
		{
			Uri fileUri = data.getData();
			if (requestCode == FilePickerActivity.REQUEST_CODE)
			{
				if (resultCode == FilePickerActivity.FILE_SELECTED)
				{
					String path = fileUri.getPath();
					promptToRetainDataAndContinue(path);
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (dbAdapter != null)
		{
			dbAdapter.close();
		}

		super.onDestroy();
	}
}
