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
package com.blogspot.tonyatkins.freespeech.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.atlassian.jconnect.droid.Api;

public class ExceptionHandler implements UncaughtExceptionHandler {
	public static final String SCREENSHOT_FILENAME = "/screenshot.png";
	public static final String STACKTRACE_FILENAME = "/stacktrace.txt";
	
	public static final int SAMPLE_EXCEPTION_HANDLER_REQUEST_CODE = 111;
	
	private Activity parentActivity;
	private Class<?extends Activity> exceptionHandlingActivityClass;

	/**
	 * @param parentActivity The root-level activity for this application.
	 * @param exceptionHandlingActivity The activity that will handle the exception (it will be passed the exception in a bundle).
	 */
	public ExceptionHandler(Activity parentActivity, Class<?extends Activity> exceptionHandlingActivityClass) {
		super();
		this.parentActivity = parentActivity;
		this.exceptionHandlingActivityClass = exceptionHandlingActivityClass;
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e(this.getClass().getName(), "Caught exception, preparing to handle internally", ex);

		Api.handleException(ex);

		Intent exceptionHandlingIntent = new Intent(parentActivity, exceptionHandlingActivityClass);
		PendingIntent pendingIntent = PendingIntent.getActivity(parentActivity.getApplication().getBaseContext(), 0, exceptionHandlingIntent, exceptionHandlingIntent.getFlags());

		AlarmManager mgr = (AlarmManager) parentActivity.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent);
		System.exit(2);

		parentActivity.startActivity(exceptionHandlingIntent);
		// FIXME:  Add a screenshot of whatever the user was doing
	}
}
