package com.blogspot.tonyatkins.myvoice.view;

import java.io.File;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.tonyatkins.myvoice.activity.FilePickerActivity;

public class FileIconView extends LinearLayout {
	private ImageView imageLayer;
	private TextView textLayer;
	private File file;
	
	public FileIconView(FilePickerActivity context, int imageResource, File file) {
		super(context);
		this.file = file;
		
		imageLayer = new ImageView(context);
		imageLayer.setImageResource(imageResource);
		addView(imageLayer);
		
		textLayer = new TextView(context);
		textLayer.setGravity(Gravity.CENTER);
		textLayer.setTextColor(Color.WHITE);
		addView(textLayer);
		
		setLabel(file.getName());
		
		// Wire in the selection button for the next stage
		if (file.isDirectory()) {
			setOnClickListener(new DirectoryPickedListener(file.getAbsolutePath(),context));
		}
		else {
			setOnClickListener(new FilePickedListener(file));
		}
	}

	private void setLabel(String label) {
		String cleanLabel = label;
		if (label.length() > 20) { 
			cleanLabel = label.substring(0, 17) + "...";
		}
		textLayer.setText(label);
		textLayer.invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int centerX = getMeasuredWidth()/2;
		
		int startImageY = getPaddingTop();
		imageLayer.layout(centerX - (imageLayer.getMeasuredWidth()/2), startImageY, centerX + (imageLayer.getMeasuredWidth()/2) , startImageY + imageLayer.getMeasuredHeight());
		int startTextY = getMeasuredHeight() - getPaddingBottom() - textLayer.getMeasuredHeight();
		textLayer.layout(centerX - (textLayer.getMeasuredWidth()/2), startTextY, centerX + (textLayer.getMeasuredWidth()/2) , startTextY + textLayer.getMeasuredHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, 2*widthMeasureSpec/3);
		setMeasuredDimension(getMeasuredWidth(), 2*getMeasuredWidth()/3);

		int sideWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int sideHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

		textLayer.measure(sideWidth, sideHeight/4);
		imageLayer.measure(sideWidth, 3*sideHeight/4);
		imageLayer.setMinimumHeight(3*sideHeight/4);
		imageLayer.setMaxHeight(3*sideHeight/4);
		imageLayer.setMinimumWidth(3*sideHeight/4);
		imageLayer.setMaxWidth(3*sideHeight/4);
	}
	
	
	private class FilePickedListener implements OnClickListener {
		private final File file;
		
		public FilePickedListener(File file) {
			super();
			this.file = file;
		}

		@Override
		public void onClick(View v) {
			Bundle bundle = new Bundle();
			bundle.putString(FilePickerActivity.FILE_NAME_BUNDLE, file.getAbsolutePath());
		}
	}
	
	private class DirectoryPickedListener implements OnClickListener {
		private final String directory;
		private FilePickerActivity activity;
		
		public DirectoryPickedListener(String directory, FilePickerActivity activity) {
			super();
			this.directory = directory;
			this.activity = activity;
		}
		
		@Override
		public void onClick(View v) {
			activity.setCwd(directory);
		}
	}
}
