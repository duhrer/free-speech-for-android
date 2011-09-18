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
package com.blogspot.tonyatkins.myvoice.view;

import java.io.File;

import com.blogspot.tonyatkins.myvoice.model.SoundButton;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FilePickerScrollView extends ScrollView {
	private TextView cwdView;
	private LinearLayout linearLayout;
	private DirectoryView directoryView;
	private String path = "/sdcard";
	private SoundButton soundButton;
	private Dialog parentDialog;
	private int fileType = ANY_FILE;
	
	public final static int SOUND_FILE = 1;
	public final static int ANY_FILE = 0;
	public final static int IMAGE_FILE = -1;
	
	
	public FilePickerScrollView(Context context, SoundButton soundButton, Dialog parentDialog, int fileType) {
		super(context);
		this.soundButton = soundButton;
		this.parentDialog = parentDialog;
		this.fileType = fileType;
		initialize(context);
	}
	
	public FilePickerScrollView(Context context, SoundButton soundButton, Dialog parentDialog, String path, int fileType) {
		super(context);
		this.soundButton = soundButton;
		this.parentDialog = parentDialog;
		this.path=path;
		this.fileType = fileType;
		initialize(context);
	}

	private void initialize(Context context) {
		linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		addView(linearLayout);
		
		if (!path.startsWith("/sdcard")) {
			path = "/sdcard";
		}
		
		// figure out if the proposed path is a directory or file and path
		File existingFile = new File(path);
		if (existingFile.exists()) {
			// use the parent directory if this is a file
			if (existingFile.isFile()) {
				path = existingFile.getParent();
			}
		}
		else {
			path = "/sdcard";
		}
		
		cwdView = new TextView(context);
		cwdView.setText("Current directory: " + path);
		linearLayout.addView(cwdView);
		
		directoryView = new DirectoryView(context, path);
		linearLayout.addView(directoryView);
		
		// add a cancel button
		Button cancelButton = new Button(context);
		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(new DialogCancelButtonListener());		
		linearLayout.addView(cancelButton);
	}


	private class DirectoryView extends LinearLayout {
		private String path;
		private Context context;
		public DirectoryView(Context context, String path) {
			super(context);
			this.context = context;
			this.path = path;
			this.setOrientation(LinearLayout.VERTICAL);
			populateUsingPath(path);
		}
		private void populateUsingPath(String path) {
			removeAllViews();
			if (!path.startsWith("/sdcard")) {
				path = "/sdcard";
			}
			File directory = new File(path);
			
			if (!path.equals("/sdcard")) {
				TextView parentTextView = new TextView(context);
				
				parentTextView.setText("parent directory");
				
				// add a handler to refresh the screen starting with the new path
				parentTextView.setOnClickListener(new DirectoryItemClickListener(this, cwdView, directory.getParent()));
				this.addView(parentTextView);
			}
			
			// this should never happen
			if (!directory.isDirectory() ) { 
				TextView errorTextView = new TextView(context);
				errorTextView.setText("Error!  File passed instead of directory.");
				this.addView(errorTextView);
			}
			else if (directory.listFiles() == null || directory.listFiles().length <= 0 ) {
				TextView errorTextView = new TextView(context);
				errorTextView.setText("No files found");
				this.addView(errorTextView);
			}
			else {
				for ( File file : directory.listFiles()) {
					TextView fileTextView = new TextView(context);
					
					if (file.isDirectory()) {
						fileTextView.setText(file.getName() + " (dir)");
						// add a handler to refresh the screen starting with the new path
						fileTextView.setOnClickListener(new DirectoryItemClickListener(this, cwdView, file.getPath()));
						this.addView(fileTextView);
					}
					else if (fileType == SOUND_FILE && !file.getName().matches("^.+.(wav|mp3|ogg|3gp)$")){
						// Do not display non-sound files
					}
					else if (fileType == IMAGE_FILE && !file.getName().matches("^.+.(png|gif|jpg|bmp|jpeg)$")){
						// Do not display non-image files
					}
					else {
						fileTextView.setText(file.getName());
						this.addView(fileTextView);
						fileTextView.setOnClickListener(new FileItemClickListener(file));
					}
					
				}
			}
		}
	}

	private class DirectoryItemClickListener implements OnClickListener {
		DirectoryView parent;
		TextView cwdView;
		String path;
		public DirectoryItemClickListener(DirectoryView parent, TextView cwdView, String path) {
			super();
			this.parent = parent;
			this.cwdView = cwdView;
			this.path = path;
		}
		public void onClick(View v) {
			parent.populateUsingPath(path);
			cwdView.setText(path);
		}
	}
	
	private class FileItemClickListener implements OnClickListener {
		File file;
		
		public FileItemClickListener(File file) {
			this.file = file;
		}
		public void onClick(View v) {
			if (fileType == IMAGE_FILE) {
				soundButton.setImagePath(file.getAbsolutePath());
				// FIXME: reload the image when it changes
			}
			else {
				soundButton.setSoundPath(file.getAbsolutePath());
			}
			parentDialog.dismiss();
		}
	}
	
    private class DialogCancelButtonListener implements View.OnClickListener {
		public void onClick(View v) {
			parentDialog.cancel();
		}
    }

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

}


