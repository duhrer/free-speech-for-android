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
package com.blogspot.tonyatkins.freespeech.model;

import java.util.ArrayList;

import android.graphics.Color;

import com.blogspot.tonyatkins.freespeech.Constants;

import nu.xom.Element;

public class Tab {
	public final static int NO_RESOURCE = -1;
	public final static int NO_ID = 0;

	public static final String _ID            = "_id";
	public static final String LABEL          = "label";
	public static final String ICON_FILE	  = "icon_file";
	public static final String ICON_RESOURCE  = "icon_resource";
	public static final String BG_COLOR  	  = "background_color";
	public static final String SORT_ORDER	  = "sort_order";

	public static final String TABLE_NAME = "tab";
	public static final String TABLE_CREATE = 
		"CREATE TABLE " +
		TABLE_NAME + " (" +
		_ID + " integer primary key, " +
		LABEL + " varchar(20), " +
		ICON_FILE + " text, " +
		ICON_RESOURCE + " integer, " +
		BG_COLOR + " integer, " +
		SORT_ORDER + " integer" +
		");";

	public static final String[] COLUMNS = {
		_ID,
		LABEL,
		ICON_FILE,
		ICON_RESOURCE,
		BG_COLOR,
		SORT_ORDER
	};
	public static final String TAB_ID_BUNDLE = "tab_id_bundle";
	public static final String TAB_LABEL_BUNDLE = "tab_label_bundle";
	public static final int LABEL_TEXT_TYPE = 5;
	public static final int BG_COLOR_TEXT_TYPE = 6;

	
	private final int id;
	private String label;
	private String iconFile;
	private int iconResource;
	private int bgColor;
	private int sortOrder;
	
	private ArrayList<SoundButton> buttons;

	public Tab(int id, String label) {
		this.id = id;
		this.label = label;
	}
	
	public Tab(int id, String label, String iconFile, int iconResource, int bgColor, int sortOrder) {
		this.id = id;
		this.label = label;
		this.iconFile = iconFile;
		this.iconResource = iconResource;
		this.bgColor = bgColor;
		this.sortOrder = sortOrder;
	}

	public Tab(Element element) {
		Element idElement = element.getFirstChildElement(_ID);
		this.id = Integer.parseInt(idElement.getValue());

		Element labelElement = element.getFirstChildElement(LABEL);
		if (labelElement != null) { this.label = labelElement.getValue(); }

		Element iconFileElement = element.getFirstChildElement(ICON_FILE); 
		if (iconFileElement != null) { 
			if (iconFileElement.getValue().startsWith("/")) {
				this.iconFile = iconFileElement.getValue(); 
			}
			else {
				this.iconFile = Constants.HOME_DIRECTORY + "/" + iconFileElement.getValue();
			}
		}
		
		Element iconResourceElement = element.getFirstChildElement(ICON_RESOURCE);
		if (iconResourceElement != null) { this.iconResource = Integer.parseInt(iconResourceElement.getValue()); }
		
		Element bgColorElement = element.getFirstChildElement(BG_COLOR);
		if (bgColorElement != null) { 
			if (bgColorElement.getValue().startsWith("#")) {
				this.bgColor = Color.parseColor(bgColorElement.getValue()); 
			}
			else {
				this.bgColor = Integer.valueOf(bgColorElement.getValue()); 
			}
		}
		
		Element sortOrderElement = element.getFirstChildElement(SORT_ORDER); 
		if (sortOrderElement == null) {
			// TODO: When sort order is implemented, this handling will need to be improved.
			this.sortOrder = (int) id;
		}
		else {
			this.sortOrder = Integer.parseInt(sortOrderElement.getValue());
		}
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public void addButton(SoundButton button){
		buttons.add(button);
	}
	
	public void removeButton(SoundButton button) {
		buttons.remove(button);
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public int getIconResource() {
		return iconResource;
	}

	public void setIconResource(int iconResource) {
		this.iconResource = iconResource;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
