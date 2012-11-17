/**
 * Copyright 2012 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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