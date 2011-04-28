package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;

public class MoveButtonActivity extends Activity {
	public static final int MOVE_BUTTON = 795;
	private Spinner tabSpinner;
	private DbAdapter dbAdapter;
	private SoundButton soundButton;
	private long currentTabId;
	private Cursor tabCursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_button);

		Bundle bundle = this.getIntent().getExtras();
		String buttonId = null;
		
		// We need to know which button and tab we're working with, quit if we don't have those.
		if (bundle != null) {
			buttonId = bundle.getString(SoundButton.BUTTON_ID_BUNDLE);
			currentTabId = Long.valueOf(bundle.getString(Tab.TAB_ID_BUNDLE));
		}
		
		if (buttonId != null && currentTabId != 0) {
			dbAdapter = new DbAdapter(this);
			tabCursor = dbAdapter.fetchAllTabs();
			int numTabs = tabCursor.getCount();
			int selectedTabPosition = 0;
			
			if (numTabs > 0) {
				for (int position = 0; position < tabCursor.getCount(); position++) {
					tabCursor.moveToPosition(position);
					long nextTabId = tabCursor.getLong(tabCursor.getColumnIndex(Tab._ID));
					if (nextTabId == currentTabId) {
						selectedTabPosition = position;
						break;
					}
				}
			}
			
			soundButton = dbAdapter.fetchButtonById(buttonId);
			String[] columns = {Tab.LABEL};
			int[] destinationViews = {R.id.move_button_tab_list_entry};
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.move_button_list_entry, tabCursor, columns, destinationViews);
			adapter.setDropDownViewResource(R.layout.move_button_list_entry);
			tabSpinner = (Spinner) findViewById(R.id.moveButtonTabSpinner);
			tabSpinner.setAdapter(adapter);

			if (selectedTabPosition != 0) { tabSpinner.setSelection(selectedTabPosition); }
			
			Button cancelButton = (Button) findViewById(R.id.moveButtonCancel);
			cancelButton.setOnClickListener(new ActivityQuitListener(this));
			
			Button moveButton = (Button) findViewById(R.id.moveButtonMove);
			moveButton.setOnClickListener(new ReturnSelectionListener());
		}
		else {
			Log.e(getClass().getCanonicalName(), "MoveButtonActivity launched with no tab or button ID.  Can't continue.");
			finish();
		}
	}
	
	@Override
	public void finish() {
		if (tabCursor != null) tabCursor.close();
		if (dbAdapter != null) dbAdapter.close();
		super.finish();
	}
	
	private class ReturnSelectionListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Long tabId = tabSpinner.getSelectedItemId();
			if (tabId != AdapterView.INVALID_ROW_ID && tabId != currentTabId) {
				soundButton.setTabId(tabId);
				dbAdapter.updateButton(soundButton);
			}
			
			// set the return code to indicate that we have made a change
			setResult(Activity.RESULT_OK);
			
			finish();
		}
	}
}
