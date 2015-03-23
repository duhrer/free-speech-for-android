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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;
import com.blogspot.tonyatkins.freespeech.db.TabDbAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class BackupUtils {
	public final static String XML_DATA_FILENAME = "data.xml";
	public final static int BUFFER_SIZE = 2048;

	public static void loadXMLFromZip(Activity activity, SQLiteDatabase db, String path, boolean deleteExistingData) {
		loadXMLFromZip(activity, db, path, deleteExistingData, null);
	}
	
	public static void loadXMLFromZip(Activity activity, SQLiteDatabase db, String path, boolean deleteExistingData, ProgressDialog dialog) {
		FileInputStream in;
		try
		{
			in = new FileInputStream(path);
			loadXMLFromZip(activity, db, in, deleteExistingData, dialog);
		}
		catch (FileNotFoundException e)
		{
			Log.e("BackupUtils", "Error loading zip file:", e);
		}
	}

	public static void loadXMLFromZip(Context context, SQLiteDatabase db, InputStream in, boolean deleteExistingData) {
		loadXMLFromZip(context, db, in, deleteExistingData, null);
	}
	
	/**
	 * Load new button data from an XML file contained in a zip file. After
	 * calling this, you must refresh the TTS data from the calling activity.
	 * 
	 * @param context
     *            The Context in which this button will have its views constructed.
	 * @param db
	 *            An existing SQLiteDatabase
	 * @param in
	 *            The InputStream to read from, typically from the zip file.
	 * @param deleteExistingData
	 *            Whether or not to remove the existing data.
	 */
	public static void loadXMLFromZip(Context context, SQLiteDatabase db, InputStream in, boolean deleteExistingData, ProgressDialog dialog) {
		BufferedInputStream bin = new BufferedInputStream(in, BUFFER_SIZE);

		if (deleteExistingData)
		{
			if (dialog != null) dialog.setMessage("Deleting existing data...");
			
			SoundButtonDbAdapter.deleteAllButtons(db);
			TabDbAdapter.deleteAllTabs(db);
		}

		try
		{
			ZipInputStream zip = new ZipInputStream(bin);
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null)
			{
				Log.d("BackupUtils", "reading zip entry " + entry.getName() + "...");
				if (entry.getName().equals(XML_DATA_FILENAME))
				{
					if (dialog != null) dialog.setMessage("Reading XML file...");

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(zip);
					
					// go through the XML file
					Element backup = doc.getDocumentElement();

					// We need to map existing tab IDs in the backup to their
					// new equivalent
					Map<Long, Long> tabIds = new HashMap<Long, Long>();

					// add tabs
					if (dialog != null) dialog.setMessage("Loading tabs...");
					
					Node tabsNode = XmlUtils.getFirstChildElement(backup, "tabs");
					if (tabsNode == null) {
						Log.e(Constants.TAG, "XML file does not contain any tabs, can't continue.");
						return;
					}
					
					NodeList tabNodeList = tabsNode.getChildNodes();
					for (int a = 0; a < tabNodeList.getLength(); a++)
					{
						Node tabNode = tabNodeList.item(a);
						if (tabNode != null && tabNode.getNodeName().toLowerCase().equals("tab"))
						{
							Tab tab;
							try
							{
								tab = new Tab(tabNode);
								Long newTabId = TabDbAdapter.createTab(tab, db);
								tabIds.put(tab.getId(), newTabId);
							}
							catch (NullPointerException e)
							{
								// Log the error, but skip errors in loading tab
								// data
								Log.e("BackupUtils", "NullPointerException loading tab from element: " + tabNode.toString(), e);
							}
						}
					}

					// add buttons
					if (dialog != null) dialog.setMessage("Loading buttons...");
					
					Node buttonsNode = XmlUtils.getFirstChildElement(backup, "buttons");
					if (buttonsNode == null) {
						Log.w(Constants.TAG, "No buttons found in backup.");
						return;
					}
					
					NodeList buttonNodeList = buttonsNode.getChildNodes();
					for (int a = 0; a < buttonNodeList.getLength(); a++)
					{
						Node buttonNode = buttonNodeList.item(a);
						if (buttonNode != null  && buttonNode.getNodeName().toLowerCase().equals("button"))
						{
							SoundButton button = new SoundButton(buttonNode);
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

							SoundButtonDbAdapter.createButton(button, db);
						}
					}
				}
				else
				{
					if (entry.isDirectory())
					{
						File dir = new File(Constants.HOME_DIRECTORY + "/" + entry.getName());
                        boolean dirCreated = dir.mkdirs();
                        if (!dirCreated) {
                            Log.e(Constants.TAG, "Cannot create output directory, backup is unlikely to work as expected.");
                        }
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
			}
			zip.close();
		}
		catch (Exception e)
		{
			// Display a reasonable error if there's an error reading the file
			Log.e("BackupUtils", "Error reading ZIP file", e);
			if (dialog != null) dialog.setMessage("Error reading zip file...");
		}
	}

	public static void exportData(Context context, SQLiteDatabase db) {
		File backupDirectory = new File(Constants.EXPORT_DIRECTORY);
        backupDirectory.mkdirs();
        if (!backupDirectory.exists()) {
            Log.e(Constants.TAG, "Could not create backup directory, backup is unlike to work as expected.");
        }

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String backupFilename = "backup-" + format.format(new Date()) + ".zip";

		// create a new zip file
		try
		{
			File backupFile = new File(Constants.EXPORT_DIRECTORY + "/" + backupFilename);
			FileOutputStream out = new FileOutputStream(backupFile);
			ZipOutputStream zippedOut = new ZipOutputStream(new BufferedOutputStream(out, BUFFER_SIZE));

			// create a new XML file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			
			Element rootElement = doc.createElement("backup");
            doc.appendChild(rootElement);

			// read in tabs and back up to XML
			Element tabs = doc.createElement("tabs");
			rootElement.appendChild(tabs);
			Cursor tabCursor = TabDbAdapter.fetchAllTabsAsCursor(db);
			tabCursor.moveToPosition(-1);
			while (tabCursor.moveToNext())
			{
				Element tab = doc.createElement("tab");

				Element id = doc.createElement(Tab._ID);
                String idValue = String.valueOf(tabCursor.getInt(tabCursor.getColumnIndex(Tab._ID)));
                id.setTextContent(idValue);

				tab.appendChild(id);

				Element label = doc.createElement(Tab.LABEL);
                String labelValue = String.valueOf(StringEscapeUtils.escapeXml(tabCursor.getString(tabCursor.getColumnIndex(Tab.LABEL))));
                label.setTextContent(labelValue);
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

						Element iconFileElement = doc.createElement(Tab.ICON_FILE);
						iconFileElement.setTextContent(StringEscapeUtils.escapeXml(zipPath));
						tab.appendChild(iconFileElement);
					}
				}

				int iconResourceInt = tabCursor.getInt(tabCursor.getColumnIndex(Tab.ICON_RESOURCE));
				if (iconResourceInt != Tab.NO_RESOURCE)
				{
					Element iconResource = doc.createElement(Tab.ICON_RESOURCE);
					iconResource.setTextContent(String.valueOf(iconResourceInt));
					tab.appendChild(iconResource);
				}

				int bgColor = tabCursor.getInt(tabCursor.getColumnIndex(Tab.BG_COLOR));
				Element bgColorElement = doc.createElement(Tab.BG_COLOR);
				bgColorElement.setTextContent(String.valueOf(bgColor));
				tab.appendChild(bgColorElement);

				int sortOrderInt = tabCursor.getInt(tabCursor.getColumnIndex(Tab.SORT_ORDER));
				Element sortOrder = doc.createElement(Tab.SORT_ORDER);
				sortOrder.setTextContent(String.valueOf(sortOrderInt));
				tab.appendChild(sortOrder);

				tabs.appendChild(tab);
			}
			tabCursor.close();

			// read in buttons and back up to XML
			Element buttonsElement = doc.createElement("buttons");
			rootElement.appendChild(buttonsElement);
			Cursor buttonCursor = SoundButtonDbAdapter.fetchAllButtonsAsCursor(db);
			buttonCursor.moveToPosition(-1);
			while (buttonCursor.moveToNext())
			{
				Element buttonElement = doc.createElement("button");

				Element idElement = doc.createElement(SoundButton._ID);
                String idValue = String.valueOf(buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton._ID)));
                idElement.setTextContent(idValue);
				buttonElement.appendChild(idElement);

				Element tabIdElement = doc.createElement(SoundButton.TAB_ID);
				tabIdElement.setTextContent(String.valueOf(buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.TAB_ID))));
				buttonElement.appendChild(tabIdElement);

				Element labelElement = doc.createElement(SoundButton.LABEL);
				labelElement.setTextContent(StringEscapeUtils.escapeXml(buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.LABEL))));
				buttonElement.appendChild(labelElement);
				
				String ttsTextString = buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.TTS_TEXT));
				if (ttsTextString != null)
				{
					Element ttsTextElement = doc.createElement(SoundButton.TTS_TEXT);
					ttsTextElement.setTextContent(StringEscapeUtils.escapeXml(ttsTextString));
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

							Element soundFileElement = doc.createElement(SoundButton.SOUND_PATH);
							soundFileElement.setTextContent(StringEscapeUtils.escapeXml(zipPath));
							buttonElement.appendChild(soundFileElement);
						}
					}
					// We shouldn't have a sound resource unless we don't have
					// either TTS or a Sound File
					else
					{
						Element soundResourceElement = doc.createElement(SoundButton.SOUND_RESOURCE);
						soundResourceElement.setTextContent(String.valueOf(buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.SOUND_RESOURCE))));
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

						Element imageFileElement = doc.createElement(SoundButton.IMAGE_PATH);
						imageFileElement.setTextContent(StringEscapeUtils.escapeXml(zipPath));
						buttonElement.appendChild(imageFileElement);
					}
				}

				int imageResourceInt = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.IMAGE_RESOURCE));
				if (imageResourceInt != SoundButton.NO_RESOURCE)
				{
					Element imageResourceElement = doc.createElement(SoundButton.IMAGE_RESOURCE);
					imageResourceElement.setTextContent(String.valueOf(imageResourceInt));
					buttonElement.appendChild(imageResourceElement);
				}

				int bgColor = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.BG_COLOR));
				Element bgColorElement = doc.createElement(SoundButton.BG_COLOR);
				bgColorElement.setNodeValue(String.valueOf(bgColor));
				buttonElement.appendChild(bgColorElement);

				int sortOrderInt = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.SORT_ORDER));
				Element sortOrderElement = doc.createElement(SoundButton.SORT_ORDER);
				sortOrderElement.setTextContent(String.valueOf(sortOrderInt));
				buttonElement.appendChild(sortOrderElement);

				int linkedTabInt = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.LINKED_TAB_ID));
				if (linkedTabInt != 0) {
					Element linkedTabIdElement = doc.createElement(SoundButton.LINKED_TAB_ID);
					linkedTabIdElement.setTextContent(String.valueOf(linkedTabInt));
					buttonElement.appendChild(linkedTabIdElement);
				}
				
				buttonsElement.appendChild(buttonElement);
			}
			buttonCursor.close();

			// write the XML output to the zip file
			// save the XML to the zip file
			ZipEntry ze = new ZipEntry(XML_DATA_FILENAME);
			zippedOut.putNextEntry(ze);

			// we have to iterate over the tree manually because Android lacks the Transformer class we would ordinarily use
            // TODO:  Examine doc to see if it's broken or if our XMLUtils are...
			String xmlContent = XmlUtils.convertDomToString(doc);
			zippedOut.write(xmlContent.getBytes());
			
			zippedOut.closeEntry();
			zippedOut.close();

            Toast.makeText(context, "Saved backup to file '" + backupFile.getName() + "'...", Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
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
