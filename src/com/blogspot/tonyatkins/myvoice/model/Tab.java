package com.blogspot.tonyatkins.myvoice.model;

import java.util.ArrayList;

public class Tab {
	public static final String _ID            = "id";
	public static final String LABEL          = "label";

	public static final String TABLE_NAME = "tab";
	public static final String TABLE_CREATE = 
		"CREATE TABLE " +
		TABLE_NAME + " (" +
		_ID + " integer primary key, " +
		LABEL + " varchar(20) " +
		");";

	public static final String[] COLUMNS = {
		_ID,
		LABEL
	};
	public static final String TAB_ID_BUNDLE = "tab_id_bundle";
	public static final String TAB_LABEL_BUNDLE = "tab_label_bundle";
	public static final int LABEL_TEXT_TYPE = 5;
	
	private final int id;
	private String label;
	private ArrayList<SoundButton> buttons;

	public Tab(int id, String label) {
		this.id = id;
		this.label = label;
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
}
