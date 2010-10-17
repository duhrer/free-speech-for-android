package com.blogspot.tonyatkins.myvoice;

import com.blogspot.tonyatkins.myvoice.R;

import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.ButtonListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

public class ViewBoardActivity extends Activity {
	public final static int ADD_ITEM = 0;
	
	SoundReferee soundReferee;
	ButtonListAdapter buttonListAdapter;
	
	private GridView gridView;
	private DbAdapter dbAdapter;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        // load the main grid view from an XML file
        setContentView(R.layout.grid_view);
        
        // get hold of the actual grid view so we can back it with an adapter
        gridView = (GridView) findViewById(R.id.gridview);
        
        // get the database connection and our content
		dbAdapter = new DbAdapter(this);
		Cursor buttonCursor =  dbAdapter.fetchAllButtons();
		
		// Initialize the sound system
		soundReferee = new SoundReferee(this);
		
        buttonListAdapter = new ButtonListAdapter(this, soundReferee, buttonCursor, dbAdapter);
        gridView.setAdapter(buttonListAdapter);

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
		menu.add(0,ADD_ITEM,0,"Add Item");
		
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ITEM:
	        Intent addButtonIntent = new Intent(this, EditButtonActivity.class);
	        startActivityForResult(addButtonIntent,EditButtonActivity.ADD_BUTTON);
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, gridView, null);
		menu.add(0,ADD_ITEM,0,"Add Button");
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ITEM:
			Intent editButtonIntent = new Intent(this,EditButtonActivity.class);
			startActivityForResult(editButtonIntent, EditButtonActivity.ADD_BUTTON);
		}
		
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				String returnedButtonData = bundle.getString(SoundButton.BUTTON_BUNDLE);
				if (returnedButtonData != null && returnedButtonData.length() > 0) {
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
		}
	}

	
}