package com.blogspot.tonyatkins.myvoice.activity;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.model.FileIconListAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class FilePickerActivity extends Activity {
	public final static String FILE_TYPE_BUNDLE = "fileType";
	public final static String CWD_BUNDLE = "workingDir";
	public static final int REQUEST_CODE = 567;
	public static final String FILE_NAME_BUNDLE = "fileName";
	private GridView fileGridView;
	private FileIconListAdapter fileIconListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_picker);
		
		fileGridView = (GridView) findViewById(R.id.file_picker_grid);
		
		int fileType=0;
		String workingDir = null;
		if (savedInstanceState != null) {
			// We have to retrieve the type of file before we can instantiate the ListAdapter
			fileType = savedInstanceState.getInt(FILE_TYPE_BUNDLE);
			workingDir = savedInstanceState.getString(CWD_BUNDLE);
		}

		if (fileType == 0) {
			fileType = FileIconListAdapter.SOUND_FILE_TYPE;
		}
		
		fileIconListAdapter = new FileIconListAdapter(this, fileType);
		fileGridView.setAdapter(fileIconListAdapter);

		// if we have working directory that's not the default, set it now
		if (workingDir != null && !workingDir.equals(FileIconListAdapter.DEFAULT_DIR)) {
			setCwd(workingDir);
		}
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.file_picker_cancel);
		cancelButton.setOnClickListener(new ActivityCancelListener());
	}
	
	private class ActivityCancelListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
		}
		
	}
	
	public void setCwd(String cwd) {
		TextView cwdLabel = (TextView) findViewById(R.id.file_picker_cwd);
		cwdLabel.setText(cwd);
		cwdLabel.invalidate();
		fileIconListAdapter.setCwd(cwd);
		fileGridView.invalidateViews();
	}
}
