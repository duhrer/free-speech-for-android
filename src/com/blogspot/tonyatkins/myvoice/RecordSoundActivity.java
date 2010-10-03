package com.blogspot.tonyatkins.myvoice;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RecordSoundActivity extends Activity {
	public final static int REQUEST_CODE = 777;

	public final static int SOUND_SAVED = 766;
	public final static int CANCELLED = 755;
	
	private MediaRecorder mediaRecorder = new MediaRecorder();
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private TextView recordingStatus;
	private Button recordButton;
	private Button playButton;
	private Button stopButton;
	private Button saveButton;
	private Button cancelButton;
	private String soundFilePath; 
	private Context context = this;
	
	static final String RECORDING_BUNDLE = "recordingBundle";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_sound);
		
		
		soundFilePath = Constants.HOME_DIRECTORY + "/test.mp4";

		// Throw a warning and disable the "save" button if there's no mic
		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mediaRecorder.setOutputFile(soundFilePath);
		} 
		catch (Exception e) {
			Toast noMicToast = Toast.makeText(this, "Sound recording is only possible on units with a microphone installed.", Toast.LENGTH_SHORT);
			noMicToast.show();
			e.printStackTrace();
			finish();
		}
		
		// A quick and dirty status text view to let us know what's going on
		// FIXME:  Replace this with an equalizer for sound levels, a time code, something more dynamic
		recordingStatus = (TextView) findViewById(R.id.RecordingStatus);
		
		recordingStatus.setText("No sound data.  Press 'Record' to start recording.");
					
		// Grab the handles of our buttons
		playButton = (Button) findViewById(R.id.play_button);
		
		stopButton = (Button) findViewById(R.id.stop_button);
		
		saveButton = (Button) findViewById(R.id.edit_sound_save);
		
		recordButton = (Button) findViewById(R.id.record_button);
		recordButton.setOnClickListener(new StartRecordingListener());
		
		cancelButton = (Button) findViewById(R.id.edit_sound_cancel);
		cancelButton.setOnClickListener(new CancelListener());
	}
	
	private class StartRecordingListener implements OnClickListener {
		public void onClick(View v) {
			recordingStatus.setText("Now recording.  Press 'Stop' or 'Record' to stop recording.");
			// disable the play and save buttons
			playButton.setOnClickListener(null);
			saveButton.setOnClickListener(null);
			
			try {
				v.setOnClickListener(new StopRecordingListener());
				stopButton.setOnClickListener(new StopRecordingListener());

				// make sure the file we're writing to exists
				File file = new File(soundFilePath);
				file.createNewFile();
				
				mediaRecorder.prepare();
				mediaRecorder.start();
			} catch (Exception e) {
				recordingStatus.setText("Can't start recorder:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	private class StopRecordingListener implements OnClickListener {
		public void onClick(View v) {
			v.setOnClickListener(new StartRecordingListener());
			stopButton.setOnClickListener(null);				
			
			try {
				// stop the recording
				mediaRecorder.stop();
				mediaRecorder.release();
				
				try {
					// wire up the playback
					mediaPlayer.setDataSource(soundFilePath);
					playButton.setOnClickListener(new PlayRecordingListener());
					recordingStatus.setText("Recorded " + mediaPlayer.getDuration() + " seconds of audio.  Press 'Play' to preview or 'Save' to finish.");
				} catch (Exception e) {
					recordingStatus.setText("Can't setup preview playback:" + e.getMessage());
					e.printStackTrace();
				}
			} catch (Exception e) {
				Toast.makeText(context, "No recording in progress to stop.", Toast.LENGTH_LONG);
				e.printStackTrace();
			}
			
			saveButton.setOnClickListener(new SaveListener());
		}
	}
	private class PlayRecordingListener implements OnClickListener {
		public void onClick(View v) {
			try {
				// disable recording during playing
				recordButton.setOnClickListener(null);
				
				mediaPlayer.prepare();
				mediaPlayer.start();
				
				recordingStatus.setText("Previewing audio. Press 'Stop' to finish preview.");
				// wire up the stop button
				stopButton.setOnClickListener(new StopPlaybackListener());
				
				// wire up the play button to stop if it's hit again
				playButton.setOnClickListener(new StopPlaybackListener());
				
				// Can't save until we're finished recording
				saveButton.setOnClickListener(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private class StopPlaybackListener implements OnClickListener {
		public void onClick(View v) {
			mediaPlayer.stop();
			mediaPlayer.release();
			
			recordingStatus.setText("Recorded " + mediaPlayer.getDuration() + " seconds of audio.  Press 'Play' to preview or 'Save' to finish.");
			
			// the stop button should no longer be clickable
			stopButton.setOnClickListener(null);
			
			// the play button should play again
			playButton.setOnClickListener(new PlayRecordingListener());
			
			// reenable recording after playback
			recordButton.setOnClickListener(new StartRecordingListener());
			
			// wire up the save button now that we have content
			saveButton.setOnClickListener(new SaveListener());
		}
	}
	
	private class CancelListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			setResult(CANCELLED);
			finish();
		}
	}
	
	private class SaveListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			Intent returnedIntent = new Intent();
			Bundle returnedBundle = new Bundle();
			
			File soundFile = new File(soundFilePath);
			if (soundFile.exists() && soundFile.length() > 0) {
				returnedBundle.putString(RECORDING_BUNDLE, soundFilePath);
				returnedIntent.putExtras(returnedBundle);
			}
			
			setResult(SOUND_SAVED,returnedIntent);
			finish();
		}
	}
}
