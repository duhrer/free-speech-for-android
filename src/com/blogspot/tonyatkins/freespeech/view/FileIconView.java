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
package com.blogspot.tonyatkins.freespeech.view;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.tonyatkins.freespeech.model.LabeledFile;
import com.blogspot.tonyatkins.freespeech.activity.FilePickerActivity;

public class FileIconView extends LinearLayout {
	private ImageView imageLayer;
	private TextView textLayer;
	private int fileType = 0;
	
	public FileIconView(FilePickerActivity context, int imageResource, File file, int fileType) {
		super(context);
		this.fileType = fileType;
		this.setOrientation(LinearLayout.HORIZONTAL);
		
		// FIXME: Make this fill the width of the line using the right attribute
		imageLayer = new ImageView(context);
		imageLayer.setImageResource(imageResource);
		addView(imageLayer);
		
		textLayer = new TextView(context);
		textLayer.setText(file.getName());
		if (file instanceof LabeledFile) setLabel(((LabeledFile) file).getLabel());
		else setLabel(file.getName());
		addView(textLayer);
		
		// Wire in the selection button for the next stage
		if (file.isDirectory()) {
			setOnClickListener(new DirectoryPickedListener(file.getAbsolutePath(),context));
		}
		else {
			setOnClickListener(new FilePickedListener(file, context));
		}
	}

	private void setLabel(String label) {
		String cleanLabel = label;
		if (label.length() > 20) { 
			cleanLabel = label.substring(0, 17) + "...";
		}
		textLayer.setText(cleanLabel);
		textLayer.invalidate();
	}
	
	private class FilePickedListener implements OnClickListener {
		private final File file;
		private Activity activity;
		
		public FilePickedListener(File file, Activity activity) {
			super();
			this.file = file;
			this.activity = activity;
		}

		@Override
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
	
	private class DirectoryPickedListener implements OnClickListener {
		private final String directory;
		private FilePickerActivity activity;
		
		public DirectoryPickedListener(String directory, FilePickerActivity activity) {
			super();
			this.directory = directory;
			this.activity = activity;
		}
		
		@Override
		public void onClick(View v) {
			activity.setCwd(directory);
		}
	}
}
