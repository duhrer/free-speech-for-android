package com.blogspot.tonyatkins.myvoice.view;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
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
	private int fileType = 0;
	
	public FileIconView(FilePickerActivity context, int imageResource, File file, int fileType) {
		super(context);
		this.file = file;
		this.fileType = fileType;
		
		int padding = 10;
		setPadding(padding, padding, padding, padding);
		
		imageLayer = new ImageView(context);
		imageLayer.setImageResource(imageResource);
		addView(imageLayer);
		
		textLayer = new TextView(context);
		textLayer.setGravity(Gravity.CENTER);
		textLayer.setTextColor(Color.WHITE);
		textLayer.setTextSize(8);
		addView(textLayer);
		
		setLabel(file.getName());
		
		// Wire in the selection button for the next stage
		if (file.isDirectory()) {
			setOnClickListener(new DirectoryPickedListener(file.getAbsolutePath(),context));
		}
		else {
			setOnClickListener(new FilePickedListener(file, context));
		}
	}

	private void setLabel(String label) {
		String cleanLabel = label;
		if (label.length() > 20) { 
			cleanLabel = label.substring(0, 17) + "...";
		}
		textLayer.setText(cleanLabel);
		textLayer.invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int centerX = getMeasuredWidth()/2;
		
		int y = getPaddingTop();
		imageLayer.layout(centerX - (imageLayer.getMeasuredWidth()/2), y, centerX + (imageLayer.getMeasuredWidth()/2) , y + imageLayer.getMeasuredHeight());
		y += imageLayer.getMeasuredHeight();
		textLayer.layout(centerX - (textLayer.getMeasuredWidth()/2), y, centerX + (textLayer.getMeasuredWidth()/2) , y + textLayer.getMeasuredHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), 2*getMeasuredWidth()/3);

		int sideWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int sideHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

		textLayer.measure(sideWidth, sideHeight/4);
		imageLayer.measure(sideWidth, 3*sideHeight/4);
		imageLayer.setMinimumHeight(3*sideHeight/4);
		imageLayer.setMinimumWidth(sideWidth);
	}
	
	private class FilePickedListener implements OnClickListener {
		private final File file;
		private Activity activity;
		
		public FilePickedListener(File file, Activity activity) {
			super();
			this.file = file;
			this.activity = activity;
		}

		@Override
		public void onClick(View v) {
			Intent returnedIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString(FilePickerActivity.FILE_NAME_BUNDLE, file.getAbsolutePath());
			bundle.putInt(FilePickerActivity.FILE_TYPE_BUNDLE, fileType);
			returnedIntent.putExtras(bundle);
			activity.setResult(FilePickerActivity.FILE_SELECTED, returnedIntent);
			activity.finish();
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
