package com.blogspot.tonyatkins.myvoice.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;

public class DbOpenHelper extends SQLiteOpenHelper {	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "myvoice";
		
	public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SoundButton.TABLE_CREATE);
		db.execSQL(Tab.TABLE_CREATE);
		
		// FIXME: convert to load the demo data from a bundled zip file.
		
		// create a default tab
		long tabId = createTab("default", null, Tab.NO_RESOURCE, null, 0, db);

		// A single sample button until we can load real sample data.
		createButton("Welcome", "Welcome to My Voice for Android.", null, SoundButton.NO_RESOURCE, null, SoundButton.NO_RESOURCE, tabId, null, 0, db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// FIXME:  Add better handling of Db Upgrades
        Log.w(DbOpenHelper.class.toString(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + SoundButton.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Tab.TABLE_NAME);
        onCreate(db);		
	}

	public long createTab(String label, String iconFile, int iconResource, String bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(Tab.LABEL, label);
		values.put(Tab.ICON_FILE, iconFile);
		values.put(Tab.ICON_RESOURCE, iconResource);
		values.put(Tab.BG_COLOR, bgColor);
		values.put(Tab.SORT_ORDER, sortOrder);
		return db.insert(Tab.TABLE_NAME, null, values );
	}

	public boolean updateTab(int id, String label, String bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(Tab.LABEL, label);
		values.put(Tab.BG_COLOR, bgColor);
		values.put(Tab.SORT_ORDER, sortOrder);
		return db.update(Tab.TABLE_NAME, values, Tab._ID + "=" + id, null) > 0;
	}
	
	public long createButton(String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, String bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(SoundButton.LABEL, label);
		values.put(SoundButton.TTS_TEXT, ttsText);
		values.put(SoundButton.SOUND_PATH, soundPath);
		values.put(SoundButton.SOUND_RESOURCE, soundResource);
		values.put(SoundButton.IMAGE_PATH, imagePath);
		values.put(SoundButton.IMAGE_RESOURCE, imageResource);
		values.put(SoundButton.TAB_ID, tabId);
		values.put(SoundButton.BG_COLOR, bgColor);
		values.put(SoundButton.SORT_ORDER, sortOrder);
		return db.insert(SoundButton.TABLE_NAME, null, values );
	}

	public boolean updateButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, String bgColor, int sortOrder, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(SoundButton.LABEL, label);
		values.put(SoundButton.TTS_TEXT, ttsText);
		values.put(SoundButton.SOUND_PATH, soundPath);
		values.put(SoundButton.SOUND_RESOURCE, soundResource);
		values.put(SoundButton.IMAGE_PATH, imagePath);
		values.put(SoundButton.IMAGE_RESOURCE, imageResource);
		values.put(SoundButton.TAB_ID, tabId);
		values.put(SoundButton.BG_COLOR, bgColor);
		values.put(SoundButton.SORT_ORDER, sortOrder);
		return db.update(SoundButton.TABLE_NAME, values, SoundButton._ID + "=" + id, null) > 0;
	}
}
