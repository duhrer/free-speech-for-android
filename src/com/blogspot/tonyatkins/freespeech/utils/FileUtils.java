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
package com.blogspot.tonyatkins.freespeech.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import com.blogspot.tonyatkins.freespeech.Constants;

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
	public static String generateUniqueFilename() {
		Date date = new Date();
		return Integer.toHexString((int) date.getTime());
	}
	public static void copy(File sourceFile, File destFile) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile),Constants.BUFFER_SIZE);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile), Constants.BUFFER_SIZE);
		
		int bytes = 0;
		byte[] buffer = new byte[Constants.BUFFER_SIZE];
		while ((bytes = bis.read(buffer)) != -1) {
			bos.write(buffer, 0, bytes);
		}
		bis.close();
		bos.close();
	}
}
