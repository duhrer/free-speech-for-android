package com.blogspot.tonyatkins.freespeech.listeners;

import com.blogspot.tonyatkins.freespeech.activity.FilePickerActivity;

import android.view.View;
import android.view.View.OnClickListener;

public class DirectoryPickedListener implements OnClickListener {
	private final String directory;
	private FilePickerActivity activity;
	
	public DirectoryPickedListener( FilePickerActivity activity,String directory) {
		super();
		this.directory = directory;
		this.activity = activity;
	}
	
	public void onClick(View v) {
		activity.setCwd(directory);
	}
}
