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
