package com.blogspot.tonyatkins.myvoice.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;

public class DbAdapter {
	private Context mContext;
	private DbOpenHelper dbOpenHelper;
	private SQLiteDatabase db;

	public DbAdapter(Context mContext) throws SQLException {
		super();
		this.mContext = mContext;

		dbOpenHelper = new DbOpenHelper(mContext);
		db=dbOpenHelper.getWritableDatabase();
	}
	
	public void close() {
		dbOpenHelper.close();
	}

	public Cursor fetchAllButtons() {
		Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , null, null, null, null, null);
        return cursor;
	}
	
	public Cursor fetchButtonsByTab(int tabId) {
		Cursor cursor = db.query(SoundButton.TABLE_NAME, SoundButton.COLUMNS , SoundButton.TAB_ID + "=" + tabId, null, null, null, null);
		return cursor;
	}
	
	public long createTab(String label) {
		return dbOpenHelper.createTab(label, db);
	}
	
	public long createButton(String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId) {
		return dbOpenHelper.createButton(label, ttsText, soundPath, soundResource, imagePath, imageResource, tabId, db);
	}

	public boolean updateButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId) {
		return dbOpenHelper.updateButton(id, label, ttsText, soundPath, soundResource, imagePath, imageResource, tabId, db);
	}
	
	public boolean updateButton(SoundButton existingButton) {
		return updateButton(existingButton.getId(), existingButton.getLabel(), existingButton.getTtsText(), existingButton.getSoundPath(), existingButton.getSoundResource(), existingButton.getImagePath(), existingButton.getImageResource(), existingButton.getTabId());
	}
	
	public long createButton(SoundButton existingButton) {
		return dbOpenHelper.createButton(existingButton.getLabel(), existingButton.getTtsText(), existingButton.getSoundPath(), existingButton.getSoundResource(), existingButton.getImagePath(), existingButton.getImageResource(), existingButton.getTabId(), db);
	}

	public boolean deleteTab(long tabId) {
		if (db.delete(Tab.TABLE_NAME, Tab._ID + "=" + tabId, null) >=0) {
			return true;
		}
		return false;
	}
	
	public boolean deleteButton(long buttonId) {
		if (db.delete(SoundButton.TABLE_NAME, SoundButton._ID + "=" + buttonId, null) >=0) {
			return true;
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
		Cursor cursor = db.query(Tab.TABLE_NAME, Tab.COLUMNS , null, null, null, null, null);
        return cursor;
	}
}
