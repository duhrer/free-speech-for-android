/**
 * Copyright 2012-2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
package com.blogspot.tonyatkins.freespeech.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.model.ColorWheelListAdapter;
import com.blogspot.tonyatkins.freespeech.view.ColorSwatch;

public class ColorPickerActivity extends FreeSpeechActivity {
	private GridView gridView; 
	
	private ColorWheelListAdapter colorWheelListAdapter;
	private ColorSwatch previewSwatch;
	
	final static String COLOR_BUNDLE = "color";
	final static int COLOR_SELECTED = 321;
	public static final int REQUEST_CODE = 321;
	
	private int selectedColor = Color.TRANSPARENT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.color_picker);

		previewSwatch = (ColorSwatch) findViewById(R.id.colorPickerPreviewSwatch);
		
		Bundle bundle = this.getIntent().getExtras();
		// get the existing color from the bundle
		if (bundle != null) {
			selectedColor = bundle.getInt(ColorPickerActivity.COLOR_BUNDLE);
			previewSwatch.setBackgroundColor(selectedColor);
		}
		
		// wire up the list adapter for the colors
		gridView = (GridView) findViewById(R.id.ColorPalette);
		colorWheelListAdapter = new ColorWheelListAdapter(this);
		gridView.setAdapter(colorWheelListAdapter);
		
		// wire up the link to set the color to null
		TextView textView = (TextView) findViewById(R.id.SetColorToTransparent);
		textView.setOnClickListener(new SetColorToNullListener());
				
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.CancelColor);
		cancelButton.setOnClickListener(new ActivityCancelListener());
		
		// wire up the save button
		Button selectButton = (Button) findViewById(R.id.SelectColor);
		selectButton.setOnClickListener(new SelectColorListener(this));
	}

	public void setSelectedColor(int selectedColor) {
		this.selectedColor = selectedColor;
		previewSwatch.setBackgroundColor(selectedColor);
		previewSwatch.invalidate();
		
		// highlight the selected color
		colorWheelListAdapter.setSelectedColor(selectedColor);
		gridView.invalidateViews();
	}
	
	private class ActivityCancelListener implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}
	
	private class SetColorToNullListener implements OnClickListener {
		public void onClick(View v) {
			setSelectedColor(Color.TRANSPARENT);
		}
	}
	
	private class SelectColorListener implements OnClickListener {
		private Activity activity;
		
		public SelectColorListener(Activity activity) {
			this.activity = activity;
		}

		public void onClick(View v) {
			Intent returnedIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putInt(ColorPickerActivity.COLOR_BUNDLE, selectedColor);
			returnedIntent.putExtras(bundle);
			activity.setResult(ColorPickerActivity.COLOR_SELECTED, returnedIntent);
			activity.finish();
		}
	}
}
