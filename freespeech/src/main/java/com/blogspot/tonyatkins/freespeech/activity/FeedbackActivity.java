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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.blogspot.tonyatkins.freespeech.Constants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
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

		File outputDir = new File(Environment.getExternalStorageDirectory(), Constants.FEEDBACK_DIRECTORY + "/" + timestamp);
		boolean directoryCreated = outputDir.mkdirs();

        if (!directoryCreated) {
            Log.e(Constants.TAG, "Unable to create output directory, feedback is unlikely to work as expected");
        }

		// Let the user choose an installed email client
		Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { Constants.CONTACT_EMAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Free Speech (" + humanTimestamp + "...");

        HashMap<String, String> dataToWrite = new HashMap<String, String>();

		// Retrieve the stack trace if we've captured one.
		Bundle data = getIntent().getExtras();
		if (data != null)
		{
			String stackTrace = data.getString(STACK_TRACE_KEY);
			if (stackTrace != null)
			{
                dataToWrite.put("trace", stackTrace.toString());
			}
		}

		// Read the log information using logcat...
		try
		{
			String cmd = "logcat -v tag -t 1000 *:S '" + Constants.TAG + "':D";
			Process process = Runtime.getRuntime().exec(cmd);
			if (process != null)
			{
                StringBuffer logBuffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
					logBuffer.append(line);
                }
                reader.close();

                dataToWrite.put("logcat", logBuffer.toString());
			}
			else
			{
				Log.e(Constants.TAG, "No logcat process was created, can't retrieve log data...");
			}
		}
		catch (IOException e)
		{
			Log.e(Constants.TAG, "Unable to retrieve log information using logcat command...", e);
		}

		// Read the device information
		StringBuffer deviceProps = new StringBuffer();
		deviceProps.append("manufacturer:" + Build.MANUFACTURER + "\n");
		deviceProps.append("brand:" + Build.BRAND + "\n");
		deviceProps.append("model:" + Build.MODEL + "\n");
		deviceProps.append("product:" + Build.PRODUCT + "\n");
		deviceProps.append("device:" + Build.DEVICE + "\n");
		deviceProps.append("release version:" + Build.VERSION.RELEASE + "\n");
		deviceProps.append("android SDK:" + String.valueOf(Build.VERSION.SDK_INT) + "\n");

        dataToWrite.put("device", deviceProps.toString());

        // Get information about the local instance of Free Speech that's installed.
        StringBuffer fsProps = new StringBuffer();
		try {
			String versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            fsProps.append("version:" + versionName + "\n");
		}
		catch (PackageManager.NameNotFoundException e) {
			Log.e(Constants.TAG, "Can't look up version number.", e);
		}
        dataToWrite.put("freespeech", fsProps.toString());

        // Get the current preferences the user has set.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String, ?> allPrefs =  preferences.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()){
            fsProps.append(entry.getKey() + ":" + entry.getValue().toString() + "\n");
        }

        ArrayList<Uri> uris = new ArrayList<Uri>();
        Iterator<String> keys = dataToWrite.keySet().iterator();
	    while (keys.hasNext()) {
            String key = keys.next();
            String stringData = dataToWrite.get(key);
            File keyOutputFile = new File(outputDir, "/" + key + "-" + timestamp + ".txt");

            try {
                FileWriter writer = new FileWriter(keyOutputFile);
                writer.write(stringData);
                writer.close();
            }
            catch (IOException e) {
                Log.e(Constants.TAG, "Can't save " + key + " output ...");
            }

            // Not allowed in API > 24
            // Uri keyOutputUri = Uri.fromFile(keyOutputFile);
			Uri keyOutputUri = FileProvider.getUriForFile(getApplicationContext(), "com.blogspot.tonyatkins.freespeech.fileprovider", keyOutputFile);
            uris.add(keyOutputUri);
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
