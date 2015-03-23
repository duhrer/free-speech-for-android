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
package com.blogspot.tonyatkins.freespeech.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import com.blogspot.tonyatkins.freespeech.model.HistoryEntry;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.utils.BackupUtils;
import com.blogspot.tonyatkins.picker.Constants;

public class DbOpenHelper extends SQLiteOpenHelper {	
	private static final int DATABASE_VERSION = 6;
	private static final String DATABASE_NAME = "freespeech";
	private Context context;

	public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SoundButton.TABLE_CREATE);
		db.execSQL(Tab.TABLE_CREATE);
        db.execSQL(HistoryEntry.TABLE_CREATE);

        // Load the demo data from a bundled zip file or die trying.
		try {
			loadData(db, DbAdapter.Data.DEFAULT);
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error reading demo data from zip file", e);
			
			// Make some default data explaining the problem.
			long tabId = TabDbAdapter.createTab("default", null, Tab.NO_RESOURCE, Color.TRANSPARENT, 0, db);
			
			// A single sample button until we can load real sample data.
			SoundButtonDbAdapter.createButton("No Data", "Error loading data.  Please use the tools menu to load the data.", null, SoundButton.NO_RESOURCE, null, SoundButton.NO_RESOURCE, tabId, Tab.NO_ID, Color.TRANSPARENT, 0, db);
		}
		
	}

	public void loadData(SQLiteDatabase db, DbAdapter.Data data) throws IOException {
		InputStream in = context.getAssets().open(data.getPath());
		Log.d(Constants.TAG, data.getMessage());
		BackupUtils.loadXMLFromZip(context, db, in, true);
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
        
        if (oldVersion == 2) {
        	Log.d(Constants.TAG, "Upgrading database to version 2");

			db.execSQL("alter table " + SoundButton.TABLE_NAME + " add column " + SoundButton.LINKED_TAB_ID + " long");
        }
        
        // There was a bug where the history table for the keyboard wasn't created for new users.
        // We may or may not have that table depending on their upgrade route before version 6.
        if (oldVersion < 6) {
        	Log.d(Constants.TAG, "Upgrading database to version 6");
            if (!tableExists(db,HistoryEntry.TABLE_NAME)) {
                db.execSQL(HistoryEntry.TABLE_CREATE);
            }
        }
	}

    private static boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return false;
            }
            cursor.close();
            return true;
        }
        return false;
    }
}
