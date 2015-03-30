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

import android.app.Activity;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ImportExportDbAdapter {
	public enum Data {
		DEMO("demo","data/demo.zip","Loading demo data from demo.zip file."),
		DEFAULT("default","data/default.zip","Loading default data from demo.zip file.");
		
		private final String name;
		private final String path;
		private final String message;

		private Data(String name, String path, String message) {
			this.name = name;
			this.path = path;
			this.message = message;
		}

		public String getName() {
			return name;
		}

		public String getPath() {
			return path;
		}

		public String getMessage() {
			return message;
		}
	}
	
	private DbOpenHelper dbOpenHelper;

	public ImportExportDbAdapter(Activity activity) throws SQLException {
		super();
		
		dbOpenHelper = new DbOpenHelper(activity);
	}

	public void close() {
		if (dbOpenHelper != null) dbOpenHelper.close();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void loadDemoData() throws IOException {
		loadDemoData(ImportExportDbAdapter.Data.DEFAULT);
	}
	
	public void loadDemoData(ImportExportDbAdapter.Data data) throws IOException {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.loadData(db, data);
        db.close();
	}
}
