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
package com.blogspot.tonyatkins.freespech.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;

import com.blogspot.tonyatkins.freespeech.R;

public class ColorSwatch extends TextView {
	private static final int DEFAULT_COLOR = Color.TRANSPARENT;
	private Paint whitePaint = new Paint();
	private Paint blackPaint = new Paint();
	boolean selected = false;
	
	public ColorSwatch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializePaint();
	}

	public ColorSwatch(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializePaint();
	}

	public ColorSwatch(Context context) {
		super(context);
		initializePaint();
	}

	public ColorSwatch(Context context, String colorString) {
		super(context);
		initializePaint();
		
		setBackgroundColor(colorString);
	}

	public void setBackgroundColor(String colorString) {
		try {
			int color = Color.parseColor(colorString);
			setBackgroundColor(color);
		} catch (IllegalArgumentException e) {
			setBackgroundColor(DEFAULT_COLOR);
		}
	}
	
	@Override
	public void setBackgroundColor(int color) {
		if (color == Color.TRANSPARENT || color == Color.BLACK) {
			super.setBackgroundColor(Color.TRANSPARENT);
			setBackgroundResource(R.drawable.darkgrayoutline);
		}
		else {
			setBackgroundResource(0);
			super.setBackgroundColor(color);
		}
	}

	private void initializePaint() {
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStyle(Style.STROKE);
		blackPaint.setColor(Color.BLACK);
		blackPaint.setStyle(Style.STROKE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (getBackground() != null) {
			getBackground().draw(canvas);
		}
		
		if (isSelected()) {
			// outline the view using a white square with an inner black square for contrast
			canvas.drawRect(0, 0, getMeasuredWidth()-1, getMeasuredHeight()-1, whitePaint);
			canvas.drawRect(1, 1, getMeasuredWidth()-2, getMeasuredHeight()-2, blackPaint);
		}
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
