package com.blogspot.tonyatkins.myvoice.activity;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.ButtonTabContentFactory;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.Tab;

public class ViewBoardActivity extends TabActivity {
	SoundReferee soundReferee;
	private DbAdapter dbAdapter;
	private TabHost tabHost;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        setContentView(R.layout.view_board);
        
        // Initialize the sound system
        soundReferee = new SoundReferee(this);
        
        // get hold of the actual grid view so we can back it with an adapter
        tabHost = getTabHost();
        
		DbAdapter dbAdapter = new DbAdapter(this);
		Cursor tabCursor =  dbAdapter.fetchAllTabs();
		// FIXME: Hide tabs if there are less than two
//		if (tabCursor.getCount() < 2) {
//			View tabWidget = getTabWidget();
//			tabWidget.setVisibility(View.GONE);
//		}
		while (tabCursor.moveToNext()) {
			 int tabId = tabCursor.getInt(tabCursor.getColumnIndex(Tab._ID));
			 String label = tabCursor.getString(tabCursor.getColumnIndex(Tab.LABEL));
			 TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(tabId));
			 tabSpec.setIndicator(label);
			 tabSpec.setContent(new ButtonTabContentFactory(this, soundReferee));
			 tabHost.addTab(tabSpec);
		}
		        
        // Wire up the volume controls so that they control the media volume for as long as we're active
        setVolumeControlStream(AudioManager.STREAM_SYSTEM);

        
        // FIXME:  Add a long-touch handler that launches a global configuration dialog
        // FIXME:  Add a global menu that displays the configuration options
        
        // FIXME:  Add an "Add Button" dialog to the global config dialog/menu
        // FIXME:  Add a "Delete Buttons" dialog to the global config dialog/menu

        // FIXME: Add a back-button handler to avoid accidental exits
        
        // FIXME: Add a home button handler to avoid accidental exits
        
        // FIXME: Add a phone button handler to avoid accidental exits
        
        // FIXME: Add a power button handler to avoid accidental exists
        
        // FIXME:  One button should have a long-press option to actually exit the program or at least toggle "safe" mode.
    }
    
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_board_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_button_menu_item:
		        Intent addButtonIntent = new Intent(this, EditButtonActivity.class);
		        Bundle addButtonBundle = new Bundle();
		        addButtonBundle.putString(Tab.TAB_ID_BUNDLE, tabHost.getCurrentTabTag());
		        addButtonIntent.putExtras(addButtonBundle);
		        startActivityForResult(addButtonIntent,EditButtonActivity.ADD_BUTTON);
		        break;
			case R.id.add_tab_menu_item:
				Intent addTabIntent = new Intent(this, EditTabActivity.class);
				startActivityForResult(addTabIntent,EditTabActivity.ADD_TAB);
				break;
			case R.id.edit_tab_menu_item:
				Intent editTabIntent = new Intent(this, EditTabActivity.class);
				Bundle editButtonBundle = new Bundle();
				editButtonBundle.putString(Tab.TAB_ID_BUNDLE, tabHost.getCurrentTabTag());
				editTabIntent.putExtras(editButtonBundle);
				startActivityForResult(editTabIntent,EditTabActivity.EDIT_TAB);
				break;
			case R.id.delete_tab_menu_item:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(true);
				if (tabHost.getChildCount() > 1) {
					builder.setTitle("Delete Tab?");
					builder.setMessage("Are you sure you want to delete this tab and all its buttons?");
					builder.setPositiveButton("Yes", new onConfirmTabDeleteListener(this));
					builder.setNegativeButton("No", new onCancelTabDeleteListener());
				}
				else {
					builder.setTitle("Can't Delete Tab");
					builder.setMessage("Can't delete this tab.  There must always be at least one tab.");
					builder.setPositiveButton("OK", new onCancelTabDeleteListener());
				}
				AlertDialog alertDialog = builder.create();
				alertDialog.show();

				break;
			case R.id.preferences_menu_item:
				Intent preferencesIntent = new Intent(this,PreferencesActivity.class);
				startActivityForResult(preferencesIntent,PreferencesActivity.EDIT_PREFERENCES);
				break;
			case R.id.quit_menu_item:
				finish();
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Stuff that has to return data in order to be useful
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case EditButtonActivity.ADD_BUTTON:
					// FIXME: Add a mechanism for refreshing tab content after adding a button
					// You may need hooks into the tab factory to do this.
					Toast.makeText(this, "Button added...", Toast.LENGTH_LONG).show();
					break;
				case EditButtonActivity.EDIT_BUTTON:
					// FIXME: Add a mechanism for refreshing tab content after adding a button
					Toast.makeText(this, "Button updated...", Toast.LENGTH_LONG).show();
					break;
				case EditTabActivity.ADD_TAB:
					tabHost.getTabWidget().invalidate();
					break;
				case EditTabActivity.EDIT_TAB:
					// Refresh the current tab
					tabHost.getCurrentTabView().invalidate();
					break;
				case PreferencesActivity.EDIT_PREFERENCES:
					soundReferee.setLocale();
					// FIXME: update number of columns in gridView if preference changes
					Toast.makeText(this, "Preferences Updated", Toast.LENGTH_LONG);
					break;
			}
		}
	}
	
	private class onConfirmTabDeleteListener implements DialogInterface.OnClickListener {
		private final Context mContext;
		
		public onConfirmTabDeleteListener(Context mContext) {
			super();
			this.mContext = mContext;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			Long tabId = (Long) getTabWidget().getTag();
			dbAdapter.deleteTab(tabId);
			dbAdapter.deleteButtonsByTab(tabId);
			getTabWidget().invalidate();
			Toast.makeText(mContext, "Tab Deleted", Toast.LENGTH_LONG).show();
		}
	}
	
	private class onCancelTabDeleteListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}

}