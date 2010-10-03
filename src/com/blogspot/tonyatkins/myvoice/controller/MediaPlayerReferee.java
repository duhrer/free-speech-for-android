package com.blogspot.tonyatkins.myvoice.controller;

import android.media.MediaPlayer;

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
			activeMediaPlayer.start();
		}
	}
	
	public void stop() {
		if (activeMediaPlayer != null && activeMediaPlayer.isPlaying()) {
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
