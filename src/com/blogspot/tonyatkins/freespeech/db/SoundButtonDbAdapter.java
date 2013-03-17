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
package com.blogspot.tonyatkins.freespeech.db;

import java.util.ArrayList;
import java.util.Collection;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.blogspot.tonyatkins.freespeech.model.SoundButton;

public class SoundButtonDbAdapter {
	public static Collection<SoundButton> fetchAllButtons(SQLiteDatabase db) {
		Collection<SoundButton> buttons = new ArrayList<SoundButton>();

		Cursor cursor = fetchAllButtonsAsCursor(db);
		if (cursor.getCount() > 0) {
			cursor.move(-1);
			while(cursor.moveToNext()) {
				SoundButton button = extractButtonFromCursor(cursor);
				buttons.add(button);
			}
		}
		return buttons;
	}
	
	public static Cursor fetchAllButtonsAsCursor(SQLiteDatabase db) {
		if (db.isOpen()) {
			Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , null, null, null, null, SoundButton.TAB_ID + ", " + SoundButton.SORT_ORDER + ", " + SoundButton._ID + " desc");
			return cursor;
		}
		return new EmptyCursor();
	}
	
	public static Cursor fetchButtonsByTab(Long tabId, SQLiteDatabase db) {
		if (db.isOpen()) {
			Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , SoundButton.TAB_ID + "=" + tabId, null, null, null, SoundButton.SORT_ORDER);
			return cursor;
		}
		return new EmptyCursor();
	}
	
	public static SoundButton extractButtonFromCursor(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(SoundButton._ID));
		String imagePath = cursor.getString(cursor.getColumnIndex(SoundButton.IMAGE_PATH));
		int imageResource = cursor.getInt(cursor.getColumnIndex(SoundButton.IMAGE_RESOURCE));
		String label = cursor.getString(cursor.getColumnIndex(SoundButton.LABEL));
		String soundPath = cursor.getString(cursor.getColumnIndex(SoundButton.SOUND_PATH));
		int soundResource = cursor.getInt(cursor.getColumnIndex(SoundButton.SOUND_RESOURCE));
		long tabId = cursor.getLong(cursor.getColumnIndex(SoundButton.TAB_ID));
		long linkedTabId = cursor.getLong(cursor.getColumnIndex(SoundButton.LINKED_TAB_ID));
		String ttsText = cursor.getString(cursor.getColumnIndex(SoundButton.TTS_TEXT));
		int bgColor = cursor.getInt(cursor.getColumnIndex(SoundButton.BG_COLOR));
		int sortOrder = cursor.getInt(cursor.getColumnIndex(SoundButton.SORT_ORDER));
		
		SoundButton soundButton = new SoundButton(id,label,ttsText,soundPath,soundResource,imagePath,imageResource,tabId,linkedTabId,bgColor,sortOrder);
		return soundButton;
	}

	public static void deleteButtonsByTab(Long tabId, SQLiteDatabase db) {
		if (db.isOpen()) {
			Cursor buttonCursor = fetchButtonsByTab(tabId, db);
			while (buttonCursor.moveToNext()) {
				deleteButton(buttonCursor.getLong(buttonCursor.getColumnIndex(SoundButton._ID)), db);
			}
			buttonCursor.close();
		}
	}
	
	public static boolean deleteAllButtons(SQLiteDatabase db) {
		if (db.delete(SoundButton.TABLE_NAME,null, null) >=0) {
			return true;
		}
		return false;
	}
	
	public static boolean deleteButton(long buttonId, SQLiteDatabase db) {
		if (db.isOpen()) {
			if (db.delete(SoundButton.TABLE_NAME, SoundButton._ID + "=" + buttonId, null) >=0) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean deleteButton(SoundButton button, SQLiteDatabase db) {
		return deleteButton(button.getId(), db);
	}

	public static long createButton(SoundButton button, SQLiteDatabase db) {
		return createButton(button.getLabel(),button.getTtsText(),button.getSoundPath(),button.getSoundResource(), button.getImagePath(),button.getImageResource(),button.getTabId(),button.getLinkedTabId(),button.getBgColor(),button.getSortOrder(),db);
	}
	
	public static boolean updateButton(SoundButton button, SQLiteDatabase db) {
		return updateButton(button.getId(), button.getLabel(),button.getTtsText(),button.getSoundPath(),button.getSoundResource(), button.getImagePath(),button.getImageResource(),button.getTabId(),button.getLinkedTabId(),button.getBgColor(),button.getSortOrder(),db);
	}
	
	public static long createButton(String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, long linkedTabId, int bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(SoundButton.LABEL, label);
		values.put(SoundButton.TTS_TEXT, ttsText);
		values.put(SoundButton.SOUND_PATH, soundPath);
		values.put(SoundButton.SOUND_RESOURCE, soundResource);
		values.put(SoundButton.IMAGE_PATH, imagePath);
		values.put(SoundButton.IMAGE_RESOURCE, imageResource);
		values.put(SoundButton.TAB_ID, tabId);
		values.put(SoundButton.LINKED_TAB_ID, linkedTabId);
		values.put(SoundButton.BG_COLOR, bgColor);
		values.put(SoundButton.SORT_ORDER, sortOrder);
		return db.insert(SoundButton.TABLE_NAME, null, values );
	}

	public static boolean updateButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, long linkedTabId, int bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(SoundButton.LABEL, label);
		values.put(SoundButton.TTS_TEXT, ttsText);
		values.put(SoundButton.SOUND_PATH, soundPath);
		values.put(SoundButton.SOUND_RESOURCE, soundResource);
		values.put(SoundButton.IMAGE_PATH, imagePath);
		values.put(SoundButton.IMAGE_RESOURCE, imageResource);
		values.put(SoundButton.TAB_ID, tabId);
		values.put(SoundButton.LINKED_TAB_ID, linkedTabId);
		values.put(SoundButton.BG_COLOR, bgColor);
		values.put(SoundButton.SORT_ORDER, sortOrder);
		return db.update(SoundButton.TABLE_NAME, values, SoundButton._ID + "=" + id, null) > 0;
	}
	
	public static Cursor fetchButtonsByTabId(String id, SQLiteDatabase db) {
		if (id == null) return null;
		
		if (db.isOpen()) {
			Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , SoundButton.TAB_ID + "=" + id, null, null, null, SoundButton.SORT_ORDER);
			return cursor;
		}
		return new EmptyCursor();
	}

	public static SoundButton fetchButtonById(String buttonId, SQLiteDatabase db) {
		if (buttonId == null) return null;
		
		return fetchButtonById(Long.valueOf(buttonId),db);
	}
	
	public static SoundButton fetchButtonById(long buttonId, SQLiteDatabase db) {
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
}
