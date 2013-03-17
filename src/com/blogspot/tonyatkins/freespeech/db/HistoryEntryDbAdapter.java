package com.blogspot.tonyatkins.freespeech.db;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.blogspot.tonyatkins.freespeech.model.HistoryEntry;

public class HistoryEntryDbAdapter {
	public static HistoryEntry extractHistoryEntryFromCursor(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(HistoryEntry._ID));
		String text = cursor.getString(cursor.getColumnIndex(HistoryEntry.TEXT));
		long createdLong = cursor.getLong(cursor.getColumnIndex(HistoryEntry.CREATED));
		Date created = new Date(createdLong);
		
		return new HistoryEntry(id,text,created);
	}

	public static Set<HistoryEntry> fetchAllHistoryEntries(SQLiteDatabase db) {
		Set<HistoryEntry> historyEntries = new TreeSet<HistoryEntry>();

		Cursor historyEntryCursor = fetchAllHistoryEntriesAsCursor(db);
		if (historyEntryCursor.getCount() > 0) {
			historyEntryCursor.moveToPosition(-1);
			while (historyEntryCursor.moveToNext()) {
				HistoryEntry historyEntry = extractHistoryEntryFromCursor(historyEntryCursor);
				historyEntries.add(historyEntry);
			}
		}
		historyEntryCursor.close();
		return historyEntries;
	}
	
	public static Cursor fetchAllHistoryEntriesAsCursor(SQLiteDatabase db) {
		if (db.isOpen()) {
			Cursor cursor = db.query(HistoryEntry.TABLE_NAME, HistoryEntry.COLUMNS , null, null, null, null, HistoryEntry.CREATED + " desc");
			return cursor;
		}
		return new EmptyCursor();
	}
	
	public static long createHistoryEntry(String ttsText,SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(HistoryEntry.TEXT, ttsText);
		values.put(HistoryEntry.CREATED, new Date().getTime());
		return db.insert(HistoryEntry.TABLE_NAME, null, values );
	}
}
