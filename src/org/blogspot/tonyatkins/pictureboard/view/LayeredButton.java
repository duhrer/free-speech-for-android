package org.blogspot.tonyatkins.pictureboard.view;

import org.blogspot.tonyatkins.pictureboard.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class LayeredButton extends ViewGroup {
	private ImageButton imageButton;
	private TextView textOverlay;
	private String text;
	private float textWidth;
	private float textHeight;

	public LayeredButton(Context context, String text) {
		super(context);
		this.text = text;
		
		textOverlay = new TextView(context);
		textOverlay.setText(text);
//		textOverlay.setBackgroundColor(Color.RED);
		
		textHeight = (float) (textOverlay.getPaint().getTextSize() * 1.25);
		textWidth = (float) (textWidth * text.length() * 1.25);
		
		Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		float ratio = textHeight / originalImage.getHeight();
		
		Matrix matrix = new Matrix();
		matrix.postScale(ratio, ratio);
		
		Bitmap scaledImage = Bitmap.createBitmap(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), matrix, true);
		
		imageButton = new ImageButton(context);
		imageButton.setImageBitmap(scaledImage);
		
		Bitmap originalBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.button);
		
//		float backgroundImageRatio = textHeight / originalBackgroundImage.getHeight();
//		Matrix backgroundMatrix = new Matrix();
//		matrix.postScale(backgroundImageRatio, backgroundImageRatio);
//
//		Bitmap scaledBackgroundImage = Bitmap.createBitmap(originalBackgroundImage, 0,0, originalBackgroundImage.getWidth(), originalBackgroundImage.getHeight(), backgroundMatrix, true );
		
		imageButton.setBackgroundResource(R.drawable.button);
		addView(imageButton);
		
		textOverlay.setGravity(Gravity.CENTER);
		textOverlay.setHeight(LayoutParams.FILL_PARENT);
		addView(textOverlay);
		textOverlay.bringToFront();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		imageButton.setMaxHeight(getMeasuredHeight());
		imageButton.setMaxWidth (getMeasuredWidth());
		imageButton.measure(getMeasuredWidth(), getMeasuredHeight());
		textOverlay.setMinimumWidth((int) textWidth);
		textOverlay.setMinimumHeight((int) textHeight);
		textOverlay.measure(getMeasuredWidth(), getMeasuredHeight());
	}
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		imageButton.layout(0, 0, imageButton.getMeasuredWidth(), imageButton.getMeasuredHeight());
		textOverlay.layout(getMeasuredWidth()/2 - textOverlay.getMeasuredWidth()/2, getMeasuredHeight()/2 - textOverlay.getMeasuredHeight()/2
, getMeasuredWidth()/2 - textOverlay.getMeasuredWidth()/2, getMeasuredHeight()/2 + textOverlay.getMeasuredHeight()/2
);
	}

}
