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
package com.blogspot.tonyatkins.myvoice.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;

import com.blogspot.tonyatkins.myvoice.R;

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
