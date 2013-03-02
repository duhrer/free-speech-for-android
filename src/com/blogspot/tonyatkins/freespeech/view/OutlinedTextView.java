package com.blogspot.tonyatkins.freespeech.view;

import com.blogspot.tonyatkins.freespeech.model.PerceivedColor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

public class OutlinedTextView extends TextView {
	private static final int MARGIN = 10;
	private final Paint  paint = new Paint(Paint.ANTI_ALIAS_FLAG);;
	
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
	public void draw(Canvas canvas) {
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

