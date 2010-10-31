package com.blogspot.tonyatkins.myvoice.activity;

import com.blogspot.tonyatkins.myvoice.model.SoundButton;

import android.text.Editable;
import android.text.TextWatcher;

class ButtonLabelTextUpdateWatcher implements TextWatcher {
	private int textType;
	private SoundButton soundButton;
	
	public ButtonLabelTextUpdateWatcher(SoundButton soundButton, int textType) {
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
			case SoundButton.BG_COLOR_TEXT_TYPE:
				soundButton.setBgColor(s.toString());
				break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
	}
	
}
