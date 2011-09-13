/**
 * Copyright (C) 2011 Tony Atkins <duhrer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
