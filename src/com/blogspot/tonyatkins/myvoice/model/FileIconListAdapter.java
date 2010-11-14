package com.blogspot.tonyatkins.myvoice.model;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.activity.FilePickerActivity;
import com.blogspot.tonyatkins.myvoice.view.FileIconView;

public class FileIconListAdapter implements ListAdapter {
	public static final int SOUND_FILE_TYPE = 1;
	public static final int IMAGE_FILE_TYPE = 1;
	public static final String DEFAULT_DIR = "/sdcard";
	
	private FilePickerActivity activity;
	private String cwd = DEFAULT_DIR;
	private List<File> files = null;
	private int fileType;
	private final FileFilter filter;
	
	public FileIconListAdapter(FilePickerActivity activity, int fileType) {
		super();
		this.activity = activity;
		filter = new SimpleFileFilter();
		
		// set the list of files based on our current directory
		loadFiles();
	}
	

	private void loadFiles() {
		File workingDirectory = new File(cwd);
		if (workingDirectory.isDirectory()) {
			File[] workingDirFiles = workingDirectory.listFiles(filter);
			if (workingDirFiles.length > 0) {
				files = new ArrayList<File>();
				// If this isn't the root directory, add the parent to the list FIRST
				String parentDir = getParentDir();
				if (parentDir != null) {
					files.add(new File(parentDir));
				}
				// add all the rest of the files to the list of files
				for (File file: workingDirFiles) { files.add(file); }
			}
		}
	}


	private String getParentDir() {
		if (!cwd.startsWith("/sdcard") || cwd.matches("^/sdcard/?$")) { return null; }

		return cwd.replaceFirst("/[^/]+/?$", "");
	}

	@Override
	public int getCount() {
		if (files != null) { return files.size(); }
		
		return 0;
	}

	@Override
	public Object getItem(int position) {
        return null;
    }

	@Override
	public long getItemId(int position) {
        return 0;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position > files.size()) return null;

		File file = files.get(position);
		
		// return an icon with the right name and image type
		int iconResource = R.drawable.file;
		if (file.isDirectory()) iconResource = R.drawable.folder;

		FileIconView view = new FileIconView(activity,iconResource,file);
		
		// TODO:  If we're working with images, display a thumbnail
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}
	
	private class SimpleFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			// exclude null files
			if (file == null) return false;
			
			// include all directories
			if (file.isDirectory()) {
				if (file.getName().startsWith(".")) return false;
				else return true;
			}
			
			// if it's a file and empty, exclude it
			if (file.length() <= 0) return false;

			return true; 
			
//			if (fileType == SOUND_FILE_TYPE && file.getName().matches(".+.(mp3|wav)")) {
//				return true;
//			}
//			else if (fileType == IMAGE_FILE_TYPE && file.getName().matches(".+.(png|bmp|gif|jpg|jpeg)")) {
//				return true;
//			}
//			
//			return false;
		}
	}

	public String getCwd() {
		return cwd;
	}


	public void setCwd(String cwd) {
		this.cwd = cwd;
		loadFiles();
	}

}
