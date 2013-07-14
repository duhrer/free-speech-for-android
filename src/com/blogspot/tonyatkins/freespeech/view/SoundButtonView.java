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
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.model.PerceivedColor;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;

import java.io.File;

public class SoundButtonView extends FrameLayout {
	private Context context;
	private SoundButton soundButton;

	private ImageView imageLayer;
	private OutlinedTextView textLayer;

	// used for preview buttons
	public SoundButtonView(Activity activity) {
		super(activity);
		this.context = activity;
		this.soundButton = new SoundButton(Long.parseLong("98765"), "Preview", "Preview Button", null, null, Long.parseLong("98765"));

		initialize();
	}

	public SoundButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		this.soundButton = new SoundButton(Long.parseLong("98765"), "Preview Button", "Preview Button", null, null, Long.parseLong("98765"));

		initialize();
		setBackgroundResource(R.drawable.button);

		if (attrs != null)
		{
			TypedArray viewAttributes = context.obtainStyledAttributes(attrs, R.styleable.SoundButtonView);

			int bgColor = viewAttributes.getColor(R.styleable.SoundButtonView_background_color, Color.GRAY);
			setButtonBackgroundColor(bgColor);
			soundButton.setBgColor(bgColor);
			soundButton.setImageResource(viewAttributes.getResourceId(R.styleable.SoundButtonView_background_src, View.NO_ID));
			for (int a = 0; a < attrs.getAttributeCount(); a++)
			{
				String attributeName = attrs.getAttributeName(a);
				if ("text".equals(attributeName))
				{
					String label = attrs.getAttributeValue(a);
					soundButton.setLabel(label);
				}
			}
		}
		reload();
	}

	public SoundButtonView(Activity activity, SoundButton soundButton) {
		super(activity);

		this.context = activity;
		this.soundButton = soundButton;

		initialize();
	}

	public void setButtonBackgroundColor(int bgColor) {
		if (getBackground() != null)
		{
			if (bgColor == Color.TRANSPARENT)
			{
				getBackground().setColorFilter(null);
			}
			else
			{
				getBackground().setColorFilter(bgColor, PorterDuff.Mode.MULTIPLY);
			}

			soundButton.setBgColor(bgColor);
			if (bgColor != Color.TRANSPARENT && PerceivedColor.getPerceivedBrightness(bgColor) < 126)
			{
				setTextColor(Color.WHITE);
			}
			else
			{
				setTextColor(Color.BLACK);
			}
		}
	}

	public void initialize() {
		if (context != null)
		{
			imageLayer = new ImageView(context);
			addView(imageLayer);

			textLayer = new OutlinedTextView(context);
			textLayer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
			textLayer.setGravity(Gravity.CENTER);
			addView(textLayer);

			reload();
		}
        setPadding(0,0,0,0);
	}

	private void setTextColor(int color) {
		textLayer.setTextColor(color);
	}

	private void setText(String label) {
		textLayer.setText(label);
	}

	private void setText(int resource) {
		textLayer.setText(resource);
	}
	
	private void loadImage() {
		if (soundButton.getImageResource() != SoundButton.NO_RESOURCE)
		{
			imageLayer.setImageResource(soundButton.getImageResource());
		}
		else if (soundButton.getImagePath() != null && new File(soundButton.getImagePath()).exists())
		{
			// check the size of the image before we decode it into memory
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(soundButton.getImagePath(), options);
			options.inSampleSize = calculateInSampleSize(options, Constants.MAX_IMAGE_WIDTH, Constants.MAX_IMAGE_HEIGHT);
			options.inJustDecodeBounds = false;

			try {
				Bitmap bitmap = BitmapFactory.decodeFile(soundButton.getImagePath(), options);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
				imageLayer.setImageDrawable(bitmapDrawable);
			}
			catch (OutOfMemoryError e) {
				Log.e(Constants.TAG, "Ran out of memory importing image '" + soundButton.getImagePath() + "'...", e);
			}
		}
		else
		{
			imageLayer.setImageResource(android.R.color.transparent);
		}

		invalidate();
	}

	public SoundButton getSoundButton() {
		return soundButton;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int centerX = (getPaddingLeft() + getMeasuredWidth()) / 2;
		int centerY = (getPaddingTop() + getMeasuredHeight()) / 2;

		int startImageX = centerX - (imageLayer.getMeasuredWidth() / 2);
		int startImageY = centerY - (imageLayer.getMeasuredHeight() / 2);

//		int startTextX = 0;
        int startTextX = centerX - (textLayer.getMeasuredWidth() / 2);
        int startTextY = centerY - (textLayer.getMeasuredHeight() / 2);

//		int startTextY = getMeasuredHeight() - getPaddingBottom() - textLayer.getMeasuredHeight();

		imageLayer.layout(startImageX, startImageY, startImageX + imageLayer.getMeasuredWidth(), startImageY + imageLayer.getMeasuredHeight());
		textLayer.layout(startTextX, startTextY, startTextX + textLayer.getMeasuredWidth(), startTextY + textLayer.getMeasuredHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		boolean scaleTextWidth = false;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (preferences != null)
		{
			scaleTextWidth = preferences.getBoolean(Constants.SCALE_TEXT_PREF, false);
		}

		// Calculate the desired height based on the device itself
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int smallestDeviceWidth = Math.min(metrics.heightPixels, metrics.widthPixels);
		int deviceScaledHeight = smallestDeviceWidth / 4;
		
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sideWidth = measuredWidth - getPaddingLeft() - getPaddingRight();

		int croppedHeight = deviceScaledHeight - getPaddingTop() - getPaddingBottom();
//		setMeasuredDimension(widthMeasureSpec, croppedHeight);
		setMeasuredDimension(sideWidth, croppedHeight);

		int textHeight = deviceScaledHeight / 4;
		int textWidth = sideWidth;
		
		if (scaleTextWidth)
		{
			// Scale the size of the text to match the button width
			Rect bounds = new Rect();
			textLayer.getPaint().getTextBounds((String) textLayer.getText(), 0, textLayer.getText().length(), bounds);
			float currentTextWidth = (bounds.right - bounds.left);
			float currentTextHeight = (bounds.bottom - bounds.top);
			float textScaleX = textWidth / currentTextWidth;
			float textScaleY = textHeight / currentTextHeight;
			
			float textScale = textScaleX * 6 / 10;
			if (currentTextHeight * textScale > (textHeight * 6/ 10) ) {
				textScale = textScaleY * 6 / 10;
			}
			
			textLayer.setTextSize(textLayer.getTextSize() * textScale);
		}

		// Measure the text layer after changing the font so that the bounds will be adjusted
		textLayer.measure(textWidth, textHeight);

		scaleImageLayer(sideWidth, croppedHeight);
	}

	private void scaleImageLayer(int maxWidth, int maxHeight) {
		if (maxWidth == 0 || maxHeight == 0)
			return;

		Drawable imageDrawable = imageLayer.getDrawable();
		if (imageDrawable == null || imageDrawable.getIntrinsicHeight() == 0 || imageDrawable.getIntrinsicWidth() == 0)
		{
			Log.w(Constants.TAG, "Button '" + soundButton.getLabel() + "' (id:" + soundButton.getId() + ") has an invalid image, and won't appear correctly.");
			return;
		}

		int imageWidth = imageDrawable.getIntrinsicWidth();
		int imageHeight = imageDrawable.getIntrinsicHeight();

		int scaledImageWidth = imageWidth * 9 / 10;
		int scaledImageHeight = imageHeight * 9 / 10;

		float xRatio = maxWidth / imageWidth;
		float yRatio = maxHeight / imageHeight;

		if (xRatio <= yRatio)
		{
			scaledImageWidth = (int) (imageWidth * xRatio);
			scaledImageHeight = (int) (imageHeight * xRatio);
		}
		else
		{
			scaledImageWidth = (int) (imageWidth * yRatio);
			scaledImageHeight = (int) (imageHeight * yRatio);
		}

		imageLayer.setMaxHeight(scaledImageHeight);
		imageLayer.setMaxWidth(scaledImageWidth);
		imageLayer.setMinimumHeight(scaledImageHeight);
		imageLayer.setMinimumWidth(scaledImageWidth);
		imageLayer.measure(scaledImageWidth, scaledImageHeight);
		imageLayer.setScaleType(ScaleType.FIT_XY);
	}

	public void setSoundButton(SoundButton soundButton) {
		this.soundButton = soundButton;
		initialize();
	}

	public void reload() {
		setBackgroundResource(R.drawable.button);
		loadImage();
		imageLayer.invalidate();

		String labelString = soundButton.getLabel();
		int labelResource = context.getResources().getIdentifier("com.blogspot.tonyatkins.freespeech:string/" + labelString, null, null);
		if (labelResource == 0) {
			setText(labelString);
		}
		else {
			setText(labelResource);
		}
		textLayer.invalidate();
		setButtonBackgroundColor(soundButton.getBgColor());
		invalidate();
	}

	public String getTtsText() {
		return soundButton.getTtsText();
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth)
		{
			if (width > height)
			{
				inSampleSize = Math.round((float) height / (float) reqHeight);
			}
			else
			{
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}
}
