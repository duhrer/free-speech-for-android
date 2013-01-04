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
 */
package com.blogspot.tonyatkins.freespeech.db;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

public class EmptyCursor implements Cursor {

	public int getCount() {
		return 0;
	}

	public int getPosition() {
		return 0;
	}

	public boolean move(int offset) {
		return false;
	}

	public boolean moveToPosition(int position) {
		return false;
	}

	public boolean moveToFirst() {
		return false;
	}

	public boolean moveToLast() {
		return false;
	}

	public boolean moveToNext() {
		return false;
	}

	public boolean moveToPrevious() {
		return false;
	}

	public boolean isFirst() {
		return false;
	}

	public boolean isLast() {
		return false;
	}

	public boolean isBeforeFirst() {
		return false;
	}

	public boolean isAfterLast() {
		return false;
	}

	public int getColumnIndex(String columnName) {
		return 0;
	}

	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		return 0;
	}

	public String getColumnName(int columnIndex) {
		return null;
	}

	public String[] getColumnNames() {
		return null;
	}

	public int getColumnCount() {
		return 0;
	}

	public byte[] getBlob(int columnIndex) {
		return null;
	}

	public String getString(int columnIndex) {
		return null;
	}

	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
	}

	public short getShort(int columnIndex) {
		return 0;
	}

	public int getInt(int columnIndex) {
		return 0;
	}

	public long getLong(int columnIndex) {
		return 0;
	}

	public float getFloat(int columnIndex) {
		return 0;
	}

	public double getDouble(int columnIndex) {
		return 0;
	}

	public boolean isNull(int columnIndex) {
		return false;
	}

	public void deactivate() {

	}

	public boolean requery() {
		return false;
	}

	public void close() {
	}

	public boolean isClosed() {
		return false;
	}

	public void registerContentObserver(ContentObserver observer) {
	}

	public void unregisterContentObserver(ContentObserver observer) {
	}

	public void registerDataSetObserver(DataSetObserver observer) {
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

	public void setNotificationUri(ContentResolver cr, Uri uri) {
	}

	public boolean getWantsAllOnMoveCalls() {
		return false;
	}

	public Bundle getExtras() {
		return null;
	}

	public Bundle respond(Bundle extras) {
		return null;
	}
}
