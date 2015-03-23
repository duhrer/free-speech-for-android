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
package com.blogspot.tonyatkins.freespeech.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.adapter.ButtonListAdapter;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;

public class ButtonTabContentFactory implements TabContentFactory {
	private final Activity activity;
	private final SoundReferee soundReferee;
	private final TabHost tabHost;
	
	public ButtonTabContentFactory(Activity activity, TabHost tabHost, SoundReferee soundReferee) {
		this.activity = activity;
		this.tabHost = tabHost;
		this.soundReferee = soundReferee;
		
	}

	public View createTabContent(String tag) {
		GridView gridView = new GridView(activity);
		getColumnPrefs(gridView);
		DbAdapter dbAdapter = new DbAdapter(activity);
		Cursor buttonCursor =  SoundButtonDbAdapter.fetchButtonsByTabId(tag,dbAdapter.getDb());
		ButtonListAdapter buttonListAdapter = new ButtonListAdapter(activity, tabHost, soundReferee, buttonCursor, dbAdapter);
        gridView.setAdapter(buttonListAdapter);
		return gridView;
	}

	private void getColumnPrefs(GridView gridView) {
		if (gridView != null) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
			gridView.setNumColumns(Integer.valueOf(preferences.getString(Constants.COLUMNS_PREF, Constants.DEFAULT_COLUMNS)));
		}
	}
}