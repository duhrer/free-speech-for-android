package com.blogspot.tonyatkins.myvoice.watchers;

import android.text.Editable;
import android.text.TextWatcher;

import com.blogspot.tonyatkins.myvoice.model.Tab;

public class TabLabelTextUpdateWatcher implements TextWatcher {
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
			case Tab.BG_COLOR_TEXT_TYPE:
				tab.setBgColor(s.toString());
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
