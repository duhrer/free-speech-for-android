package com.blogspot.tonyatkins.myvoice.model;

import java.util.ArrayList;

import nu.xom.Element;

public class Tab {
	public final static int NO_RESOURCE = -1;

	public static final String _ID            = "id";
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
		BG_COLOR + " varchar(255), " +
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
	private String bgColor;
	private int sortOrder;
	
	private ArrayList<SoundButton> buttons;

	public Tab(int id, String label) {
		this.id = id;
		this.label = label;
	}
	
	public Tab(int id, String label, String iconFile, int iconResource, String bgColor, int sortOrder) {
		this.id = id;
		this.label = label;
		this.iconFile = iconFile;
		this.iconResource = iconResource;
		this.bgColor = bgColor;
		this.sortOrder = sortOrder;
	}

	public Tab(Element element) {
		this.id = Integer.parseInt(element.getFirstChildElement(_ID).toString());
		this.label = element.getFirstChildElement(LABEL).toString();
		this.iconFile = element.getFirstChildElement(ICON_FILE).toString();
		this.iconResource = Integer.parseInt(element.getFirstChildElement(ICON_RESOURCE).toString());
		this.bgColor = element.getFirstChildElement(BG_COLOR).toString();
		this.sortOrder = Integer.parseInt(element.getFirstChildElement(SORT_ORDER).toString());
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

	public String getBgColor() {
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
