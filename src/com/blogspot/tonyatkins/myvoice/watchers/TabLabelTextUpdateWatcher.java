/**
 * Copyright (C) 2011 Tony Atkins <duhrer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
