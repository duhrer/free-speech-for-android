package com.blogspot.tonyatkins.myvoice.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.GridView;
import android.widget.TabHost.TabContentFactory;

import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;

public class ButtonTabContentFactory implements TabContentFactory {
	private Activity activity;
	private SoundReferee soundReferee;
	
	public ButtonTabContentFactory(Activity activity, SoundReferee soundReferee) {
		super();
		this.activity = activity;
		this.soundReferee = soundReferee;
		
	}


	@Override
	public View createTabContent(String tag) {
		GridView gridView = new GridView(activity);
		getColumnPrefs(gridView);
		DbAdapter dbAdapter = new DbAdapter(activity, new SoundReferee(activity));
		Cursor buttonCursor =  dbAdapter.fetchButtonsByTabId(tag);
		ButtonListAdapter buttonListAdapter = new ButtonListAdapter(activity, soundReferee, buttonCursor, dbAdapter);
        gridView.setAdapter(buttonListAdapter);
		return gridView;
	}

	private void getColumnPrefs(GridView gridView) {
		if (gridView != null) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
			gridView.setNumColumns(Integer.parseInt(preferences.getString("columns", "3")));
		}
	}
}