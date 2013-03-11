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
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.blogspot.tonyatkins.freespeech.Constants;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class FeedbackActivity extends FreeSpeechActivity {
	public static final int REQUEST_CODE = 1357;
	public static final String STACK_TRACE_KEY = "stacktrace";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Date currentDate = new Date();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = df.format(currentDate);
		DateFormat humanDf = new SimpleDateFormat("MM-dd-yy HH:mm");
		String humanTimestamp = humanDf.format(currentDate);

		File outputDir = new File(Environment.getExternalStorageDirectory() + "/" + Constants.FEEDBACK_DIRECTORY + "/" + timestamp);
		outputDir.mkdirs();

		// Let the user choose an installed email client
		Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { Constants.CONTACT_EMAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Free Speech (" + humanTimestamp + "...");

		ArrayList<Uri> uris = new ArrayList<Uri>();

		// Retrieve the stack trace if we've captured one.
		Bundle data = getIntent().getExtras();
		if (data != null)
		{
			String stackTrace = data.getString(STACK_TRACE_KEY);
			if (stackTrace != null)
			{
				try
				{
					File traceFile = new File(outputDir.getAbsolutePath() + "/" + "trace-" + timestamp + ".txt");
					FileOutputStream fos = new FileOutputStream(traceFile);
					fos.write(stackTrace.getBytes());
					fos.close();
					Uri traceUri = Uri.fromFile(traceFile);
					uris.add(traceUri);
				}
				catch (Exception e)
				{
					Log.e(Constants.TAG, "Error creating stack trace file:", e);
				}
			}
		}

		// Read the log information using the following command:
		// logcat -v tag -t 1000 *:S Banga:D
		try
		{
			String cmd = "logcat -v tag -t 10000 *:S Banga:D";
			Process process = Runtime.getRuntime().exec(cmd);
			if (process != null)
			{
				File logCatOutput = new File(outputDir.getAbsolutePath() + "/" + "logcat-" + timestamp + ".txt");
				BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(logCatOutput));
				BufferedInputStream input = new BufferedInputStream(process.getInputStream());

				byte[] buffer = new byte[4096];
				int bytes = 0;
				while ((bytes = input.read(buffer)) != -1)
				{
					output.write(buffer);
				}
				input.close();
				output.close();

				Uri logUri = Uri.fromFile(logCatOutput);
				uris.add(logUri);
			}
			else
			{
				Log.e(Constants.TAG, "No process was created based, can't save output.");
			}
		}
		catch (IOException e)
		{
			Log.e(Constants.TAG, "Unable to retrieve log information using logcat command.", e);
		}

		// Read the device information
		HashMap<String,String> deviceProps = new HashMap<String,String>();
		deviceProps.put("manufacturer", Build.MANUFACTURER);
		deviceProps.put("brand", Build.BRAND);
		deviceProps.put("model", Build.MODEL);
		deviceProps.put("product", Build.PRODUCT);
		deviceProps.put("device", Build.DEVICE);
		deviceProps.put("release version", Build.VERSION.RELEASE);
		deviceProps.put("android SDK", Build.VERSION.SDK);
		
		try
		{
			File deviceInfoFile = new File(outputDir.getAbsolutePath() + "/" + "device-" + timestamp + ".txt");
			FileWriter writer = new FileWriter(deviceInfoFile);
			for (Entry<String,String> entry : deviceProps.entrySet()) {
				writer.append(entry.getKey() + "=" + entry.getValue() + "\r\n");
			}
			writer.close();
			Uri deviceUri = Uri.fromFile(deviceInfoFile);
			uris.add(deviceUri);
		}
		catch (Exception e)
		{
			Log.e(Constants.TAG, "Couldn't retrieve device information to add to feedback.", e);
		}
		
		
		i.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris);
		startActivityForResult(i, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
}
