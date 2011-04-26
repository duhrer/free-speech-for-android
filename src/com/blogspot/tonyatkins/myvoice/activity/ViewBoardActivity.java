package com.blogspot.tonyatkins.myvoice.activity;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.ButtonTabContentFactory;
import com.blogspot.tonyatkins.myvoice.model.Tab;
import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

public class ViewBoardActivity extends TabActivity {
	public static final int RESULT_RESTART_REQUIRED = 8579;
	private DbAdapter dbAdapter;
	private SoundReferee soundReferee;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean fullScreen = preferences.getBoolean(Constants.FULL_SCREEN_PREF, false);
    	if (fullScreen) {
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}

    	dbAdapter = new DbAdapter(this, soundReferee);
		
        setContentView(R.layout.view_board);
        
        // Initialize the sound system
        soundReferee = new SoundReferee(this);
        
        loadTabs();
        
        // Wire up the volume controls so that they control the media volume for as long as we're active
        setVolumeControlStream(AudioManager.STREAM_SYSTEM);
                
        // FIXME:  Add a "Delete Buttons" dialog to the global config dialog/menu
        // FIXME: Add a back-button handler to avoid accidental exits
        // FIXME: Add a home button handler to avoid accidental exits
        // FIXME: Add a phone button handler to avoid accidental exits
        // FIXME: Add a power button handler to avoid accidental exists
        
        // FIXME:  One button should have a long-press option to actually exit the program or at least toggle "safe" mode.
    }
    
	public void loadTabs() {
		TabHost tabHost = getTabHost();
		String currentTag = tabHost.getCurrentTabTag();

		if (currentTag == null) { 
			currentTag = dbAdapter.getDefaultTabId();
		}
		
		// We have to work around a bug by resetting the tab to 0 when we reload the content
		tabHost.setCurrentTab(0);
		
		Cursor tabCursor =  dbAdapter.fetchAllTabs();
		View tabWidget = getTabWidget();

		// Hide the tab bar if we only have one tab
		if (tabCursor.getCount() < 2) {
			tabWidget.setVisibility(View.GONE);
		}
		else {
			tabWidget.setVisibility(View.VISIBLE);
		}
		
		setTabBgColor(Color.BLACK);
		
		while (tabCursor.moveToNext()) {
			 int tabId = tabCursor.getInt(tabCursor.getColumnIndex(Tab._ID));
			 String label = tabCursor.getString(tabCursor.getColumnIndex(Tab.LABEL));
			 
			 if (currentTag != null && tabId == Integer.parseInt(currentTag)) {
				 setTabBgColor(tabCursor.getString(tabCursor.getColumnIndex(Tab.BG_COLOR)));
			 }
			 
			 TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(tabId));
			 tabSpec.setIndicator(label);
			 tabSpec.setContent(new ButtonTabContentFactory(this, soundReferee));
			 tabHost.addTab(tabSpec);
		}
		dbAdapter.close();
		tabHost.setCurrentTabByTag(currentTag);
		tabHost.setOnTabChangedListener(new ColoredTabChangeListener(this));
		
		setTabTextColors();
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
		        addButtonIntent.putExtra(Tab.TAB_ID_BUNDLE, getTabHost().getCurrentTabTag());
		        startActivityForResult(addButtonIntent,EditButtonActivity.ADD_BUTTON);
		        break;
			case R.id.add_tab_menu_item:
				Intent addTabIntent = new Intent(this, EditTabActivity.class);
				startActivityForResult(addTabIntent,EditTabActivity.ADD_TAB);
				break;
			case R.id.edit_tab_menu_item:
				Intent editTabIntent = new Intent(this, EditTabActivity.class);
				editTabIntent.putExtra(Tab.TAB_ID_BUNDLE, getTabHost().getCurrentTabTag());
				startActivityForResult(editTabIntent,EditTabActivity.EDIT_TAB);
				break;
			case R.id.delete_tab_menu_item:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(true);
				if (getTabHost().getTabWidget().getTabCount() > 1) {
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
			case R.id.tools_menu_item:
				Intent toolsIntent = new Intent(this,ToolsActivity.class);
				startActivity(toolsIntent);
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
					Toast.makeText(this, "Button added...", Toast.LENGTH_LONG).show();
					break;
				case EditButtonActivity.EDIT_BUTTON:
					Toast.makeText(this, "Button updated...", Toast.LENGTH_LONG).show();
					break;
				case MoveButtonActivity.MOVE_BUTTON:
					Toast.makeText(this, "Button moved...", Toast.LENGTH_LONG).show();
					break;
				case EditTabActivity.ADD_TAB:
					String newTabId = data.getStringExtra(Tab.TAB_ID_BUNDLE);
					getTabHost().setCurrentTabByTag(newTabId);
					break;
				case PreferencesActivity.EDIT_PREFERENCES:
					Toast.makeText(this, "Preferences unchanged...", Toast.LENGTH_LONG).show();
					break;
			}
			
			// Always load tabs when coming back, there are too many things that might have changed, and the operation is inexpensive
			if (dbAdapter.isDatabaseOpen()) { loadTabs(); }
			
			// If the database isn't open, force a restart to pick up the changes.
			else {
				setResult(RESULT_RESTART_REQUIRED);
				finish();
			}
		}
		else if (resultCode == PreferencesActivity.RESULT_PREFS_CHANGED) {
			Toast.makeText(this, "Preferences changed, relaunching....", Toast.LENGTH_LONG).show();
			setResult(RESULT_RESTART_REQUIRED);
			finish();
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
			Long tabId = Long.parseLong(getTabHost().getCurrentTabTag());
	        DbAdapter dbAdapter = new DbAdapter(mContext, soundReferee);
			dbAdapter.deleteTab(tabId);
			dbAdapter.deleteButtonsByTab(tabId);
			dbAdapter.close();
			loadTabs();
			
			Toast.makeText(mContext, "Tab Deleted", Toast.LENGTH_LONG).show();
		}
	}
	
	private class onCancelTabDeleteListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}

	private class ColoredTabChangeListener implements OnTabChangeListener {
		private Context context;
		
		public ColoredTabChangeListener(Context context) {
			this.context = context;
		}

		@Override
		public void onTabChanged(String tabId) {
			// FIXME: find a way to update the tab header
			DbAdapter dbAdapter = new DbAdapter(context, soundReferee);
			Tab tab = dbAdapter.fetchTabById(tabId);
			setTabBgColor(tab.getBgColor());
			setTabTextColors();
		}
	}
	private void setTabBgColor(String bgColor) {
		int phasedTabColor = Color.BLACK;
		try {
			if (bgColor != null && bgColor.length() > 0 && bgColor.startsWith("#")) {
				int rawTabColor = Color.parseColor(bgColor);
				phasedTabColor = Color.argb(192, Color.red(rawTabColor), Color.green(rawTabColor), Color.blue(rawTabColor));
			}
			setTabBgColor(phasedTabColor);
		} catch (IllegalArgumentException e) {
			// The colors should be checked before they're saved.
			// We need this for illegal colors that have been imported or previously set
			Toast.makeText(this, "Can't set background color to '" + bgColor + "'", Toast.LENGTH_LONG).show();
		}
	}

	private void setTabTextColors() {
		// Get the current tab view
		int currentTab = getTabHost().getCurrentTab();
		TabWidget widget = getTabWidget();
		
		for (int a = 0; a < widget.getTabCount(); a++) {
			View view = getTabWidget().getChildTabViewAt(a);
			
			if (view != null && view instanceof ViewGroup) {
				ViewGroup viewGroup = (ViewGroup) view;
				for (int b=0; b<viewGroup.getChildCount(); b++) {
					View childView = viewGroup.getChildAt(b);
					
					if (childView instanceof TextView) {
						if (a == currentTab) ((TextView) childView).setTextColor(Color.BLACK);
						else ((TextView) childView).setTextColor(Color.WHITE);
					}
				}
			}
		}
	}

	private void setTabBgColor(int tabColor) {
		getTabHost().getTabContentView().setBackgroundColor(tabColor);
	}
	
	@Override
	protected void onDestroy() {
		if (soundReferee != null && soundReferee.getTts() != null) {
			soundReferee.destroyTts();
		}
		super.onDestroy();
	}
}