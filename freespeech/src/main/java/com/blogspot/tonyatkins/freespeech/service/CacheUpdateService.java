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
package com.blogspot.tonyatkins.freespeech.service;

import java.io.File;
import java.util.Collection;
import java.util.Date;
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
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RemoteViews;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.utils.I18nUtils;
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

				if (buttonId == ALL_BUTTONS) {
					TtsCacheUtils.deleteTtsFiles();
				}
				
				synchronized(buttons) {
                    DbOpenHelper helper = new DbOpenHelper(this);
                    SQLiteDatabase db = helper.getReadableDatabase();
					if (buttonId == ALL_BUTTONS) {
						Collection<SoundButton> newButtons = SoundButtonDbAdapter.fetchAllButtons(db);
						buttons.clear();
						buttons.addAll(newButtons);
						buttonsToProcess = newButtons.size();
					}
					else {
						SoundButton button = SoundButtonDbAdapter.fetchButtonById(buttonId, db);
						if (button != null) {
							buttons.add(button);
							buttonsToProcess++;
						}
					}
                    db.close();
				}
				
				if (task.isRunning() || task.scheduledExecutionTime() > 0) {
					if (buttonId == ALL_BUTTONS) {
						Log.i(Constants.TAG, "'Update All' was called multiple times.  We will go through everything one last time but abort any previous runs...");
						task.cancel();
						task = new CacheButtonTtsTask();
						Timer timer = new Timer();
                        timer.schedule(new WaitForTtsInitTask(),100);
						timer.schedule(task, 200);
					}
				}
				else {
					Log.i(Constants.TAG, "Start was called and update task is not already running.  Starting updates...");
					Timer timer = new Timer();
                    timer.schedule(new WaitForTtsInitTask(),100);
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

    private class WaitForTtsInitTask extends TimerTask {
        @Override
        public void run() {
            Log.d(Constants.TAG,"Checking to see if TTS is ready before caching sounds.");
            Date startDate = new Date();
            while (soundReferee == null || !soundReferee.isTtsReady()) {
                Date now = new Date();
                if (now.getTime() - startDate.getTime() > Constants.TTS_INIT_TIMEOUT) {
                    Log.d(Constants.TAG,"TTS took longer than " + Constants.TTS_INIT_TIMEOUT/1000 + " seconds to initialize.  Aborting run.");
                    break;
                }

                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
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
			boolean saveTTS;
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			saveTTS = preferences.getBoolean(Constants.TTS_SAVE_PREF, false);
			Log.d(Constants.TAG,"Retrieved preferences, saveTTS is set to " + String.valueOf(saveTTS) + ".");
			
			if ((button.getTtsText() == null || button.getTtsText().length() == 0 || !saveTTS) && button.getTtsOutputFile() != null) {
				// remove the existing sound file if we have no TTS
				File existingFile = new File(Environment.getExternalStorageDirectory(), button.getTtsOutputFile());
				if (existingFile.exists()) { 
					existingFile.delete(); 
				}
				
				return true;
			}
			else {
				// Create the directory if it doesn't exist
				File outputDir = new File(Environment.getExternalStorageDirectory(), Constants.TTS_OUTPUT_DIRECTORY + "/" + button.getId());
				if (!outputDir.exists()) {
					boolean dirCreated = outputDir.mkdirs();
                    if (!dirCreated) {
                        Log.e(Constants.TAG, "Unable to create output directory, cache update service may not work.");
                    }
				}
				
				TextToSpeech tts = soundReferee.getTts();
				if (tts != null && soundReferee.isTtsReady()) {
					// Save the file
					HashMap<String, String> myHashRender = new HashMap<String,String>();
					myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(button.getId()));

					String ttsString = I18nUtils.getText(getApplicationContext(),button.getTtsText());

					int returnCode = tts.synthesizeToFile(ttsString, myHashRender, button.getTtsOutputFile());
					if (returnCode == TextToSpeech.SUCCESS) {
						return true;
					}
					else {
						Log.e("TTS Error", "Can't save TTS output for button.  ID: (" + button.getId() + "), TTS Text: (" + button.getTtsText() + ").  The error code was: " + returnCode);
					}
				}
                else {
                    Log.e(Constants.TAG,"Required TTS engine was null or not initialized, can't continue caching TTS files.");
                }
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "Exception while saving file to TTS:", e);
		}
		
		return false;
	}
}
