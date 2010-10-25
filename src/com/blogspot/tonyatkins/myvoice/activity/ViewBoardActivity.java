package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.ButtonTabContentFactory;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.ButtonListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;

public class ViewBoardActivity extends TabActivity {
	SoundReferee soundReferee;
	ButtonListAdapter buttonListAdapter;
	
	private GridView gridView;
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
        
        // FIXME: pull the list of tabs instead
        String[] tabArray = {"default","undefined"};

        for (String tab : tabArray) {
        	TabHost.TabSpec tabSpec = tabHost.newTabSpec(tab);
        	tabSpec.setIndicator(tab);
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
		        startActivityForResult(addButtonIntent,EditButtonActivity.ADD_BUTTON);
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
		if (data != null && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				String returnedButtonData = bundle.getString(SoundButton.BUTTON_BUNDLE);
				if (returnedButtonData != null && returnedButtonData.length() > 0) {
				}
				switch (requestCode) {
					case EditButtonActivity.ADD_BUTTON:
						dbAdapter.createButton(new SoundButton(returnedButtonData));
						buttonListAdapter.refresh();
						gridView.invalidateViews();
						Toast.makeText(this, "Button added...", Toast.LENGTH_LONG).show();
						break;
					case EditButtonActivity.EDIT_BUTTON:
						dbAdapter.updateButton(new SoundButton(returnedButtonData));
						
						buttonListAdapter.refresh();
						gridView.invalidateViews();
						Toast.makeText(this, "Button updated...", Toast.LENGTH_LONG).show();
						break;
				}
			}
		}
		// Stuff that doesn't have to return data
		else {
			switch (requestCode) {
				case PreferencesActivity.EDIT_PREFERENCES:
					soundReferee.setLocale();
					// FIXME: update number of columns in gridView if preference changes
					Toast.makeText(this, "Preferences Updated", Toast.LENGTH_LONG);
					break;
			}
		}
	}
}