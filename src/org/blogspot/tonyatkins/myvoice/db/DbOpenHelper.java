package org.blogspot.tonyatkins.myvoice.db;

import org.blogspot.tonyatkins.myvoice.R;
import org.blogspot.tonyatkins.myvoice.model.SoundButton;
import org.blogspot.tonyatkins.myvoice.model.Tab;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper extends SQLiteOpenHelper {	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "picture_board";
		
	public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SoundButton.TABLE_CREATE);
		db.execSQL(Tab.TABLE_CREATE);
		
		// create a default tab
		long tabId = createTab("default", db);

		// create sample buttons in the default tab
		// String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, SQLiteDatabase db
		createButton("Play MP3", "", "", R.raw.mp3sample, "", SoundButton.NO_RESOURCE, tabId, db);
		createButton("Play Wav", "", "", R.raw.wavsample, "", SoundButton.NO_RESOURCE, tabId, db);
		createButton("Play MP3 File", "", "/sdcard/swoosh.mp3", SoundButton.NO_RESOURCE, "", SoundButton.NO_RESOURCE, tabId, db);
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

	public long createTab(String label, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(Tab.LABEL, label);
		return db.insert(Tab.TABLE_NAME, null, values );
	}
	
	public long createButton(String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(SoundButton.LABEL, label);
		values.put(SoundButton.TTS_TEXT, ttsText);
		values.put(SoundButton.SOUND_PATH, soundPath);
		values.put(SoundButton.SOUND_RESOURCE, soundResource);
		values.put(SoundButton.IMAGE_PATH, imagePath);
		values.put(SoundButton.IMAGE_RESOURCE, imageResource);
		values.put(SoundButton.TAB_ID, tabId);
		return db.insert(SoundButton.TABLE_NAME, null, values );
	}

	public boolean updateButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(SoundButton.LABEL, label);
		values.put(SoundButton.TTS_TEXT, ttsText);
		values.put(SoundButton.SOUND_PATH, soundPath);
		values.put(SoundButton.SOUND_RESOURCE, soundResource);
		values.put(SoundButton.IMAGE_PATH, imagePath);
		values.put(SoundButton.IMAGE_RESOURCE, imageResource);
		values.put(SoundButton.TAB_ID, tabId);
		return db.update(SoundButton.TABLE_NAME, values, SoundButton._ID + "=" + id, null) > 0;
	}
}
