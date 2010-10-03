package com.blogspot.tonyatkins.myvoice.model;


import com.blogspot.tonyatkins.myvoice.controller.MediaPlayerReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class ButtonListAdapter implements ListAdapter {
	private Context mContext;
	private MediaPlayerReferee mediaPlayerReferee;
	private Cursor mCursor;
	private DbAdapter dbAdapter;
	
	public ButtonListAdapter(Context context, MediaPlayerReferee mediaPlayerReferee, Cursor cursor, DbAdapter dbAdapter) {
		super();
		mContext = context;
		this.mediaPlayerReferee = mediaPlayerReferee;
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
						mCursor.getInt(0),
						mCursor.getString(1),
						mCursor.getString(2),
						mCursor.getString(3),
						mCursor.getInt(4),
						mCursor.getString(5),
						mCursor.getInt(6),
						mCursor.getLong(7)
						);
			return new SoundButtonView(mContext,soundButton,mediaPlayerReferee, this, dbAdapter);
		}

		return null;
		// TODO: Come up with a simple method of comparing this view to our button and using the cache where appropriate
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
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
