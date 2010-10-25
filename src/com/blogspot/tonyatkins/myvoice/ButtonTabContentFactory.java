package com.blogspot.tonyatkins.myvoice;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.GridView;
import android.widget.TabHost.TabContentFactory;

import com.blogspot.tonyatkins.myvoice.activity.PreferencesActivity;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.locale.LocaleBuilder;
import com.blogspot.tonyatkins.myvoice.model.ButtonListAdapter;

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
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		gridView.setNumColumns(Integer.parseInt(preferences.getString("columns", "3")));
		
		// get the database connection and our content
		DbAdapter dbAdapter = new DbAdapter(activity);
		
		// FIXME: Limit by tab instead of showing all buttons on all tabs
		Cursor buttonCursor =  dbAdapter.fetchButtonsByTab(tag);
		
		ButtonListAdapter buttonListAdapter = new ButtonListAdapter(activity, soundReferee, buttonCursor, dbAdapter);
        gridView.setAdapter(buttonListAdapter);

		return gridView;
	}

}
