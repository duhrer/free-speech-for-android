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
package com.blogspot.tonyatkins.freespeech.utils;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.service.CacheUpdateService;

public class TtsCacheUtils {
	public static void deleteTtsFiles() {
		File ttsOutputDirectory = new File(Environment.getExternalStorageDirectory(), Constants.TTS_OUTPUT_DIRECTORY);
		FileUtils.recursivelyDelete(ttsOutputDirectory);
	}

	public static void rebuildTtsFiles(Activity activity) {
		Log.i(Constants.TAG, "Starting TTS cache service and updating all buttons...");
		Intent intent = new Intent(activity,CacheUpdateService.class);
		intent.putExtra(CacheUpdateService.BUTTON_ID, CacheUpdateService.ALL_BUTTONS);
		activity.startService(intent);
	}

	public static void rebuildTtsFile(SoundButton soundButton, Activity activity) {
		Log.i(Constants.TAG, "Starting TTS cache service and updating button '" + soundButton.getLabel() + "'...");
		Intent intent = new Intent(activity,CacheUpdateService.class);
		intent.putExtra(CacheUpdateService.BUTTON_ID, soundButton.getId());
		activity.startService(intent);
	}

	public static void stopService(Activity activity) {
		Log.i(Constants.TAG, "Stopping cache update service...");
		
		Intent intent = new Intent(activity,CacheUpdateService.class);
		intent.putExtra(CacheUpdateService.STOP, true);
		activity.startService(intent);
	}
}


