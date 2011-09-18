/**
 * Copyright 2011 Tony Atkins <duhrer@gmail.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Tony Atkins ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Tony Atkins OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 */
package com.blogspot.tonyatkins.myvoice.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextEditView extends LinearLayout {
	public static final int LABEL_TEXT = 0;
	public static final int TTS_TEXT   = 1;
	
	private SoundButtonView soundButtonView;
	private int stringToUpdate;
	private Dialog parentDialog;
	
	private EditText editText;
	private TextView instructionsView;
	private LinearLayout buttonPanel;
	private Button cancelButton;
	private Button continueButton;
	
	
	public TextEditView(Context context, SoundButtonView soundButtonView, int stringToUpdate, Dialog parentDialog) {
		super(context);
		this.setOrientation(LinearLayout.VERTICAL);
		
		this.soundButtonView = soundButtonView;
		this.stringToUpdate = stringToUpdate;
		this.parentDialog = parentDialog;
		
		instructionsView = new TextView(context);
		instructionsView.setText("There should be instructions here");
		addView(instructionsView);
		
		editText = new EditText(context);
		if (stringToUpdate == LABEL_TEXT) {
			editText.setText(soundButtonView.getSoundButton().getLabel());
		}
		else {
			editText.setText(soundButtonView.getSoundButton().getTtsText());
		}
		addView(editText);
		
		buttonPanel = new LinearLayout(context);
		buttonPanel.setOrientation(LinearLayout.HORIZONTAL);
		addView(buttonPanel);
		
		cancelButton = new Button(context);
		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(new CancelClickListener());
		buttonPanel.addView(cancelButton);
		
		continueButton = new Button(context);
		continueButton.setText("Update");
		continueButton.setOnClickListener(new ContinueClickListener());
		buttonPanel.addView(continueButton);
	}
	
	private class CancelClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			parentDialog.cancel();
		}
	}
	
	private class ContinueClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (stringToUpdate == LABEL_TEXT) {
				soundButtonView.updateLabel(editText.getText().toString());
			}
			else if (stringToUpdate == TTS_TEXT) {
				soundButtonView.updateTtsText(editText.getText().toString());
			}
			parentDialog.dismiss();
		}
	}
	
	void setInstructions(String instructions) {
		instructionsView.setText(instructions);
		instructionsView.invalidate();
	}
}
