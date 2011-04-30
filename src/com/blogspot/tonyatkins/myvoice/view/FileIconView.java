package com.blogspot.tonyatkins.myvoice.view;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.tonyatkins.myvoice.activity.FilePickerActivity;
import com.blogspot.tonyatkins.myvoice.model.LabeledFile;

public class FileIconView extends LinearLayout {
	private ImageView imageLayer;
	private TextView textLayer;
	private int fileType = 0;
	
	public FileIconView(FilePickerActivity context, int imageResource, File file, int fileType) {
		super(context);
		this.fileType = fileType;
		this.setOrientation(LinearLayout.HORIZONTAL);
		
		// FIXME: Make this fill the width of the line using the right attribute
		imageLayer = new ImageView(context);
		imageLayer.setImageResource(imageResource);
		addView(imageLayer);
		
		textLayer = new TextView(context);
		textLayer.setText(file.getName());
		if (file instanceof LabeledFile) setLabel(((LabeledFile) file).getLabel());
		else setLabel(file.getName());
		addView(textLayer);
		
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
