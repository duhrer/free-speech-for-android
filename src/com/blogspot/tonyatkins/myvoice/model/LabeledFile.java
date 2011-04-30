package com.blogspot.tonyatkins.myvoice.model;

import java.io.File;
import java.net.URI;

public class LabeledFile extends File {
	private String label;
	
	public LabeledFile(String path, String label) {
		super(path);
		this.label = label;
	}

	public LabeledFile(URI uri, String label) {
		super(uri);
		this.label = label;
	}

	public LabeledFile(File dir, String name, String label) {
		super(dir, name);
		this.label = label;
	}

	public LabeledFile(String dirPath, String name, String label) {
		super(dirPath, name);
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
