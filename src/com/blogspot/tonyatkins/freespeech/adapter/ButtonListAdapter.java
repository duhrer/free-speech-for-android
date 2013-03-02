/**
 * Copyright 2012-2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Tony Atkins ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Tony Atkins OR
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
import android.database.Cursor;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.view.SoundButtonView;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.ButtonPlayClickListener;
import com.blogspot.tonyatkins.freespeech.listeners.ConfigurationLongClickListener;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;

public class ButtonListAdapter implements ListAdapter {
	private Activity activity;
	private SoundReferee soundReferee;
	private Cursor mCursor;
	private DbAdapter dbAdapter;
	
	public ButtonListAdapter(Activity activity, SoundReferee mediaPlayerReferee, Cursor cursor, DbAdapter dbAdapter) {
		super();
		this.activity = activity;
		this.soundReferee = mediaPlayerReferee;
		mCursor = cursor;
		this.dbAdapter = dbAdapter;
	}
	

	public int getCount() {
		if (mCursor != null) {
			return mCursor.getCount();
		}
		return 0;
	}

	public Object getItem(int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

	public long getItemId(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
                return mCursor.getLong(0);
        }
        return 0;
    }

	public View getView(int position, View convertView, ViewGroup parent) {
		if (mCursor.moveToPosition(position)) {
			SoundButton soundButton = 
				new SoundButton(
						mCursor.getInt(mCursor.getColumnIndex(SoundButton._ID)),
						mCursor.getString(mCursor.getColumnIndex(SoundButton.LABEL)),
						mCursor.getString(mCursor.getColumnIndex(SoundButton.TTS_TEXT)),
						mCursor.getString(mCursor.getColumnIndex(SoundButton.SOUND_PATH)),
						mCursor.getInt(mCursor.getColumnIndex(SoundButton.SOUND_RESOURCE)),
						mCursor.getString(mCursor.getColumnIndex(SoundButton.IMAGE_PATH)),
						mCursor.getInt(mCursor.getColumnIndex(SoundButton.IMAGE_RESOURCE)),
						mCursor.getLong(mCursor.getColumnIndex(SoundButton.TAB_ID)),
						mCursor.getInt(mCursor.getColumnIndex(SoundButton.BG_COLOR)),
						mCursor.getInt(mCursor.getColumnIndex(SoundButton.SORT_ORDER))
						);
			
			
			LayoutInflater inflater = LayoutInflater.from(activity);
			SoundButtonView view = (SoundButtonView) inflater.inflate(R.layout.view_board_button_layout, parent, false);
			view.setSoundButton(soundButton);
			
			// Wire in the ButtonPlayClickListener so that the button can be played
			ButtonPlayClickListener playListener = new ButtonPlayClickListener(soundReferee);
			view.setOnClickListener(playListener);
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
			boolean allowEditing = preferences.getBoolean(Constants.ALLOW_EDITING_PREF, true);
			if (allowEditing) {
				// Wire in the ConfigurationLongClickListener so that the button can be configured
				ConfigurationLongClickListener configurationListener = new ConfigurationLongClickListener(activity, dbAdapter, soundButton, this, (GridView) parent);
				view.setOnLongClickListener(configurationListener);
			}

			return view;
		}

		return null;
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
	
	public void refresh() {
		mCursor.requery();
	}
}
