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
 */
package com.blogspot.tonyatkins.freespeech.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;
import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;

public class BackupUtils {
	public final static String XML_DATA_FILENAME = "data.xml";
	public final static int BUFFER_SIZE = 2048;

	public static void loadXMLFromZip(Activity activity, DbAdapter dbAdapter, String path, boolean deleteExistingData) {
		loadXMLFromZip(activity, dbAdapter, path, deleteExistingData, null);
	}
	
	public static void loadXMLFromZip(Activity activity, DbAdapter dbAdapter, String path, boolean deleteExistingData, ProgressDialog dialog) {
		FileInputStream in;
		try
		{
			in = new FileInputStream(path);
			loadXMLFromZip(activity, dbAdapter, in, deleteExistingData, dialog);
		}
		catch (FileNotFoundException e)
		{
			Log.e("BackupUtils", "Error loading zip file:", e);
		}
	}

	public static void loadXMLFromZip(Context context, DbAdapter dbAdapter, InputStream in, boolean deleteExistingData) {
		loadXMLFromZip(context, dbAdapter, in, deleteExistingData, null);
	}
	
	/**
	 * Load new button data from an XML file contained in a zip file. After
	 * calling this, you must refresh the TTS data from the calling activity.
	 * 
	 * @param context
	 * @param dbAdapter
	 *            An existing DbAdapter, used to write to the database.
	 * @param in
	 *            The InputStream to read from, typically from the zip file.
	 * @param deleteExistingData
	 *            Whether or not to remove the existing data.
	 */
	public static void loadXMLFromZip(Context context, DbAdapter dbAdapter, InputStream in, boolean deleteExistingData, ProgressDialog dialog) {
		BufferedInputStream bin = new BufferedInputStream(in, BUFFER_SIZE);

		// take a backup first
		if (dialog != null) dialog.setMessage("Backing up existing data...");
		exportData(context, dbAdapter);

		if (deleteExistingData)
		{
			if (dialog != null) dialog.setMessage("Deleting existing data...");
			
			dbAdapter.deleteAllButtons();
			dbAdapter.deleteAllTabs();
		}

		try
		{
			ZipInputStream zip = new ZipInputStream(bin);
			ZipEntry entry = zip.getNextEntry();
			while (entry != null)
			{
				Log.d("BackupUtils", "reading zip entry " + entry.getName() + "...");
				if (entry.getName().equals(XML_DATA_FILENAME))
				{
					if (dialog != null) dialog.setMessage("Reading XML file...");
					// This is apparently necessary to see the SAX driver
					System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

					Builder builder = new Builder();

					// unpack the XML file and display a reasonable error if
					// it's not a real XML file
					Document doc = builder.build(zip);

					// go through the XML file
					Element backup = doc.getRootElement();

					// We need to map existing tab IDs in the backup to their
					// new equivalent
					Map<Long, Long> tabIds = new HashMap<Long, Long>();

					// add tabs
					if (dialog != null) dialog.setMessage("Loading tabs...");
					Element tabs = backup.getFirstChildElement("tabs");
					Elements tabElements = tabs.getChildElements("tab");
					for (int a = 0; a < tabElements.size(); a++)
					{
						Element tabElement = tabElements.get(a);
						if (tabElement != null)
						{
							Tab tab;
							try
							{
								tab = new Tab(tabElement);
								Long newTabId = dbAdapter.createTab(tab);
								tabIds.put((long) tab.getId(), newTabId);
							}
							catch (NullPointerException e)
							{
								// Log the error, but skip errors in loading tab
								// data
								Log.e("BackupUtils", "NullPointerException loading tab from element: " + tabElement.toXML(), e);
							}
						}
					}

					// add buttons
					if (dialog != null) dialog.setMessage("Loading buttons...");
					Element buttons = backup.getFirstChildElement("buttons");
					Elements buttonElements = buttons.getChildElements("button");
					for (int a = 0; a < buttonElements.size(); a++)
					{
						Element buttonElement = buttonElements.get(a);
						if (buttonElement != null)
						{
							SoundButton button = new SoundButton(buttonElement);
							Long remappedTabId = tabIds.get(button.getTabId());
							if (remappedTabId == null)
							{
								// set the tab to the first available tab as a
								// catch-all
								Long defaultTabId = tabIds.entrySet().iterator().next().getValue();
								button.setTabId(defaultTabId);
							}
							else
							{
								button.setTabId(remappedTabId);
							}

							dbAdapter.createButton(button);
						}
					}
				}
				else
				{
					if (entry.isDirectory())
					{
						File dir = new File(Constants.HOME_DIRECTORY + "/" + entry.getName());
						dir.mkdirs();
					}
					else
					{
						// unpack all remaining files to
						// Constants.HOME_DIRECTORY
						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(Constants.HOME_DIRECTORY + "/" + entry.getName()), BUFFER_SIZE);
						byte[] buffer = new byte[BUFFER_SIZE];
						int count;
						while ((count = zip.read(buffer, 0, BUFFER_SIZE)) != -1)
						{
							out.write(buffer, 0, count);
						}
						out.flush();
						out.close();
					}
				}

				entry = zip.getNextEntry();
			}
		}
		catch (IOException e)
		{
			// Display a reasonable error if there's an error reading the file
			Log.e("BackupUtils", "Error reading ZIP file", e);
			if (dialog != null) dialog.setMessage("Error reading zip file...");
		}
		catch (ValidityException e)
		{
			Log.e("BackupUtils", "Invalid XML file inside ZIP", e);
			if (dialog != null) dialog.setMessage("Invalid XML in backup ZIP...");
		}
		catch (ParsingException e)
		{
			Log.e("BackupUtils", "Error parsing XML file inside ZIP", e);
			if (dialog != null) dialog.setMessage("Error parsing XML from backup ZIP...");
		}
	}

	public static void exportData(Context context, DbAdapter dbAdapter) {
		exportData(context, dbAdapter, null);
	}
	
	public static void exportData(Context context, DbAdapter dbAdapter, ProgressDialog dialog) {
		File backupDirectory = new File(Constants.EXPORT_DIRECTORY);
		backupDirectory.mkdirs();

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String backupFilename = "backup-" + format.format(new Date()) + ".zip";

		// create a new zip file
		try
		{
			if (dialog != null) dialog.setMessage("Creating zip file...");
			File backupFile = new File(Constants.EXPORT_DIRECTORY + "/" + backupFilename);
			FileOutputStream out = new FileOutputStream(backupFile);
			ZipOutputStream zippedOut = new ZipOutputStream(new BufferedOutputStream(out, BUFFER_SIZE));

			// create a new XML file
			if (dialog != null) dialog.setMessage("Creating XML file...");
			Element rootElement = new Element("backup");

			// read in tabs and back up to XML
			if (dialog != null) dialog.setMessage("Backing up tabs...");
			Element tabs = new Element("tabs");
			rootElement.appendChild(tabs);
			Cursor tabCursor = dbAdapter.fetchAllTabs();
			tabCursor.moveToPosition(-1);
			while (tabCursor.moveToNext())
			{
				Element tab = new Element("tab");

				Element id = new Element(Tab._ID);
				id.appendChild(String.valueOf(tabCursor.getInt(tabCursor.getColumnIndex(Tab._ID))));
				tab.appendChild(id);

				Element label = new Element(Tab.LABEL);
				label.appendChild(String.valueOf(tabCursor.getString(tabCursor.getColumnIndex(Tab.LABEL))));
				tab.appendChild(label);

				String iconFileString = tabCursor.getString(tabCursor.getColumnIndex(Tab.ICON_FILE));
				if (iconFileString != null && !iconFileString.equalsIgnoreCase("null"))
				{
					File iconFile = new File(iconFileString);
					if (iconFile.exists())
					{
						// If an external file exists, back it up
						String zipPath = "images/" + iconFile.getName();
						addFileToZip(iconFile, zipPath, zippedOut);

						Element iconFileElement = new Element(Tab.ICON_FILE);
						iconFileElement.appendChild(zipPath);
						tab.appendChild(iconFileElement);
					}

				}

				int iconResourceInt = tabCursor.getInt(tabCursor.getColumnIndex(Tab.ICON_RESOURCE));
				if (iconResourceInt != Tab.NO_RESOURCE)
				{
					Element iconResource = new Element(Tab.ICON_RESOURCE);
					iconResource.appendChild(String.valueOf(iconResourceInt));
					tab.appendChild(iconResource);
				}

				int bgColor = tabCursor.getInt(tabCursor.getColumnIndex(Tab.BG_COLOR));
				Element bgColorElement = new Element(Tab.BG_COLOR);
				bgColorElement.appendChild(String.valueOf(bgColor));
				tab.appendChild(bgColorElement);

				int sortOrderInt = tabCursor.getInt(tabCursor.getColumnIndex(Tab.SORT_ORDER));
				if (sortOrderInt != 0)
				{
					Element sortOrder = new Element(Tab.SORT_ORDER);
					sortOrder.appendChild(String.valueOf(sortOrderInt));
					tab.appendChild(sortOrder);
				}

				tabs.appendChild(tab);
			}
			tabCursor.close();
			if (dialog != null) dialog.setMessage("Finished backing up tabs...");

			// read in buttons and back up to XML
			if (dialog != null) dialog.setMessage("Backing up buttons...");
			Element buttonsElement = new Element("buttons");
			rootElement.appendChild(buttonsElement);
			Cursor buttonCursor = dbAdapter.fetchAllButtonsAsCursor();
			buttonCursor.moveToPosition(-1);
			while (buttonCursor.moveToNext())
			{
				Element buttonElement = new Element("button");

				Element idElement = new Element(SoundButton._ID);
				idElement.appendChild(String.valueOf(buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton._ID))));
				buttonElement.appendChild(idElement);

				Element tabIdElement = new Element(SoundButton.TAB_ID);
				tabIdElement.appendChild(String.valueOf(buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.TAB_ID))));
				buttonElement.appendChild(tabIdElement);

				Element labelElement = new Element(SoundButton.LABEL);
				labelElement.appendChild(buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.LABEL)));
				buttonElement.appendChild(labelElement);

				String ttsTextString = buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.TTS_TEXT));
				if (ttsTextString != null)
				{
					Element ttsTextElement = new Element(SoundButton.TTS_TEXT);
					ttsTextElement.appendChild(ttsTextString);
					buttonElement.appendChild(ttsTextElement);
				}
				// Don't bother with external sounds unless there's no TTS Text
				else
				{
					String soundFileString = buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.SOUND_PATH));
					if (soundFileString != null)
					{

						File soundFile = new File(soundFileString);
						if (soundFile.exists())
						{
							// If an external sound file exists, back it up
							String zipPath = "sounds/" + soundFile.getName();
							addFileToZip(soundFile, zipPath, zippedOut);

							Element soundFileElement = new Element(SoundButton.SOUND_PATH);
							soundFileElement.appendChild(zipPath);
							buttonElement.appendChild(soundFileElement);
						}
					}
					// We shouldn't have a sound resource unless we don't have
					// either TTS or a Sound File
					else
					{
						Element soundResourceElement = new Element(SoundButton.SOUND_RESOURCE);
						soundResourceElement.appendChild(String.valueOf(buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.SOUND_RESOURCE))));
						buttonElement.appendChild(soundResourceElement);
					}
				}

				String imageFileString = buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.IMAGE_PATH));
				if (imageFileString != null)
				{
					File imageFile = new File(imageFileString);
					if (imageFile.exists())
					{
						// If an external image file exists, back it up
						String zipPath = "images/" + imageFile.getName();
						addFileToZip(imageFile, zipPath, zippedOut);

						Element imageFileElement = new Element(SoundButton.IMAGE_PATH);
						imageFileElement.appendChild(zipPath);
						buttonElement.appendChild(imageFileElement);
					}
				}

				int imageResourceInt = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.IMAGE_RESOURCE));
				if (imageResourceInt != SoundButton.NO_RESOURCE)
				{
					Element imageResourceElement = new Element(SoundButton.IMAGE_RESOURCE);
					imageResourceElement.appendChild(String.valueOf(imageResourceInt));
					buttonElement.appendChild(imageResourceElement);
				}

				int bgColor = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.BG_COLOR));
				Element bgColorElement = new Element(SoundButton.BG_COLOR);
				bgColorElement.appendChild(String.valueOf(bgColor));
				buttonElement.appendChild(bgColorElement);

				int sortOrderInt = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.SORT_ORDER));
				if (sortOrderInt != 0)
				{
					Element sortOrderElement = new Element(SoundButton.SORT_ORDER);
					sortOrderElement.appendChild(String.valueOf(sortOrderInt));
					buttonElement.appendChild(sortOrderElement);
				}

				buttonsElement.appendChild(buttonElement);
			}
			buttonCursor.close();
			if (dialog != null) dialog.setMessage("Finished backing up buttons...");

			// write the XML output to the zip file
			ZipEntry xmlZipEntry = new ZipEntry(XML_DATA_FILENAME);
			zippedOut.putNextEntry(xmlZipEntry);

			Document doc = new Document(rootElement);

			Serializer serializer = new Serializer(zippedOut, "UTF-8");
			serializer.setIndent(2);
			serializer.setMaxLength(64);
			serializer.write(doc);

			if (dialog != null) dialog.setMessage("Saved XML file...");

			zippedOut.closeEntry();
			zippedOut.close();
			if (dialog != null) dialog.setMessage("Finished creating ZIP file...");

			// let the user know that the backup was saved
			if (dialog != null) dialog.setMessage("Zip file saved to " + backupFile.getName() + "...");

			if (dialog == null) {
				Toast.makeText(context, "Saved export to " + backupFile.getName(), Toast.LENGTH_SHORT).show();
			}
			else {
				dialog.setMessage("Saved export to " + backupFile.getName());
			}
		}
		catch (IOException e)
		{
			Toast.makeText(context, "Can't create zip file, check logs for details.", Toast.LENGTH_LONG).show();
			Log.e("BackupUtils", "Can't create backup zip file.", e);
		}
	}

	/**
	 * @param file
	 *            The File to add to the ZipOutputStream.
	 * @param path
	 *            The path to the file within the zip (i.e. the path the path
	 *            will appear to be in when unpacked)
	 * @param out
	 *            A ZipOutputStream to add the file to.
	 * @throws IOException
	 */
	private static void addFileToZip(File file, String path, ZipOutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];

		ZipEntry entry = new ZipEntry(path);
		out.putNextEntry(entry);
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
		int count;
		while ((count = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1)
		{
			out.write(buffer, 0, count);
		}
		inputStream.close();
		out.closeEntry();
	}
}
