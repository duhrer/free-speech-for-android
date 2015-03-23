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
package com.blogspot.tonyatkins.freespeech.model;

import java.util.Date;

public class HistoryEntry implements Comparable<HistoryEntry> {
	private static final long NO_ID = -1;
	private long ID = NO_ID;

	public static final String _ID            = "_id";
	public static final String TEXT          = "text";
	public static final String CREATED	  = "created";

	public static final String TABLE_NAME = "history";
	public static final String TABLE_CREATE = 
		"CREATE TABLE " +
		TABLE_NAME + " (" +
		_ID + " integer primary key, " +
		TEXT + " varchar(20), " +
		CREATED + " long " +
		");";

	public static final String[] COLUMNS = {
		_ID,
		TEXT,
		CREATED
	};
	
	private final String ttsText;
	private final Date created;
	
	public HistoryEntry(String ttsText) {
		this.ttsText = ttsText;
		this.created = new Date();
	}
	
	public HistoryEntry(String ttsText, Date created) {
		this.ttsText = ttsText;
		this.created = created;
	}
	public HistoryEntry(long iD, String ttsText, Date created) {
		ID = iD;
		this.ttsText = ttsText;
		this.created = created;
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public String getTtsText() {
		return ttsText;
	}

	public Date getCreated() {
		return created;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (ID ^ (ID >>> 32));
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((ttsText == null) ? 0 : ttsText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistoryEntry other = (HistoryEntry) obj;
		if (ID != other.ID)
			return false;
		if (created == null)
		{
			if (other.created != null)
				return false;
		}
		else if (!created.equals(other.created))
			return false;
		if (ttsText == null)
		{
			if (other.ttsText != null)
				return false;
		}
		else if (!ttsText.equals(other.ttsText))
			return false;
		return true;
	}

	@Override
	public int compareTo(HistoryEntry otherHistoryEntry) {
		if (!otherHistoryEntry.getCreated().equals(created)) {
			return -1 * created.compareTo(otherHistoryEntry.getCreated());
		}
		if (!otherHistoryEntry.getTtsText().equals(ttsText)) {
			return ttsText.compareTo(otherHistoryEntry.getTtsText());
		}
		
		return 0;
	}
}
