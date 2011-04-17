package com.blogspot.tonyatkins.myvoice.utils;

import java.io.File;

public class FileUtils {
	public static void recursivelyDelete(File file) {
		if (!file.exists()) return;
		
		// If this is a directory, we have to clear out its contents first
		if (file.isDirectory()) {
			for (String filename : file.list()) {
				File childFile = new File(filename);
				if (childFile.isDirectory()) recursivelyDelete(childFile);
				else childFile.delete();
			}
		} 
		
		// Now delete the original item
		file.delete();
	}
}
