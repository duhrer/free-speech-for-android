package org.blogspot.tonyatkins.myvoice.model;

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
	
	private int id;
	private String label;
	private ArrayList<SoundButton> buttons;

	public Tab(String label) {
		super();
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

	public void setId(int id) {
		this.id = id;
	}
	
	public void addButton(SoundButton button){
		buttons.add(button);
	}
	
	public void removeButton(SoundButton button) {
		buttons.remove(button);
	}
}
