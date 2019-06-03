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

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.ImportExportDbAdapter;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.BackupUtils;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;
import com.blogspot.tonyatkins.picker.activity.FilePickerActivity;
import com.blogspot.tonyatkins.picker.adapter.FileIconListAdapter;

public class ToolsActivity extends FreeSpeechActivity {
	public static final int TOOLS_REQUEST = 759;
	public static final int TOOLS_DATA_CHANGED = 957;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tools);
		
		// wire up the export button
		Button exportButton = (Button) findViewById(R.id.toolsExportButton);
		exportButton.setOnClickListener(new ExportClickListener());

		Button importButton = (Button) findViewById(R.id.toolsImportButton);

		Button demoButton = (Button) findViewById(R.id.toolsDemoButton);
		
		Button defaultDataButton = (Button) findViewById(R.id.toolsRestoreDefaultButton);

		Button deleteButton = (Button) findViewById(R.id.toolsDeleteButton);

		boolean allowEditing = preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true);
		if (allowEditing) {
			importButton.setOnClickListener(new ImportClickListener(this));
			demoButton.setOnClickListener(new LoadDataListener(ImportExportDbAdapter.Data.DEMO));
			defaultDataButton.setOnClickListener(new LoadDataListener(ImportExportDbAdapter.Data.DEFAULT));
			deleteButton.setOnClickListener(new DeleteDataListener());
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
	}

	private class ExportClickListener implements OnClickListener {
		public void onClick(View v) {
			Toast.makeText(ToolsActivity.this, "Exporting Data...", Toast.LENGTH_SHORT).show();

            DbOpenHelper helper = new DbOpenHelper(ToolsActivity.this);

            SQLiteDatabase db = helper.getReadableDatabase();
			BackupUtils.exportData(ToolsActivity.this, db);
            db.close();
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
		builder.setPositiveButton("Yes", new RestoreChoiceListener(path, true));
		builder.setNegativeButton("No", new RestoreChoiceListener(path, false));
		Dialog dialog = builder.create();
		dialog.show();
	}

	private class LoadDataListener implements OnClickListener {
		private final ImportExportDbAdapter.Data data;
		public LoadDataListener(ImportExportDbAdapter.Data data) {
			super();
			this.data = data;
		}

		public void onClick(View v) {
			// ask whether to replace existing data
			AlertDialog.Builder builder = new AlertDialog.Builder(ToolsActivity.this);
			builder.setMessage("Delete existing data and load demo data?");
			builder.setPositiveButton("Yes", new LoadDataChoiceListener(data, true));
			builder.setNegativeButton("No", new LoadDataChoiceListener(data, false));
			Dialog dialog = builder.create();
			dialog.show();
		}
	}

	private class DeleteDataListener implements OnClickListener {
		public void onClick(View v) {
			// ask whether to replace existing data
			AlertDialog.Builder builder = new AlertDialog.Builder(ToolsActivity.this);
			builder.setMessage("Delete existing data?");
			builder.setPositiveButton("Yes", new DeleteDataChoiceListener(true));
			builder.setNegativeButton("No", new DeleteDataChoiceListener(false));
			Dialog dialog = builder.create();
			dialog.show();
		}
	}

	private class DeleteDataChoiceListener implements Dialog.OnClickListener {
		boolean deleteData = false;

		public DeleteDataChoiceListener(boolean deleteData) {
			super();
			this.deleteData = deleteData;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (deleteData)
			{
				Toast.makeText(ToolsActivity.this, "Exporting Data...", Toast.LENGTH_SHORT).show();

                DbOpenHelper helper = new DbOpenHelper(ToolsActivity.this);

                SQLiteDatabase db = helper.getWritableDatabase();

				BackupUtils.exportData(ToolsActivity.this, db);

				Toast.makeText(ToolsActivity.this,"Deleting Data...",Toast.LENGTH_SHORT).show();
				SoundButtonDbAdapter.deleteAllButtons(db);
				TabDbAdapter.deleteAllTabs(db);
				TabDbAdapter.createTab("default", null, Tab.NO_RESOURCE, Color.TRANSPARENT, 0, db);

                db.close();

				Toast.makeText(ToolsActivity.this, "All data deleted.", Toast.LENGTH_LONG).show();
				setResult(TOOLS_DATA_CHANGED);
			}
		}
	}

	private class LoadDataChoiceListener implements Dialog.OnClickListener {
		private final ImportExportDbAdapter.Data data;
		boolean loadData = false;

		public LoadDataChoiceListener(ImportExportDbAdapter.Data data, boolean loadData) {
			super();
			this.data = data;
			this.loadData = loadData;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (loadData)
			{
                DbOpenHelper helper = new DbOpenHelper(ToolsActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
				Toast.makeText(ToolsActivity.this, "Exporting Data...", Toast.LENGTH_SHORT).show();
				BackupUtils.exportData(ToolsActivity.this, db);

				try
				{
					SoundButtonDbAdapter.deleteAllButtons(db);
					TabDbAdapter.deleteAllTabs(db);

                    ImportExportDbAdapter dbAdapter = new ImportExportDbAdapter(ToolsActivity.this);
					dbAdapter.loadDemoData(data);
                    dbAdapter.close();

                    refreshTTS();

                    setResult(TOOLS_DATA_CHANGED);
					Toast.makeText(ToolsActivity.this, "Data loaded.", Toast.LENGTH_LONG).show();
				}
				catch (IOException e)
				{
					Log.e(Constants.TAG, "Can't load data", e);
					Toast.makeText(ToolsActivity.this, "Error loading data, check logs for details.", Toast.LENGTH_LONG).show();
				}
                finally {
                    db.close();
                }

			}
		}
	}

	private class RestoreChoiceListener implements Dialog.OnClickListener {
		private String path;
		private boolean result;

		/**
		 * @param result
		 *            Whether to preserve data in the resulting restore launched
		 *            by the dialog.
		 */
		public RestoreChoiceListener(String path, boolean result) {
			this.path = path;
			this.result = result;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			setResult(TOOLS_DATA_CHANGED);
			Toast.makeText(ToolsActivity.this, "Restoring Data...", Toast.LENGTH_SHORT).show();

            DbOpenHelper helper = new DbOpenHelper(ToolsActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();
			BackupUtils.loadXMLFromZip(ToolsActivity.this, db, path, result);
            db.close();

            refreshTTS();

			Toast.makeText(ToolsActivity.this, "Finished restoring data...", Toast.LENGTH_SHORT).show();
		}
	}

	public void promptToPickBackupAndContinue(Context context) {
		// prompt for backup location (using file picker)
		Intent intent = new Intent(context, FilePickerActivity.class);
		intent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.BACKUP_FILE_TYPE);
		File exportDir = new File(Environment.getExternalStorageDirectory(), Constants.EXPORT_DIRECTORY);
		intent.putExtra(FilePickerActivity.CWD_BUNDLE, exportDir.getAbsolutePath());
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

    // We must clear out the TTS cache if it's enabled
    private void refreshTTS() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ToolsActivity.this);
        boolean saveTTS = preferences.getBoolean(Constants.TTS_SAVE_PREF, false);

        if (saveTTS) {
            TtsCacheUtils.rebuildTtsFiles(ToolsActivity.this);
        }
    }
}
