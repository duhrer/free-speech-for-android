/**
 * Copyright 2008-2013 Clayton Lewis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blogspot.tonyatkins.freespeech.handler;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.activity.ExceptionCatcherActivity;

public class ExceptionHandler implements UncaughtExceptionHandler {
	private final Activity parentActivity;
	private final Class<? extends Activity> exceptionHandlingActivityClass;

	public ExceptionHandler(Activity parentActivity, Class<? extends Activity> exceptionHandlingActivityClass) {
		this.parentActivity = parentActivity;
		this.exceptionHandlingActivityClass = exceptionHandlingActivityClass;
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e(Constants.TAG, "Caught exception, preparing to handle internally", ex);

		// Pass along stack trace information to exception handling class
		Intent exceptionHandlingIntent = new Intent(parentActivity, exceptionHandlingActivityClass);
		if (ex != null) {
			exceptionHandlingIntent.putExtra(ExceptionCatcherActivity.STACK_TRACE_KEY,Log.getStackTraceString(ex));
		}
		
		PendingIntent pendingIntent = PendingIntent.getActivity(parentActivity.getApplication().getBaseContext(), 0, exceptionHandlingIntent, exceptionHandlingIntent.getFlags());

		AlarmManager mgr = (AlarmManager) parentActivity.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent);
		System.exit(2);

		parentActivity.startActivity(exceptionHandlingIntent);
	}
}
