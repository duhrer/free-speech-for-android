package com.blogspot.tonyatkins.freespeech.listeners;

import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.view.SoundButtonView;

import android.view.View;
import android.view.View.OnClickListener;

public class ButtonPlayClickListener implements OnClickListener {
	private final SoundReferee soundReferee;
	
	
	public ButtonPlayClickListener(SoundReferee soundReferee) {
		super();
		this.soundReferee = soundReferee;
	}


	@Override
	public void onClick(View v) {
		soundReferee.playSoundButton((SoundButtonView) v);
	}
}
