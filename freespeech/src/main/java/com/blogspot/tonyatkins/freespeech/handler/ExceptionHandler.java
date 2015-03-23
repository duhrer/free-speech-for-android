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

        // Log the message even though we are planning to handle it internally.
        ex.printStackTrace();

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
