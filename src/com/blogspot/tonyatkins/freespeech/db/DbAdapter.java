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
 */
package com.blogspot.tonyatkins.freespeech.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class DbAdapter {
	private DbOpenHelper dbOpenHelper;
	private SQLiteDatabase db;

	public DbAdapter(Context context) throws SQLException {
		super();
		
		dbOpenHelper = new DbOpenHelper(context);
		db=dbOpenHelper.getWritableDatabase();
	}
	
	public DbAdapter(DbOpenHelper dbOpenHelper, SQLiteDatabase db) {
		super();
		
		this.dbOpenHelper = dbOpenHelper;
		this.db=db;
	}
	
	public void close() {
		if (dbOpenHelper != null) dbOpenHelper.close();
		if (db != null) db.close();
	}

	public Collection<SoundButton> fetchAllButtons() {
		Collection<SoundButton> buttons = new ArrayList<SoundButton>();

		Cursor cursor = fetchAllButtonsAsCursor();
		if (cursor.getCount() > 0) {
			cursor.move(-1);
			while(cursor.moveToNext()) {
				SoundButton button = extractButtonFromCursor(cursor);
				buttons.add(button);
			}
		}
		return buttons;
	}
	
	public Cursor fetchAllButtonsAsCursor() {
		if (db.isOpen()) {
			Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , null, null, null, null, SoundButton.SORT_ORDER + ", " + SoundButton._ID + " desc");
			return cursor;
		}
		return new EmptyCursor();
	}
	
	public Cursor fetchButtonsByTab(Long tabId) {
		if (db.isOpen()) {
			Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , SoundButton.TAB_ID + "=" + tabId, null, null, null, SoundButton.SORT_ORDER);
			return cursor;
		}
		return new EmptyCursor();
	}
	
	public boolean updateTab(int id, String label, String bgColor, int sortOrder) {
		return dbOpenHelper.updateTab(id, label, bgColor, sortOrder, db);
	}
	
	public boolean updateTab(Tab tab) {
		return dbOpenHelper.updateTab(tab.getId(), tab.getLabel(), tab.getBgColor(), tab.getSortOrder(), db);
	}
	
	public long createTab(String label, String iconFile, int iconResource, String bgColor, int sortOrder) {
		return dbOpenHelper.createTab(label, iconFile, iconResource, bgColor, sortOrder, db);
	}
	
	public long createButton(String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, String bgColor, int sortOrder) {
		return dbOpenHelper.createButton(label, ttsText, soundPath, soundResource, imagePath, imageResource, tabId, bgColor, sortOrder, db);
	}

	public boolean updateButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, String bgColor, int sortOrder) {
		return dbOpenHelper.updateButton(id, label, ttsText, soundPath, soundResource, imagePath, imageResource, tabId, bgColor, sortOrder, db);
	}
	
	public boolean updateButton(SoundButton existingButton) {
		return updateButton(existingButton.getId(), existingButton.getLabel(), existingButton.getTtsText(), existingButton.getSoundPath(), existingButton.getSoundResource(), existingButton.getImagePath(), existingButton.getImageResource(), existingButton.getTabId(), existingButton.getBgColor(), existingButton.getSortOrder());
	}
	
	public long createButton(SoundButton existingButton) {
		return dbOpenHelper.createButton(existingButton.getLabel(), existingButton.getTtsText(), existingButton.getSoundPath(), existingButton.getSoundResource(), existingButton.getImagePath(), existingButton.getImageResource(), existingButton.getTabId(), existingButton.getBgColor(), existingButton.getSortOrder(), db);
	}

	public boolean deleteAllTabs() {
		if (db.delete(Tab.TABLE_NAME,null, null) >=0) {
			return true;
		}
		return false;
	}
	
	public boolean deleteAllButtons() {
		if (db.delete(SoundButton.TABLE_NAME,null, null) >=0) {
			return true;
		}
		return false;
	}
	
	public boolean deleteTab(long tabId) {
		if (db.delete(Tab.TABLE_NAME, Tab._ID + "=" + tabId, null) >=0) {
			return true;
		}
		return false;
	}
	
	public boolean deleteTab (Tab tab) {
		return deleteTab(tab.getId());
	}
	
	public boolean deleteButton(long buttonId) {
		if (db.isOpen()) {
			if (db.delete(SoundButton.TABLE_NAME, SoundButton._ID + "=" + buttonId, null) >=0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean deleteButton(SoundButton button) {
		return deleteButton(button.getId());
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public Cursor fetchAllTabs() {
		if (db.isOpen()) {
			Cursor cursor = db.query(Tab.TABLE_NAME, Tab.COLUMNS , null, null, null, null, Tab.SORT_ORDER);
			return cursor;
		}
		return new EmptyCursor();
	}

	public Cursor fetchButtonsByTabId(String id) {
		if (id == null) return null;
		
		if (db.isOpen()) {
			Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , SoundButton.TAB_ID + "=" + id, null, null, null, SoundButton.SORT_ORDER);
			return cursor;
		}
		return new EmptyCursor();
	}

	public Tab fetchTabById(String tabId) {
		if (tabId == null) return null;
		
		if (db.isOpen()) {
			Cursor cursor = db.query(Tab.TABLE_NAME, Tab.COLUMNS , Tab._ID + "='" + tabId + "'" , null, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				int id = cursor.getInt(cursor.getColumnIndex(Tab._ID));
				String label = cursor.getString(cursor.getColumnIndex(Tab.LABEL));
				String iconFile = cursor.getString(cursor.getColumnIndex(Tab.ICON_FILE));
				int iconResource = cursor.getInt(cursor.getColumnIndex(Tab.ICON_RESOURCE));
				String bgColor = cursor.getString(cursor.getColumnIndex(Tab.BG_COLOR));
				int sortOrder = cursor.getInt(cursor.getColumnIndex(Tab.SORT_ORDER));
				cursor.close();
				return new Tab(id, label, iconFile, iconResource, bgColor, sortOrder);
			}
			cursor.close();
		}
		return null;
	}

	public long createTab(Tab newTab) {
		return createTab(newTab.getLabel(), newTab.getIconFile(), newTab.getIconResource(), newTab.getBgColor(), newTab.getSortOrder());
	}

	public SoundButton fetchButtonById(String buttonId) {
		if (buttonId == null) return null;
		
		return fetchButtonById(Long.valueOf(buttonId));
	}
	
	public SoundButton fetchButtonById(long buttonId) {
		if (db.isOpen()) {
			Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , SoundButton._ID + "=" + buttonId, null, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				SoundButton soundButton = extractButtonFromCursor(cursor);
				cursor.close();
				return soundButton;
			}
			
			cursor.close();
		}
		
		return null;
	}

	private SoundButton extractButtonFromCursor(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(SoundButton._ID));
		String imagePath = cursor.getString(cursor.getColumnIndex(SoundButton.IMAGE_PATH));
		int imageResource = cursor.getInt(cursor.getColumnIndex(SoundButton.IMAGE_RESOURCE));
		String label = cursor.getString(cursor.getColumnIndex(SoundButton.LABEL));
		String soundPath = cursor.getString(cursor.getColumnIndex(SoundButton.SOUND_PATH));
		int soundResource = cursor.getInt(cursor.getColumnIndex(SoundButton.SOUND_RESOURCE));
		long tabId = cursor.getLong(cursor.getColumnIndex(SoundButton.TAB_ID));
		String ttsText = cursor.getString(cursor.getColumnIndex(SoundButton.TTS_TEXT));
		String bgColor = cursor.getString(cursor.getColumnIndex(SoundButton.BG_COLOR));
		int sortOrder = cursor.getInt(cursor.getColumnIndex(SoundButton.SORT_ORDER));
		
		SoundButton soundButton = new SoundButton(id,label,ttsText,soundPath,soundResource,imagePath,imageResource,tabId,bgColor,sortOrder);
		return soundButton;
	}

	public void deleteButtonsByTab(Long tabId) {
		if (db.isOpen()) {
			Cursor buttonCursor = fetchButtonsByTab(tabId);
			while (buttonCursor.moveToNext()) {
				deleteButton(buttonCursor.getLong(buttonCursor.getColumnIndex(SoundButton._ID)));
			}
			buttonCursor.close();
		}
	}

	public String getDefaultTabId() {
		if (db.isOpen()) {
			Cursor defaultTabCursor = db.query(Tab.TABLE_NAME, Tab.COLUMNS, null, null, null, null, Tab.SORT_ORDER+","+Tab._ID, "1");
			defaultTabCursor.moveToFirst();
			long id = defaultTabCursor.getLong(defaultTabCursor.getColumnIndex(SoundButton._ID));
			defaultTabCursor.close();
			return String.valueOf(id); 
		}
		
		return null;
	}

	public boolean isDatabaseOpen() {
		return db.isOpen();
	}

	public SQLiteDatabase getDb() {
		return db;
	}

	public void loadDemoData() throws IOException {
		dbOpenHelper.loadDemoData(db);
	}
}
