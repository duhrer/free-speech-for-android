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
package com.blogspot.tonyatkins.freespeech.adapter;


import android.app.Activity;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TabHost;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.listeners.ButtonPlayClickListener;
import com.blogspot.tonyatkins.freespeech.listeners.ConfigurationLongClickListener;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.view.SoundButtonView;

import java.util.Collection;

public abstract class ButtonListAdapter implements ListAdapter {
	private final Activity activity;
	private final SoundReferee soundReferee;
    private Collection<SoundButton> buttons;
	private final TabHost tabHost;
	
	public ButtonListAdapter(Activity activity, TabHost tabHost,SoundReferee mediaPlayerReferee, Collection<SoundButton> buttons) {
		super();
		this.activity = activity;
		this.tabHost = tabHost;
		this.soundReferee = mediaPlayerReferee;
        this.buttons = buttons;
	}

    public ButtonListAdapter(Activity activity, TabHost tabHost,SoundReferee mediaPlayerReferee) {
        super();
        this.activity = activity;
        this.tabHost = tabHost;
        this.soundReferee = mediaPlayerReferee;

        refresh();
    }

    public int getCount() {
        return buttons.size();
	}

	public Object getItem(int position) {
        return buttons.toArray()[position];
    }

	public long getItemId(int position) {
        return ((SoundButton) buttons.toArray()[position]).getId();
    }

	public View getView(int position, View convertView, ViewGroup parent) {
        SoundButton soundButton = (SoundButton) getItem(position);
        LayoutInflater inflater = LayoutInflater.from(activity);
        SoundButtonView view = (SoundButtonView) inflater.inflate(R.layout.view_board_button_layout, parent, false);
        view.setSoundButton(soundButton);

        // Wire in the ButtonPlayClickListener so that the button can be played
        ButtonPlayClickListener playListener = new ButtonPlayClickListener(soundReferee, tabHost);
        view.setOnClickListener(playListener);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean allowEditing = preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true);
        if (allowEditing) {
            // Wire in the ConfigurationLongClickListener so that the button can be configured
            ConfigurationLongClickListener configurationListener = new ConfigurationLongClickListener(activity, soundButton, this, (GridView) parent);
            view.setOnLongClickListener(configurationListener);
        }

        return view;
    }

	public int getItemViewType(int position) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		return false;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

    protected void setButtons(Collection<SoundButton> buttons) {
        this.buttons = buttons;
    }

    public abstract void refresh(SQLiteDatabase db);

    public void refresh() {
        DbOpenHelper helper = new DbOpenHelper(activity);
        SQLiteDatabase db = helper.getReadableDatabase();
        refresh(db);
        db.close();
    }
}
