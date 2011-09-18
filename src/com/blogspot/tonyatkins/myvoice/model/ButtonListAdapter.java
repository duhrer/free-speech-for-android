/**
 * Copyright 2011 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
 */
package com.blogspot.tonyatkins.myvoice.model;


import android.app.Activity;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

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
	

	@Override
	public int getCount() {
		if (mCursor != null) {
			return mCursor.getCount();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

	@Override
	public long getItemId(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
                return mCursor.getLong(0);
        }
        return 0;
    }

	@Override
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
						mCursor.getString(mCursor.getColumnIndex(SoundButton.BG_COLOR)),
						mCursor.getInt(mCursor.getColumnIndex(SoundButton.SORT_ORDER)),
						soundReferee
						);
			return new SoundButtonView(activity,soundButton,soundReferee, this, dbAdapter);
		}

		return null;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}
	
	public void refresh() {
		mCursor.requery();
	}
}
