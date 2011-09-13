/**
 * Copyright (C) 2011 Tony Atkins <duhrer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
