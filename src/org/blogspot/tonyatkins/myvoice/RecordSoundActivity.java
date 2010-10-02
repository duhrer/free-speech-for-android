package org.blogspot.tonyatkins.myvoice;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class RecordSoundActivity extends Activity {
	public final static int REQUEST_CODE = 777;

	public final static int SOUND_SAVED = 766;
	public final static int CANCELLED = 755;
	
	private MediaRecorder mediaRecorder = new MediaRecorder();
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private Button recordButton;
	private Button playButton;
	private Button stopButton;
	private Button saveButton;
	private Button cancelButton;
	private String soundFilePath; 
	
	static final String RECORDING_BUNDLE = "recordingBundle";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_sound);
		
		File root = Environment.getExternalStorageDirectory();
		soundFilePath = root.getAbsolutePath() + "/org.blogspot.tonyatkins.pictureboard/" + "foo.3gp";

		// Throw a warning and disable the "save" button if there's no mic
		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setOutputFile(soundFilePath);
			
		} 
		catch (Exception e) {
			Toast noMicToast = Toast.makeText(this, "Sound recording is only possible on units with a microphone installed.", Toast.LENGTH_SHORT);
			noMicToast.show();
			e.printStackTrace();
			finish();
		}
					
		// Grab the handles of our buttons
		playButton = (Button) findViewById(R.id.play_button);
		
		// These will be wired up if we actually record sound.  For now, just get the handles.
		stopButton = (Button) findViewById(R.id.stop_button);
		saveButton = (Button) findViewById(R.id.edit_sound_cancel);
		
		recordButton = (Button) findViewById(R.id.record_button);
		recordButton.setOnClickListener(new StartRecordingListener());
		
		cancelButton = (Button) findViewById(R.id.edit_sound_cancel);
		cancelButton.setOnClickListener(new CancelListener());
	}
	
	private class StartRecordingListener implements OnClickListener {
		public void onClick(View v) {
			// disabled the play and stop buttons
			playButton.setOnClickListener(null);
			stopButton.setOnClickListener(null);
			try {
				mediaRecorder.prepare();
				mediaRecorder.start();
				v.setOnClickListener(new StopRecordingListener());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private class StopRecordingListener implements OnClickListener {
		public void onClick(View v) {
			mediaRecorder.stop();
			mediaRecorder.release();
			v.setOnClickListener(new StartRecordingListener());
			
			// wire up the playback
		    try {
				mediaPlayer.setDataSource(soundFilePath);
				playButton.setOnClickListener(new PlayRecordingListener());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	private class PlayRecordingListener implements OnClickListener {
		public void onClick(View v) {
			try {
				// disable recording during playing
				recordButton.setOnClickListener(null);
				
				mediaPlayer.prepare();
				mediaPlayer.start();
				
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
