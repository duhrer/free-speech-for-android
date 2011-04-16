package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.myvoice.model.FileIconListAdapter;
import com.blogspot.tonyatkins.myvoice.utils.BackupUtils;
import com.blogspot.tonyatkins.myvoice.utils.SoundUtils;

public class ToolsActivity extends Activity {
	private DbAdapter dbAdapter;
	private SoundReferee soundReferee;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean fullScreen = preferences.getBoolean("fullScreen", false);
		if (fullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		super.onCreate(savedInstanceState);
		soundReferee = new SoundReferee(this);
		dbAdapter = new DbAdapter(this, soundReferee);
		
		setContentView(R.layout.tools);

		// wire up the export button
		Button exportButton = (Button) findViewById(R.id.toolsExportButton);
		exportButton.setOnClickListener(new ExportClickListener(this, dbAdapter));
		
		// wire up the import button
		Button importButton = (Button) findViewById(R.id.toolsImportButton);
		importButton.setOnClickListener(new ImportClickListener(this));
		
		// wire up the TTS refresh button
		Button ttsButton = (Button) findViewById(R.id.toolsImportButton);
		ttsButton.setOnClickListener(new TtsRefreshListener(this));
		
		// wire up the quit button
		Button exitButton = (Button) findViewById(R.id.toolsExitButton);
		exitButton.setOnClickListener(new ActivityQuitListener(this));
	}
	
	

	private class ExportClickListener implements OnClickListener {
		private Context context;
		private DbAdapter dbAdapter;
		
		public ExportClickListener(Context context, DbAdapter dbAdapter) {
			this.context = context;
			this.dbAdapter = dbAdapter;
		}

		public void onClick(View v) {
			BackupUtils.exportData(context,dbAdapter);
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
	
	private class TtsRefreshListener implements OnClickListener {
		private Context context;
		
		public TtsRefreshListener(Context context) {
			this.context = context;
		}

		@Override
		public void onClick(View v) {
			SoundUtils.checkTtsFiles(context, dbAdapter, false);
		}
	}
	
	private void promptToRetainDataAndContinue(String path) {
		// ask whether to replace existing data
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Delete existing data?");
		builder.setPositiveButton("Yes", new RestoreChoiceListener(this,dbAdapter,path,true));
		builder.setNegativeButton("No", new RestoreChoiceListener(this,dbAdapter,path,false));
		Dialog dialog = builder.create();
		dialog.show();
	}
	
	private class RestoreChoiceListener implements Dialog.OnClickListener {
		private Context context;
		private DbAdapter dbAdapter;
		
		private String path;
		private boolean result;
		
		/**
		 * @param context The Context in which to display subsequent dialogs, et cetera.
		 * @param result Whether to preserve data in the resulting restore launched by the dialog.
		 */
		public RestoreChoiceListener(Context context, DbAdapter dbAdapter,String path, boolean result) {
			this.context = context;
			this.path = path;
			this.result = result;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			BackupUtils.loadXMLFromZip(context, dbAdapter, path, result);
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
			Bundle returnedBundle = data.getExtras();
			if (returnedBundle != null) {
				if (requestCode == FilePickerActivity.REQUEST_CODE) {
					if (resultCode == FilePickerActivity.FILE_SELECTED) {
						int fileType = returnedBundle.getInt(FilePickerActivity.FILE_TYPE_BUNDLE);
						String path = returnedBundle.getString(FilePickerActivity.FILE_NAME_BUNDLE);
						if (fileType != 0) {
							if (fileType == FileIconListAdapter.BACKUP_FILE_TYPE) {
								promptToRetainDataAndContinue(path);
							}
						}
					}
				}
			}
		}
	}


	
	@Override
	protected void onDestroy() {
		if (soundReferee != null && soundReferee.getTts() != null) {
			soundReferee.destroyTts();
		}
		super.onDestroy();
	}
}
