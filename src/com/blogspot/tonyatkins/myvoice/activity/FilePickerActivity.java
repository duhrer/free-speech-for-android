package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.model.FileIconListAdapter;

public class FilePickerActivity extends Activity {
	public final static String FILE_TYPE_BUNDLE = "fileType";
	public final static String CWD_BUNDLE = "workingDir";
	public static final int REQUEST_CODE = 567;
	public static final String FILE_NAME_BUNDLE = "fileName";
	public static final int FILE_SELECTED = 678;
	private GridView fileGridView;
	private FileIconListAdapter fileIconListAdapter;
	int fileType = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_picker);
		
		fileGridView = (GridView) findViewById(R.id.file_picker_grid);
		
		String workingDir = null;

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			// We have to retrieve the type of file before we can instantiate the ListAdapter
			fileType = bundle.getInt(FILE_TYPE_BUNDLE);
			workingDir = bundle.getString(CWD_BUNDLE);
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
		
		// Wire up a link to clear the current value
		TextView clearValue = (TextView) findViewById(R.id.file_picker_set_to_null);
		clearValue.setOnClickListener(new NullPickedListener());
		
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
		fileGridView.invalidate();
		fileGridView.invalidateViews();
	}
	
	private class NullPickedListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent returnedIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString(FilePickerActivity.FILE_NAME_BUNDLE, null);
			bundle.putInt(FilePickerActivity.FILE_TYPE_BUNDLE, fileType);
			returnedIntent.putExtras(bundle);
			setResult(FilePickerActivity.FILE_SELECTED, returnedIntent);
			finish();
		}
	}
}
