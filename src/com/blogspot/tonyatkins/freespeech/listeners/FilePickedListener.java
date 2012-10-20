package com.blogspot.tonyatkins.freespeech.listeners;

import java.io.File;

import com.blogspot.tonyatkins.freespeech.activity.FilePickerActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class FilePickedListener implements OnClickListener {
	private Activity activity;
	private final File file;
	private int fileType;
	
	public FilePickedListener(Activity activity, File file, int fileType) {
		super();
		this.activity = activity;
		this.file = file;
		this.fileType = fileType;
	}

	public void onClick(View v) {
		Intent returnedIntent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(FilePickerActivity.FILE_NAME_BUNDLE, file.getAbsolutePath());
		bundle.putInt(FilePickerActivity.FILE_TYPE_BUNDLE, fileType);
		returnedIntent.putExtras(bundle);
		activity.setResult(FilePickerActivity.FILE_SELECTED, returnedIntent);
		activity.finish();
	}
}