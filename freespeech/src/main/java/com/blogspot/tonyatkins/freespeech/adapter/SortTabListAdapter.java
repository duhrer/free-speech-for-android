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


import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.listeners.DragLongClickListener;
import com.blogspot.tonyatkins.freespeech.listeners.TabListDragListener;
import com.blogspot.tonyatkins.freespeech.model.Tab;

import java.util.Collection;

public abstract class SortTabListAdapter implements ListAdapter {
	private final Activity activity;
    private Collection<Tab> tabs;

	public SortTabListAdapter(Activity activity, Collection<Tab> tabs) {
		super();
		this.activity = activity;
        this.tabs = tabs;
	}

    public SortTabListAdapter(Activity activity) {
        super();
        this.activity = activity;

        refresh();
    }

	public int getCount() {
        return tabs.size();
	}

	public Object getItem(int position) {
        return tabs.toArray()[position];
    }

	public long getItemId(int position) {
        return ((Tab) getItem(position)).getId();
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public View getView(int position, View convertView, ViewGroup parent) {
        Tab tab = (Tab) getItem(position);

        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.sort_tabs_tab_layout, parent, false);
        Button button = (Button) view.findViewById(R.id.sortTabsTabButton);

        String labelString = tab.getLabel();
        int labelResource = activity.getResources().getIdentifier("com.blogspot.tonyatkins.freespeech:string/" + labelString, null, null);
        if (labelResource == 0) {
            button.setText(labelString);
        }
        else {
            button.setText(labelResource);
        }

        // Wire in the long click listener that will start the long drag.
        button.setOnLongClickListener(new DragLongClickListener(tab));

        // Wire in the drag listener.
        button.setOnDragListener(new TabListDragListener(tab, activity, (ListView) parent));

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

    public abstract void refresh(SQLiteDatabase db);

    public void refresh() {
        DbOpenHelper helper = new DbOpenHelper(activity);
        SQLiteDatabase db = helper.getReadableDatabase();
        refresh(db);
        db.close();
    }

    protected void setTabs(Collection<Tab> tabs) {
        this.tabs = tabs;
    }
}
