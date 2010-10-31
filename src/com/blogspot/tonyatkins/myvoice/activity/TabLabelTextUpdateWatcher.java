package com.blogspot.tonyatkins.myvoice.activity;

import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;

import android.text.Editable;
import android.text.TextWatcher;

class TabLabelTextUpdateWatcher implements TextWatcher {
	private int textType;
	private Tab tab;
	
	public TabLabelTextUpdateWatcher(Tab tab, int textType) {
		super();
		this.tab = tab;
		this.textType = textType;
	}

	@Override
	public void afterTextChanged(Editable s) {
		switch (textType) {
			case Tab.LABEL_TEXT_TYPE:
				tab.setLabel(s.toString());
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
