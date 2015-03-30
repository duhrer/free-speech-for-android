/**
 * Copyright 2012-2015 Upright Software <info@uprightsoftware.com>. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Upright Software ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Upright Software OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.view;

import com.blogspot.tonyatkins.freespeech.model.PerceivedColor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;

public class OutlinedTextView extends TextView {
	private static final int MARGIN = 10;
	private final Paint  paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public OutlinedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializePaint();
	}
	
	private void initializePaint() {
		initializePaint(getPaint().getColor());
	}

	private void initializePaint(int color) {
		int baseColor = Color.WHITE;
		if (PerceivedColor.getPerceivedBrightness(color) >= 126) {
			baseColor = Color.BLACK;
		}
		
		int transparentColor = Color.argb(85, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
		
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setColor(transparentColor);
	}

	public OutlinedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializePaint();
	}

	public OutlinedTextView(Context context) {
		super(context);
		initializePaint();
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		RectF rect = new RectF(0 - MARGIN, 0 - MARGIN, getWidth() + MARGIN, getHeight() + MARGIN);
		canvas.drawRoundRect(rect, getHeight()-10, getHeight()-10, paint);
		super.draw(canvas);
	}
	
	@Override
	public void setTextColor(int color) {
		super.setTextColor(color);
		initializePaint(color);
	}
}

