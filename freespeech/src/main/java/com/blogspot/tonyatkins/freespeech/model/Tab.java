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
//	public static final String TAB_LABEL_BUNDLE = "tab_label_bundle";
//	public static final int LABEL_TEXT_TYPE = 5;
//	public static final int BG_COLOR_TEXT_TYPE = 6;

	
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
		int tabId = NO_ID;
		String idNodeValueString = XmlUtils.getNodeValue(idNode);
		if (idNodeValueString != null) {
			tabId = Integer.parseInt(idNodeValueString);
		}
		this.id = tabId;
		
		Node labelNode = XmlUtils.getFirstChildElement(tabNode,LABEL);
		if (labelNode != null) { 
			this.label = XmlUtils.getNodeValue(labelNode); 
		}

		Node iconFileNode = XmlUtils.getFirstChildElement(tabNode,ICON_FILE); 
		String iconFileNodeValue = XmlUtils.getNodeValue(iconFileNode);
		if (iconFileNodeValue != null) {
			if (iconFileNodeValue.startsWith("/")) {
				this.iconFile = iconFileNodeValue; 
			}
			else {
				this.iconFile = "/" + iconFileNodeValue;
			}
		}
		
		Node iconResourceNode = XmlUtils.getFirstChildElement(tabNode,ICON_RESOURCE);
		String iconResourceNodeValue = XmlUtils.getNodeValue(iconResourceNode);
		if (iconResourceNodeValue != null) {
			this.iconResource = Integer.parseInt(iconResourceNodeValue); 
		}
		
		Node bgColorNode = XmlUtils.getFirstChildElement(tabNode,BG_COLOR);
		String bgColorNodeValue = XmlUtils.getNodeValue(bgColorNode);
		if (bgColorNodeValue != null) {
			if (bgColorNodeValue.startsWith("#")) {
				this.bgColor = Color.parseColor(bgColorNodeValue); 
			}
			else {
				this.bgColor = Integer.valueOf(bgColorNodeValue); 
			}
		}
		
		Node sortOrderNode = XmlUtils.getFirstChildElement(tabNode,SORT_ORDER); 
		String sortOrderNodeValue = XmlUtils.getNodeValue(sortOrderNode, String.valueOf(id));
		this.sortOrder = Integer.parseInt(sortOrderNodeValue);
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

//	public void addButton(SoundButton button){
//		buttons.add(button);
//	}
//
//	public void removeButton(SoundButton button) {
//		buttons.remove(button);
//	}

	public String getIconFile() {
		return iconFile;
	}

//	public void setIconFile(String iconFile) {
//		this.iconFile = iconFile;
//	}

	public int getIconResource() {
		return iconResource;
	}

//	public void setIconResource(int iconResource) {
//		this.iconResource = iconResource;
//	}

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

    // Because of the way TreeSet operates, we have to make this support natural sorting by all elements.
    //
    // Elements where compareTo returns 0 are implicitly treated as equal.
	@Override
	public int compareTo(Tab otherTab) {
		if (otherTab.getSortOrder() != getSortOrder()) {
			return getSortOrder() - otherTab.getSortOrder();
		}
        else if (!otherTab.getLabel().equals(getLabel())) {
            return getLabel().compareTo(otherTab.getLabel());
        }
        else if (otherTab.getId() != getId()) {
            return Math.round(getId() - otherTab.getId());
        }
        // These should never be reached, as the ID should always be unique, but we will leave them for corner cases like new buttons.
        else if (otherTab.getBgColor() != getBgColor()) {
            return getBgColor()- otherTab.getBgColor();
        }
        else if (otherTab.getIconResource() != getIconResource()) {
            return getIconResource()- otherTab.getIconResource();
        }
        else if (otherTab.getIconFile() != getIconFile()) {
            return getIconFile().compareTo(otherTab.getIconFile());
        }

        // If you manage to make two tabs with everything the same, we're down to comparing buttons
        if (buttons != otherTab.buttons) {
            for (int a=0; a<buttons.size(); a++) {
                SoundButton button = buttons.get(a);
                SoundButton otherButton = otherTab.buttons.size() >= a+1 ? otherTab.buttons.get(a) : null;

                if (button != otherButton) {
                    return button.compareTo(otherButton);
                }
            }
        }

		return 0;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tab tab = (Tab) o;

        if (bgColor != tab.bgColor) return false;
        if (iconResource != tab.iconResource) return false;
        if (id != tab.id) return false;
        if (sortOrder != tab.sortOrder) return false;
        if (!buttons.equals(tab.buttons)) return false;
        if (!iconFile.equals(tab.iconFile)) return false;
        if (!label.equals(tab.label)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + label.hashCode();
        result = 31 * result + iconFile.hashCode();
        result = 31 * result + iconResource;
        result = 31 * result + bgColor;
        result = 31 * result + sortOrder;
        result = 31 * result + buttons.hashCode();
        return result;
    }
}
