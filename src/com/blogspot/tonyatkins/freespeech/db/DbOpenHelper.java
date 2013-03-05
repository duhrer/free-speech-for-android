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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.BackupUtils;
import com.blogspot.tonyatkins.picker.Constants;

public class DbOpenHelper extends SQLiteOpenHelper {	
	private static final int DATABASE_VERSION = 3;
	private static final String DATABASE_NAME = "freespeech";
	private Context context;
	private DbAdapter dbAdapter;
		
	public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SoundButton.TABLE_CREATE);
		db.execSQL(Tab.TABLE_CREATE);
		
		// Load the demo data from a bundled zip file or die trying.
		try {
			loadData(db, DbAdapter.Data.DEFAULT);
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error reading demo data from zip file", e);
			
			// Make some default data explaining the problem.
			long tabId = createTab("default", null, Tab.NO_RESOURCE, Color.TRANSPARENT, 0, db);
			
			// A single sample button until we can load real sample data.
			createButton("No Data", "Error loading data.  Please use the tools menu to load the data.", null, SoundButton.NO_RESOURCE, null, SoundButton.NO_RESOURCE, tabId, Tab.NO_ID, Color.TRANSPARENT, 0, db);
		}
		
	}

	public void loadData(SQLiteDatabase db, DbAdapter.Data data) throws IOException {
		InputStream in = context.getAssets().open(data.getPath());
		dbAdapter = new DbAdapter(this, db);
		
		Log.d(Constants.TAG, data.getMessage());
		BackupUtils.loadXMLFromZip(context, dbAdapter, in, true);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DbOpenHelper.class.toString(), "Upgrading database from version " + oldVersion + " to " + newVersion + "...");
        if (oldVersion == 1) {
        	Log.d(Constants.TAG, "Upgrading database from version 1");
        	// Rename and move all tab data to the new format, then recreate the table.
			db.execSQL("ALTER TABLE button RENAME TO button_old;");
        	db.execSQL(SoundButton.TABLE_CREATE);
        	db.execSQL("INSERT INTO button (_ID,LABEL,TTS_TEXT,SOUND_PATH,SOUND_RESOURCE,IMAGE_PATH,IMAGE_RESOURCE,TAB_ID,SORT_ORDER) " + 
        			   "select _ID,LABEL,TTS_TEXT,SOUND_PATH,SOUND_RESOURCE,IMAGE_PATH,IMAGE_RESOURCE,TAB_ID,SORT_ORDER from button_old");
 
        	// iterate through and manually convert the background colors
        	
        	Map<Long,Integer> updateButtonValues = new HashMap<Long,Integer>();
        	String[] columns = {"_ID","BACKGROUND_COLOR"};
			Cursor cursor = db.query("button_old", columns , null, null, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				while (cursor.moveToNext()) {
					long id = cursor.getLong(0);
					String oldBgColor = cursor.getString(1);
					int color = Color.TRANSPARENT;
					if (oldBgColor != null) {
						try {
							int tempColor = Color.parseColor(oldBgColor);
							color = tempColor;
						}
						catch (IllegalArgumentException e) {
							Log.w(Constants.TAG, "found invalid color during upgrade, button will now be transparent");
						}
					}
					updateButtonValues.put(id, color);
				}
			}
			for (long id : updateButtonValues.keySet()) {
				int color = updateButtonValues.get(id);
				db.execSQL("UPDATE button SET BACKGROUND_COLOR=" + color + " WHERE _ID=" + id);
			}
			
        	// drop the old table
        	db.execSQL("DROP TABLE button_old");
        	
        	
        	// Rename and move all button data to the new format, then recreate the table.
			db.execSQL("ALTER TABLE tab RENAME TO tab_old;");
			db.execSQL(Tab.TABLE_CREATE);
			db.execSQL("INSERT INTO tab (_ID,LABEL,ICON_FILE,ICON_RESOURCE,SORT_ORDER) " + 
					   "select _ID,LABEL,ICON_FILE,ICON_RESOURCE,SORT_ORDER from tab_old");
			
			// iterate through and manually convert the background colors
			Map<Long,Integer> updateTabValues = new HashMap<Long,Integer>();
			Cursor tabCursor = db.query("tab_old", columns , null, null, null, null, null);
			if (tabCursor.getCount() > 0) {
				tabCursor.moveToFirst();
				while (tabCursor.moveToNext()) {
					long id = tabCursor.getLong(0);
					String oldBgColor = tabCursor.getString(1);

					int color = Color.TRANSPARENT;
					if (oldBgColor != null) {
						try {
							int tempColor = Color.parseColor(oldBgColor);
							color = tempColor;
						}
						catch (IllegalArgumentException e) {
							Log.w(Constants.TAG, "found invalid color during upgrade, button will now be transparent");
						}
					}

					updateTabValues.put(id, color);
				}
			}
			for (long id : updateTabValues.keySet()) {
				int color = updateTabValues.get(id);
				db.execSQL("UPDATE tab SET BACKGROUND_COLOR=" + color + " WHERE _ID=" + id);
			}
			
			// drop the old table
        	db.execSQL("DROP TABLE tab_old");
        }
        
        if (oldVersion <= 2) {
        	Log.d(Constants.TAG, "Upgrading database from version 2");
			db.execSQL("alter table " + SoundButton.TABLE_NAME + " add column " + SoundButton.LINKED_TAB_ID + " long");
        }
	}

	public long createTab(String label, String iconFile, int iconResource, int bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(Tab.LABEL, label);
		values.put(Tab.ICON_FILE, iconFile);
		values.put(Tab.ICON_RESOURCE, iconResource);
		values.put(Tab.BG_COLOR, bgColor);
		values.put(Tab.SORT_ORDER, sortOrder);
		return db.insert(Tab.TABLE_NAME, null, values );
	}

	public boolean updateTab(long id, String label, int bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(Tab.LABEL, label);
		values.put(Tab.BG_COLOR, bgColor);
		values.put(Tab.SORT_ORDER, sortOrder);
		return db.update(Tab.TABLE_NAME, values, Tab._ID + "=" + id, null) > 0;
	}
	
	public long createButton(String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, long linkedTabId, int bgColor, int sortOrder, SQLiteDatabase db) {
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

	public boolean updateButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, long linkedTabId, int bgColor, int sortOrder, SQLiteDatabase db) {
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
}
