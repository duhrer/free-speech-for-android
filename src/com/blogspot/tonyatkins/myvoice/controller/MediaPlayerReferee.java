package com.blogspot.tonyatkins.myvoice.controller;

import android.media.MediaPlayer;
import android.util.Log;

public class MediaPlayerReferee {
	private MediaPlayer activeMediaPlayer;

	public MediaPlayer getActiveMediaPlayer() {
		return activeMediaPlayer;
	}

	public void setActiveMediaPlayer(MediaPlayer activeMediaPlayer) {
		// If there's a previous media player, stop it first
		stop();
		
		this.activeMediaPlayer = activeMediaPlayer;
	}
	
	public void start() {
		if (activeMediaPlayer != null && !activeMediaPlayer.isPlaying()) {
			try {
				activeMediaPlayer.start();
			} catch (Exception e) {
				Log.e(getClass().toString(), "Error loading file", e);
			} 
		}
	}
	
	public void stop() {
		if (activeMediaPlayer != null && activeMediaPlayer.isPlaying()) {
			// We pause and rewind the media player because stop requires reinitialization
			activeMediaPlayer.pause();
			activeMediaPlayer.seekTo(0);
		}
	}

	
	public boolean isPlaying() {
		if (activeMediaPlayer != null && activeMediaPlayer.isPlaying()) {
			return true;
		}
		
		return false;
	}
}
