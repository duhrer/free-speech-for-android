package com.blogspot.tonyatkins.freespeech.db;

import java.util.Set;
import java.util.TreeSet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class TabDbAdapter {
	public static long createTab(String label, String iconFile, int iconResource, int bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(Tab.LABEL, label);
		values.put(Tab.ICON_FILE, iconFile);
		values.put(Tab.ICON_RESOURCE, iconResource);
		values.put(Tab.BG_COLOR, bgColor);
		values.put(Tab.SORT_ORDER, sortOrder);
		return db.insert(Tab.TABLE_NAME, null, values );
	}
	
	public static long createTab(Tab newTab, SQLiteDatabase db) {
		return createTab(newTab.getLabel(), newTab.getIconFile(), newTab.getIconResource(), newTab.getBgColor(), newTab.getSortOrder(),db);
	}
	
	public static boolean deleteAllTabs(SQLiteDatabase db) {
		if (db.delete(Tab.TABLE_NAME,null, null) >=0) {
			return true;
		}
		return false;
	}

	public static boolean deleteTab(long tabId, SQLiteDatabase db) {
		if (db.delete(Tab.TABLE_NAME, Tab._ID + "=" + tabId, null) >=0) {
			return true;
		}
		return false;
	}
	
	public static boolean deleteTab (Tab tab, SQLiteDatabase db) {
		return deleteTab(tab.getId(), db);
	}

	/**
	 * @param cursor The database cursor to retrieve a tab from.  This method will not manipulate the cursor in any way.  All operations to set the position of the cursor, etc. must be conducted before calling this method.
	 * @return A Tab object based on the current position of the cursor.
	 */
	public static Tab extractTabFromCursor(Cursor cursor) {
		int id = cursor.getInt(cursor.getColumnIndex(Tab._ID));
		String label = cursor.getString(cursor.getColumnIndex(Tab.LABEL));
		String iconFile = cursor.getString(cursor.getColumnIndex(Tab.ICON_FILE));
		int iconResource = cursor.getInt(cursor.getColumnIndex(Tab.ICON_RESOURCE));
		int bgColor = cursor.getInt(cursor.getColumnIndex(Tab.BG_COLOR));
		int sortOrder = cursor.getInt(cursor.getColumnIndex(Tab.SORT_ORDER));

		return new Tab(id, label, iconFile, iconResource, bgColor, sortOrder);
	}
	
	public static Set<Tab> fetchAllTabs(SQLiteDatabase db) {
		Set<Tab> tabs = new TreeSet<Tab>();

		Cursor tabCursor = fetchAllTabsAsCursor(db);
		if (tabCursor.getCount() > 0) {
			tabCursor.moveToPosition(-1);
			while (tabCursor.moveToNext()) {
				Tab tab = extractTabFromCursor(tabCursor);
				tabs.add(tab);
			}
		}
		tabCursor.close();
		
		return tabs;
	}
	
	public static Cursor fetchAllTabsAsCursor(SQLiteDatabase db) {
		if (db.isOpen()) {
			Cursor cursor = db.query(Tab.TABLE_NAME, Tab.COLUMNS , null, null, null, null, Tab.SORT_ORDER);
			return cursor;
		}
		return new EmptyCursor();
	}
	
	public static Tab fetchTabById(String tabId, SQLiteDatabase db) {
		if (tabId == null) return null;
		
		if (db.isOpen()) {
			Cursor cursor = db.query(Tab.TABLE_NAME, Tab.COLUMNS , Tab._ID + "='" + tabId + "'" , null, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				Tab tab = extractTabFromCursor(cursor);
				cursor.close();
				return tab;
			}
			
			cursor.close();
		}
		return null;
	}

	public static String getDefaultTabId(SQLiteDatabase db) {
		if (db.isOpen()) {
			Cursor defaultTabCursor = db.query(Tab.TABLE_NAME, Tab.COLUMNS, null, null, null, null, Tab.SORT_ORDER+","+Tab._ID, "1");
			defaultTabCursor.moveToFirst();
			long id = defaultTabCursor.getLong(defaultTabCursor.getColumnIndex(SoundButton._ID));
			defaultTabCursor.close();
			return String.valueOf(id); 
		}
		
		return null;
	}

	public static boolean updateTab(Tab tab, SQLiteDatabase db) {
		return updateTab(tab.getId(),tab.getLabel(),tab.getBgColor(),tab.getSortOrder(),db);
	}
	
	public static boolean updateTab(long id, String label, int bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(Tab.LABEL, label);
		values.put(Tab.BG_COLOR, bgColor);
		values.put(Tab.SORT_ORDER, sortOrder);
		return db.update(Tab.TABLE_NAME, values, Tab._ID + "=" + id, null) > 0;
	}
}
