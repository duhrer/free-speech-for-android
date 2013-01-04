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
 */
package com.blogspot.tonyatkins.freespeech.service;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RemoteViews;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.utils.TtsCacheUtils;


public class CacheUpdateService extends Service {
	public static final String STOP = "STOP";
	public static final String BUTTON_ID = "BUTTON_ID";
	public static final long ALL_BUTTONS = -1234;
	public static final long NO_BUTTONS = -4321;

	private int buttonsToProcess = 0;
	private int buttonsProcessed = 0;
	
	private Notification notification;

	private CacheButtonTtsTask task;

	private SoundReferee soundReferee;
	
	private LinkedBlockingQueue<SoundButton> buttons = new LinkedBlockingQueue<SoundButton>();
	private Intent intent;
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.soundReferee = new SoundReferee(getBaseContext());

		Log.i(Constants.TAG, "Service create() in progress...");

		task = new CacheButtonTtsTask();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		this.intent = intent;

		if (intent == null) {
			Log.e(Constants.TAG, "Cache update service was started with an empty intent.  Can't continue.");
			stopSelf();
		}
		else {
			boolean stopRunning = intent.getBooleanExtra(STOP, false);
			
			if (stopRunning) {
				if (task.isRunning() || task.scheduledExecutionTime() > 0) {
					Log.i(Constants.TAG, "Start was called with the STOP flag, cancelling...");
					task.cancel();
				}
				else {
					Log.i(Constants.TAG, "Start was called with the STOP flag, but no tasks are running or scheduled.  Shutting down.");
					stopSelf();
				}
			}
			else {
				long buttonId = intent.getLongExtra(BUTTON_ID, NO_BUTTONS);
				DbAdapter adapter = new DbAdapter(this);
				
				if (buttonId == ALL_BUTTONS) {
					TtsCacheUtils.deleteTtsFiles();
				}
				
				synchronized(buttons) {
					if (buttonId == ALL_BUTTONS) {
						Collection<SoundButton> newButtons = adapter.fetchAllButtons();
						buttons.clear();
						buttons.addAll(newButtons);
						buttonsToProcess = newButtons.size();
					}
					else {
						SoundButton button = adapter.fetchButtonById(buttonId);
						if (button != null) {
							buttons.add(button);
							buttonsToProcess++;
						}
					}
				}
				
				if (task.isRunning() || task.scheduledExecutionTime() > 0) {
					if (buttonId == ALL_BUTTONS) {
						Log.i(Constants.TAG, "'Update All' was called multiple times.  We will go through everything one last time but abort any previous runs...");
						task.cancel();
						task = new CacheButtonTtsTask();
						Timer timer = new Timer();
						timer.schedule(task, 200);
					}
				}
				else {
					Log.i(Constants.TAG, "Start was called and update task is not already running.  Starting updates...");
					Timer timer = new Timer();
					timer.schedule(task, 200);
				}
			}
			
		}
	}

	public void onDestroy() {
		Log.i(Constants.TAG, "Service destroy() in progress...");

		final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
		notificationManager.cancel(42);
		
		soundReferee.destroyTts();

		super.onDestroy();
	}

	private class CacheButtonTtsTask extends TimerTask {
		private boolean cancelled = false;
		private boolean running = false;

		@Override
		public void run() {
			running = true;
			Log.i(Constants.TAG, "Creating progress bar notification...");

			final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
			notification = new Notification(R.drawable.icon,getString(R.string.cache_progress_bar_label), System.currentTimeMillis());
			notification.contentIntent=PendingIntent.getActivity(getApplicationContext(), 0, CacheUpdateService.this.intent, 0);
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.cache_progress_bar);

			notification.contentView.setTextViewText(R.id.cacheProgressBarTotal, "0/0");
			notification.contentView.setTextViewText(R.id.cacheProgressBarPercentage, "0%");

			notification.contentView.setProgressBar(R.id.cacheProgressBar,100, 0, false);
			
			
			notificationManager.notify(42, notification);

			Log.i(Constants.TAG, "Processing sound button list...");
			int i = 0;
			try {
				while (buttons.size() > 0) {
					if (isCancelled()) {
						throw new InterruptedException("Cancelled from within while loop...");
					}
					
					SoundButton button = buttons.take();
					saveTtsToFile(button);
					buttonsProcessed++;
					
					int processedPercentage = 0;
					if (buttonsToProcess > 0) {
						processedPercentage = Math.round((buttonsProcessed/buttonsToProcess) * 100F);
					}
					
					notification.contentView.setTextViewText(R.id.cacheProgressBarTotal, buttonsProcessed + "/" + buttonsToProcess);
					notification.contentView.setTextViewText(R.id.cacheProgressBarPercentage, processedPercentage + "%");
					notification.contentView.setProgressBar(R.id.cacheProgressBar, buttonsToProcess, buttonsProcessed, false);
					notificationManager.notify(42, notification);
					
					// Pause between runs to avoid hammering the CPU
					Thread.sleep(500L);
				}
				Log.i(Constants.TAG, "Processing completed normally...");
			} 
			catch (InterruptedException e) {
				Log.i(Constants.TAG, "Processing interrupted at " + i + "%...");
				Log.i(Constants.TAG, e.getMessage());
			}
			finally {
				running = false;
				CacheUpdateService.this.stopSelf();
			}
		}

		@Override
		public boolean cancel() {
			cancelled = true;
			return super.cancel();
		}

		private boolean isCancelled() {
			return cancelled;
		}

		private boolean isRunning() {
			return running;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public boolean saveTtsToFile(SoundButton button) {
		try {
			boolean saveTTS = false;
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			saveTTS = preferences.getBoolean(Constants.TTS_SAVE_PREF, false);
			Log.d(getClass().getCanonicalName(),"Retrieved preferences, saveTTS is set to " + String.valueOf(saveTTS) + ".");
			
			if ((button.getTtsText() == null || button.getTtsText().length() == 0 || !saveTTS) && button.getTtsOutputFile() != null) {
				// remove the existing sound file if we have no TTS
				File existingFile = new File(button.getTtsOutputFile());
				if (existingFile.exists()) { 
					existingFile.delete(); 
				}
				
				return true;
			}
			else {
				// Create the directory if it doesn't exist
				File outputDir = new File(Constants.TTS_OUTPUT_DIRECTORY + "/" + button.getId());
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}
				
				TextToSpeech tts = soundReferee.getTts();
				if (tts != null) {
					// Save the file
					HashMap<String, String> myHashRender = new HashMap<String,String>();
					myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(button.getId()));
					int returnCode = tts.synthesizeToFile(button.getTtsText(), myHashRender, button.getTtsOutputFile());
					if (returnCode == TextToSpeech.SUCCESS) {
						return true;
					}
					else {
						Log.e("TTS Error", "Can't save TTS output for button.  ID: (" + button.getId() + "), TTS Text: (" + button.getTtsText() + ").  The error code was: " + returnCode);
					}
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getCanonicalName(), "Exception while saving file to TTS:", e);
		}
		
		return false;
	}
}
