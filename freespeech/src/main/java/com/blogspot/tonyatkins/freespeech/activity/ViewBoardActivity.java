/**
 * Copyright 2012-2015 Upright Software <info@uprightsoftware.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Upright Software ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Upright Software OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.blogspot.tonyatkins.freespeech.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ActivityLaunchListener;
import com.blogspot.tonyatkins.freespeech.model.ButtonTabContentFactory;
import com.blogspot.tonyatkins.freespeech.model.Tab;

import java.util.Collection;

public class ViewBoardActivity extends FreeSpeechTabActivity {
	private static final String ADD_TAB_MENU_ITEM_TITLE = "Add Tab";
	private static final String EDIT_TAB_MENU_ITEM_TITLE = "Edit Tab";
	private static final String DELETE_TAB_MENU_ITEM_TITLE = "Delete Tab";
	final String[] configurationDialogOptions = { ADD_TAB_MENU_ITEM_TITLE, EDIT_TAB_MENU_ITEM_TITLE, DELETE_TAB_MENU_ITEM_TITLE, "Cancel" };
	
	public static final int RESULT_RESTART_REQUIRED = 8579;
	private SoundReferee soundReferee;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        BoardPreferenceChangeListener boardPreferenceChangeListener = new BoardPreferenceChangeListener();
		preferences.registerOnSharedPreferenceChangeListener(boardPreferenceChangeListener);
    	
        setContentView(R.layout.view_board);
        
        // Initialize the sound system
        soundReferee = new SoundReferee(this);
        
        loadTabs();
        
        // Wire up the volume controls so that they control the media volume for as long as we're active
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        View mainView = findViewById(android.R.id.tabcontent);
		boolean allowEditing = preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true);
		if (allowEditing) {
			mainView.setOnClickListener(new LaunchAddDialogListener(this));
		}
		
        // TODO:  Add a "Delete Buttons" dialog to the global config dialog/menu
        // TODO: Add a back-button handler to avoid accidental exits
        // TODO: Add a home button handler to avoid accidental exits
        // TODO: Add a phone button handler to avoid accidental exits
        // TODO: Add a power button handler to avoid accidental exits
    }
    
    private class LaunchAddDialogListener implements View.OnClickListener {
    	private final Activity activity;
    	
		public LaunchAddDialogListener(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onClick(View v) {
			LayoutInflater inflater = getLayoutInflater();
			View dialogLayout = inflater.inflate(R.layout.view_board_add_menu, (ViewGroup) findViewById(android.R.id.tabcontent),false);

			View addButtonRow = dialogLayout.findViewById(R.id.view_board_add_menu_add_button);
			Intent addButtonIntent = new Intent(activity,EditButtonActivity.class);
			addButtonIntent.putExtra(Tab.TAB_ID_BUNDLE, getTabHost().getCurrentTabTag());
			addButtonRow.setOnClickListener(new ActivityLaunchListener(activity, EditButtonActivity.ADD_BUTTON, addButtonIntent));

			View addTabRow = dialogLayout.findViewById(R.id.view_board_add_menu_add_tab);
			Intent addTabIntent = new Intent(activity,EditTabActivity.class);
			addTabIntent.putExtra(Tab.TAB_ID_BUNDLE, getTabHost().getCurrentTabTag());
			addTabRow.setOnClickListener(new ActivityLaunchListener(activity, EditTabActivity.EDIT_TAB, addTabIntent));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setView(dialogLayout);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
    }
    
	@Override
	public void finish() {
		soundReferee.destroyTts();
		super.finish();
	}
    
	public void loadTabs() {
		TabHost tabHost = getTabHost();
		String currentTag = tabHost.getCurrentTabTag();

        DbOpenHelper helper = new DbOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
		if (currentTag == null) {
			currentTag = TabDbAdapter.getDefaultTabId(db);
		}
		
		// We have to work around a bug by resetting the tab to 0 when we reload the content
		tabHost.setCurrentTab(0);

		// We're reloading the tabs, so we have to get rid of our current content.
		tabHost.clearAllTabs();

        Collection<Tab> tabs = TabDbAdapter.fetchAllTabs(db);

		int contentViewColor = Color.BLACK;

        for (Tab tab : tabs) {
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(tab.getId()));

            String label = tab.getLabel();
            int labelResource = getResources().getIdentifier("com.blogspot.tonyatkins.freespeech:string/" + label, null, null);
            if (labelResource == 0) {
                tabSpec.setIndicator(label);
            } else {
                tabSpec.setIndicator(getResources().getString(labelResource));
            }

            tabSpec.setContent(new ButtonTabContentFactory(this, tabHost, soundReferee));
            tabHost.addTab(tabSpec);

            if (currentTag != null && tab.getId() == Integer.parseInt(currentTag)) {
                contentViewColor = tab.getBgColor();
            }
        }

		setTabBgColor(contentViewColor);
        db.close();

        // Add long-click handling of "tab" actions (add, edit, delete)
        // TabHost doesn't expose the list of tags directly, so we have to cycle through the list of tabs to get their tags.
        for (int a = getTabWidget().getTabCount() - 1; a >=0; a--) {
        	getTabHost().setCurrentTab(a);
        	View tab = getTabWidget().getChildTabViewAt(a);
        	getTabHost().setCurrentTab(a);
        	
			boolean allowEditing = preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true);
			if (allowEditing) {
				tab.setOnLongClickListener(new TabMenuListener(getTabHost().getCurrentTabTag()));
			}
        }

        // Now reset the current tab to the previous value
        tabHost.setCurrentTabByTag(currentTag);

        // Add a listener to rework the colors when the tabs are changed
        tabHost.setOnTabChangedListener(new ColoredTabChangeListener());
		
		// Hide the tab bar if we only have one tab or if the controls are hidden by our preferences
        View tabWidget = getTabWidget();
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean hideTabControls = preferences.getBoolean(Constants.HIDE_TAB_CONTROLS_PREF, false);
		if (hideTabControls || getTabWidget().getTabCount() < 2) tabWidget.setVisibility(View.GONE);
		else tabWidget.setVisibility(View.VISIBLE);
		
		// We have to do this the first time, from now on it will happen whenever the tab changes
		setTabTextColors();
		
		setTabBgColor(contentViewColor);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		boolean allowEditing = preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true);
		if (allowEditing) {
			inflater.inflate(R.menu.view_board_menu, menu);
		}
		else {
			inflater.inflate(R.menu.view_board_menu_no_editing, menu);
		}
		
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
			case R.id.delete_tab_menu_item:
				Long tabId = Long.parseLong(getTabHost().getCurrentTabTag());
				deleteTab(tabId);
				break;
			case R.id.edit_tab_menu_item:
				Intent editTabIntent = new Intent(this, EditTabActivity.class);
				editTabIntent.putExtra(Tab.TAB_ID_BUNDLE, getTabHost().getCurrentTabTag());
				startActivityForResult(editTabIntent,EditTabActivity.EDIT_TAB);
				break;
			case R.id.keyboard_menu_item:
				Intent keyboardIntent = new Intent(this, KeyboardActivity.class);
				startActivityForResult(keyboardIntent,KeyboardActivity.REQUEST_CODE);
				break;
			case R.id.sort_buttons_menu_item:
				Intent sortButtonsIntent = new Intent(this, SortButtonsActivity.class);
				sortButtonsIntent.putExtra(Tab.TAB_ID_BUNDLE, getTabHost().getCurrentTabTag());
				startActivityForResult(sortButtonsIntent,SortButtonsActivity.REQUEST_CODE);
				break;
			case R.id.sort_tabs_menu_item:
				Intent sortTabsIntent = new Intent(this, SortTabsActivity.class);
				startActivityForResult(sortTabsIntent,SortTabsActivity.REQUEST_CODE);
				break;
			case R.id.preferences_menu_item:
				Intent preferencesIntent = new Intent(this,PreferencesActivity.class);
				startActivityForResult(preferencesIntent,PreferencesActivity.EDIT_PREFERENCES);
				break;
			case R.id.tools_menu_item:
				Intent toolsIntent = new Intent(this,ToolsActivity.class);
				startActivityForResult(toolsIntent,ToolsActivity.TOOLS_REQUEST);
				break;
			case R.id.quit_menu_item:
				finish();
				break;
			case R.id.feedback_menu_item:
				Intent feedbackIntent = new Intent(this, FeedbackActivity.class);
				startActivity(feedbackIntent);
			  break;
			case R.id.about_menu_item:
				Intent aboutIntent = new Intent(this, AboutActivity.class);
				startActivity(aboutIntent);
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void deleteTab(Long tabId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		if (getTabHost().getTabWidget().getTabCount() > 1) {
			builder.setTitle("Delete Tab?");
			builder.setMessage("Are you sure you want to delete this tab and all its buttons?");
			builder.setPositiveButton("Yes", new onConfirmTabDeleteListener(tabId));
			builder.setNegativeButton("No", new onCancelTabDeleteListener());
		}
		else {
			builder.setTitle("Can't Delete Tab");
			builder.setMessage("Can't delete this tab.  There must always be at least one tab.");
			builder.setPositiveButton("OK", new onCancelTabDeleteListener());
		}
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
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
			loadTabs();
        }
        // If our preferences have changed, we need to restart
        // TODO:  Tailor this to be a bit less broad-spectrum
        else if (resultCode == PreferencesActivity.RESULT_PREFS_CHANGED || resultCode == ToolsActivity.TOOLS_DATA_CHANGED) {
			setResult(RESULT_RESTART_REQUIRED);
			finish();
		}
	}

	private class onConfirmTabDeleteListener implements DialogInterface.OnClickListener {
		private Long tabId;
		
		public onConfirmTabDeleteListener(Long tabId) {
			super();
			this.tabId = tabId;
		}

		public void onClick(DialogInterface dialog, int which) {
            DbOpenHelper helper = new DbOpenHelper(ViewBoardActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();
			SoundButtonDbAdapter.deleteButtonsByTab(tabId, db);
			TabDbAdapter.deleteTab(tabId, db);
            db.close();

            loadTabs();
			
			Toast.makeText(ViewBoardActivity.this, "Tab Deleted", Toast.LENGTH_LONG).show();
		}
	}
	
	private class onCancelTabDeleteListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}

	private class ColoredTabChangeListener implements OnTabChangeListener {
		public void onTabChanged(String tabId) {
            DbOpenHelper helper = new DbOpenHelper(ViewBoardActivity.this);
            SQLiteDatabase db = helper.getReadableDatabase();

			Tab tab = TabDbAdapter.fetchTabById(tabId, db);
            db.close();

			// If the tab has been deleted already, it'll be null and we should ignore it
			if (tab != null) {
				setTabBgColor(tab.getBgColor());
				setTabTextColors();
			}
		}
	}

//	private void setTabBgColor(String bgColor) {
//		int phasedTabColor = Color.BLACK;
//		try {
//			if (bgColor != null && bgColor.length() > 0 && bgColor.startsWith("#")) {
//				int rawTabColor = Color.parseColor(bgColor);
//				phasedTabColor = Color.argb(192, Color.red(rawTabColor), Color.green(rawTabColor), Color.blue(rawTabColor));
//			}
//			setTabBgColor(phasedTabColor);
//		} catch (IllegalArgumentException e) {
//			// The colors should be checked before they're saved.
//			// We need this for illegal colors that have been imported or previously set
//			Toast.makeText(this, "Can't set background color to '" + bgColor + "'", Toast.LENGTH_LONG).show();
//		}
//	}

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
	
	private class TabMenuListener implements OnLongClickListener {
		private Object tag;
		
		public TabMenuListener(Object tag) {
			this.tag = tag;
		}

		public boolean onLongClick(View v) {
			AlertDialog.Builder configurationDialogBuilder = new AlertDialog.Builder(ViewBoardActivity.this);
			configurationDialogBuilder.setTitle("Tab Menu");
			configurationDialogBuilder.setItems(configurationDialogOptions, new TabConfigurationDialogOnClickListener(tag));
			configurationDialogBuilder.setCancelable(true);
			AlertDialog configureDialog = configurationDialogBuilder.create();
			configureDialog.show();
			
			return true;
		}
		
	}
	
	private class TabConfigurationDialogOnClickListener implements OnClickListener {
		private Object tag;
		
		public TabConfigurationDialogOnClickListener(Object tag) {
			this.tag = tag;
		}

		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			
			String selectedOption = "";
			if (configurationDialogOptions.length > which) {
				selectedOption = configurationDialogOptions[which];
			}

			if (selectedOption.equals(ADD_TAB_MENU_ITEM_TITLE)) {
				Intent addTabIntent = new Intent(ViewBoardActivity.this, EditTabActivity.class);
				startActivityForResult(addTabIntent,EditTabActivity.ADD_TAB);
			}
			else if (selectedOption.equals(EDIT_TAB_MENU_ITEM_TITLE)) {
				Intent editTabIntent = new Intent(ViewBoardActivity.this, EditTabActivity.class);
				editTabIntent.putExtra(Tab.TAB_ID_BUNDLE, tag.toString());
				startActivityForResult(editTabIntent,EditTabActivity.EDIT_TAB);
			}
			else if (selectedOption.equals(DELETE_TAB_MENU_ITEM_TITLE)) {
				Long tabId = Long.parseLong(tag.toString());
				deleteTab(tabId);
			}
		}
		
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (getTabWidget().getTabCount() < 2) {
			if (menu != null) {
				for (int a=0; a< menu.size(); a++) { menu.getItem(a).setVisible(true); }
			}
		}
		else {
			// Hide the menu options to edit and delete tabs to save space
			if (menu != null) {
				for (int a=0; a< menu.size(); a++) {
					MenuItem item = menu.getItem(a);
					if (((String)item.getTitle()).toLowerCase().contains("tab")) item.setVisible(false);
				}
			}
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	private class BoardPreferenceChangeListener implements  OnSharedPreferenceChangeListener {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			setResult(RESULT_RESTART_REQUIRED);
			finish();
		}
	}
}