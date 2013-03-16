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

import org.w3c.dom.Node;

import android.graphics.Color;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.utils.XmlUtils;

public class Tab implements HasId, Comparable<Tab>{
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

	
	private final long id;
	private String label;
	private String iconFile;
	private int iconResource;
	private int bgColor;
	private int sortOrder;
	
	private ArrayList<SoundButton> buttons;

	public Tab(long id, String label) {
		this.id = id;
		this.label = label;
	}
	
	public Tab(long id, String label, String iconFile, int iconResource, int bgColor, int sortOrder) {
		this.id = id;
		this.label = label;
		this.iconFile = iconFile;
		this.iconResource = iconResource;
		this.bgColor = bgColor;
		this.sortOrder = sortOrder;
	}

	public Tab(Node tabNode) {
		Node idNode = XmlUtils.getFirstChildElement(tabNode,_ID);
		this.id = Integer.parseInt(idNode.getNodeValue());

		Node labelNode = XmlUtils.getFirstChildElement(tabNode,LABEL);
		if (labelNode != null) { this.label = labelNode.getNodeValue(); }

		Node iconFileNode = XmlUtils.getFirstChildElement(tabNode,ICON_FILE); 
		if (iconFileNode != null) { 
			if (iconFileNode.getNodeValue().startsWith("/")) {
				this.iconFile = iconFileNode.getNodeValue(); 
			}
			else {
				this.iconFile = Constants.HOME_DIRECTORY + "/" + iconFileNode.getNodeValue();
			}
		}
		
		Node iconResourceNode = XmlUtils.getFirstChildElement(tabNode,ICON_RESOURCE);
		if (iconResourceNode != null) { this.iconResource = Integer.parseInt(iconResourceNode.getNodeValue()); }
		
		Node bgColorNode = XmlUtils.getFirstChildElement(tabNode,BG_COLOR);
		if (bgColorNode != null) { 
			if (bgColorNode.getNodeValue().startsWith("#")) {
				this.bgColor = Color.parseColor(bgColorNode.getNodeValue()); 
			}
			else {
				this.bgColor = Integer.valueOf(bgColorNode.getNodeValue()); 
			}
		}
		
		Node sortOrderElement = XmlUtils.getFirstChildElement(tabNode,SORT_ORDER); 
		if (sortOrderElement == null) {
			this.sortOrder = (int) id;
		}
		else {
			this.sortOrder = Integer.parseInt(sortOrderElement.getNodeValue());
		}
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public long getId() {
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

	@Override
	public int compareTo(Tab otherTab) {
		// Sort order is the only support ordering method.
		if (otherTab.getSortOrder() != getSortOrder()) {
			return getSortOrder() - otherTab.getSortOrder();
		}
		
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bgColor;
		result = prime * result + ((buttons == null) ? 0 : buttons.hashCode());
		result = prime * result + ((iconFile == null) ? 0 : iconFile.hashCode());
		result = prime * result + iconResource;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + sortOrder;
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
		Tab other = (Tab) obj;
		if (bgColor != other.bgColor)
			return false;
		if (buttons == null)
		{
			if (other.buttons != null)
				return false;
		}
		else if (!buttons.equals(other.buttons))
			return false;
		if (iconFile == null)
		{
			if (other.iconFile != null)
				return false;
		}
		else if (!iconFile.equals(other.iconFile))
			return false;
		if (iconResource != other.iconResource)
			return false;
		if (id != other.id)
			return false;
		if (label == null)
		{
			if (other.label != null)
				return false;
		}
		else if (!label.equals(other.label))
			return false;
		if (sortOrder != other.sortOrder)
			return false;
		return true;
	}
}
