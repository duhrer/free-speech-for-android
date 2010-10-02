package org.blogspot.tonyatkins.pictureboard;

import org.blogspot.tonyatkins.pictureboard.model.SoundButton;

import android.text.Editable;
import android.text.TextWatcher;

class TextUpdateWatcher implements TextWatcher {
	private int textType;
	private SoundButton soundButton;
	
	public TextUpdateWatcher(SoundButton soundButton, int textType) {
		super();
		this.soundButton = soundButton;
		this.textType = textType;
	}

	@Override
	public void afterTextChanged(Editable s) {
		switch (textType) {
			case SoundButton.LABEL_TEXT_TYPE:
				soundButton.setLabel(s.toString());
				break;
			case SoundButton.TTS_TEXT_TYPE: 
				soundButton.setTtsText(s.toString());
				break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
		// TODO Auto-generated method stub
		
	}
	
}
