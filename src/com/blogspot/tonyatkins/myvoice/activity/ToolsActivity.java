package com.blogspot.tonyatkins.myvoice.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import nu.xom.Document;
import nu.xom.Element;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ToolsActivity extends Activity {
	private DbAdapter dbAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this, new SoundReferee(this));
		
		setContentView(R.layout.tools);

		// wire up the export button
		Button exportButton = (Button) findViewById(R.id.toolsExportButton);
		exportButton.setOnClickListener(new ExportClickListener());
		
		// wire up the import button
		Button importButton = (Button) findViewById(R.id.toolsImportButton);
		importButton.setOnClickListener(new ImportClickListener());
		
		// wire up the quit button
		Button exitButton = (Button) findViewById(R.id.toolsExitButton);
		exitButton.setOnClickListener(new ActivityQuitListener(this));
	}

	private class ExportClickListener implements OnClickListener {
		public void onClick(View v) {
			exportData();
		}

	}

	private class ImportClickListener implements OnClickListener {
		public void onClick(View v) {
			importData();
		}
	}
	
	private void exportData() {
		int BUFFER_SIZE = 2048;
		byte[] buffer = new byte[BUFFER_SIZE];
		
		File backupDirectory = new File(Constants.EXPORT_DIRECTORY);
		backupDirectory.mkdirs();

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String backupFilename = "backup-" + format.format(new Date()) + ".zip";
		
		// create a new zip file
		try {
			Toast.makeText(this, "Creating zip file...", Toast.LENGTH_SHORT).show();
			File backupFile = new File(Constants.EXPORT_DIRECTORY + "/" + backupFilename);
			FileOutputStream out = new FileOutputStream(backupFile);
			ZipOutputStream zippedOut = new ZipOutputStream(new BufferedOutputStream(out,BUFFER_SIZE));
			
			// create a new XML file 
			Toast.makeText(this, "Creating XML file...", Toast.LENGTH_SHORT).show();
			Element rootElement = new Element("backup");
			
			// read in tabs and back up to XML
			Toast.makeText(this, "Backing up tabs...", Toast.LENGTH_SHORT).show();
			Element tabs = new Element("tabs");
			rootElement.appendChild(tabs);
			Cursor tabCursor = dbAdapter.fetchAllTabs();
			tabCursor.moveToPosition(-1);
			while (tabCursor.moveToNext()) {
				Element tab = new Element("tab");
				
				Element id = new Element(Tab._ID);
				id.appendChild(String.valueOf(tabCursor.getInt(tabCursor.getColumnIndex(Tab._ID))));
				tab.appendChild(id);
				
				Element label = new Element(Tab.LABEL);
				label.appendChild(String.valueOf(tabCursor.getString(tabCursor.getColumnIndex(Tab.LABEL))));
				tab.appendChild(label);
				
				String iconFileString = tabCursor.getString(tabCursor.getColumnIndex(Tab.ICON_FILE));
				if (iconFileString != null && ! iconFileString.equalsIgnoreCase("null")) {
					Element iconFile = new Element(Tab.ICON_FILE);
					iconFile.appendChild(iconFileString);
					tab.appendChild(iconFile);
					
					// FIXME: If an external file exists, back it up
				}
				
				int iconResourceInt = tabCursor.getInt(tabCursor.getColumnIndex(Tab.ICON_RESOURCE));
				if (iconResourceInt != Tab.NO_RESOURCE) {
					Element iconResource = new Element(Tab.ICON_RESOURCE);
					iconResource.appendChild(String.valueOf(iconResourceInt));
					tab.appendChild(iconResource);
				}
				
				String bgColorString = tabCursor.getString(tabCursor.getColumnIndex(Tab.BG_COLOR));
				if (bgColorString != null) {
					Element bgColor = new Element(Tab.BG_COLOR);
					bgColor.appendChild(bgColorString);
					tab.appendChild(bgColor);
				}
				
				int sortOrderInt = tabCursor.getInt(tabCursor.getColumnIndex(Tab.SORT_ORDER));
				if (sortOrderInt != 0) {
					Element sortOrder = new Element(Tab.SORT_ORDER);
					sortOrder.appendChild(String.valueOf(sortOrderInt));
					tab.appendChild(sortOrder);
				}
				
				tabs.appendChild(tab);
			}
			tabCursor.close();
			Toast.makeText(this, "Finished backing up tabs...", Toast.LENGTH_SHORT).show();
			
			// read in buttons and back up to XML
			Toast.makeText(this, "Backing up buttons...", Toast.LENGTH_SHORT).show();
			Element buttonsElement = new Element("buttons");
			rootElement.appendChild(buttonsElement);
			Cursor buttonCursor = dbAdapter.fetchAllButtons();
			buttonCursor.moveToPosition(-1);
			while (buttonCursor.moveToNext()) {
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
				if (ttsTextString != null) {
					Element ttsTextElement = new Element(SoundButton.TTS_TEXT);
					ttsTextElement.appendChild(ttsTextString);
					buttonElement.appendChild(ttsTextElement);
				}
				// Don't bother with external sounds unless there's no TTS Text
				else {
					String soundFileString = buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.SOUND_PATH));
					if (soundFileString != null) {
						Element soundFileElement = new Element(SoundButton.SOUND_PATH);
						soundFileElement.appendChild(soundFileString);
						buttonElement.appendChild(soundFileElement);
						
						File soundFile = new File(soundFileString);
						if (soundFile.exists()) {
							// If an external sound file exists, back it up
							ZipEntry soundFileZipEntry = new ZipEntry("sounds/" + soundFile.getName());
							zippedOut.putNextEntry(soundFileZipEntry);
							BufferedInputStream soundFileInputStream = new BufferedInputStream(new FileInputStream(soundFile),BUFFER_SIZE);
							int count;
							while ((count=soundFileInputStream.read(buffer,0,BUFFER_SIZE)) != -1) {
								out.write(buffer,0,count);
							}
							soundFileInputStream.close();
							zippedOut.closeEntry();
						}
					}
					// We shouldn't have a sound resource unless we don't have either TTS or a Sound File
					else {
						Element soundResourceElement = new Element(SoundButton.SOUND_RESOURCE);
						soundResourceElement.appendChild(String.valueOf(buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.SOUND_RESOURCE))));
						buttonElement.appendChild(soundResourceElement);
					}
				}
				
				String imageFileString = buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.IMAGE_PATH));
				if (imageFileString != null) {
					Element imageFileElement = new Element(SoundButton.IMAGE_PATH);
					imageFileElement.appendChild(imageFileString);
					buttonElement.appendChild(imageFileElement);
					
					File imageFile = new File(imageFileString);
					if (imageFile.exists()) {
						// If an external image file exists, back it up
						ZipEntry imageFileZipEntry = new ZipEntry("images/" + imageFile.getName());
						zippedOut.putNextEntry(imageFileZipEntry);
						BufferedInputStream imageFileInputStream = new BufferedInputStream(new FileInputStream(imageFile),BUFFER_SIZE);
						int count;
						while ((count=imageFileInputStream.read(buffer,0,BUFFER_SIZE)) != -1) {
							out.write(buffer,0,count);
						}
						imageFileInputStream.close();
						zippedOut.closeEntry();
					}
				}
				
				int imageResourceInt = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.IMAGE_RESOURCE));
				if (imageResourceInt != SoundButton.NO_RESOURCE) {
					Element imageResourceElement = new Element(SoundButton.IMAGE_RESOURCE);
					imageResourceElement.appendChild(String.valueOf(imageResourceInt));
					buttonElement.appendChild(imageResourceElement);
				}
				
				String bgColorString = buttonCursor.getString(buttonCursor.getColumnIndex(SoundButton.BG_COLOR));
				if (bgColorString != null) {
					Element bgColorElement = new Element(SoundButton.BG_COLOR);
					bgColorElement.appendChild(bgColorString);
					buttonElement.appendChild(bgColorElement);
				}
				
				int sortOrderInt = buttonCursor.getInt(buttonCursor.getColumnIndex(SoundButton.SORT_ORDER));
				if (sortOrderInt != 0) {
					Element sortOrderElement = new Element(SoundButton.SORT_ORDER);
					sortOrderElement.appendChild(String.valueOf(sortOrderInt));
					buttonElement.appendChild(sortOrderElement);
				}
				
				buttonsElement.appendChild(buttonElement);
			}
			buttonCursor.close();
			Toast.makeText(this, "Finished backing up buttons...", Toast.LENGTH_SHORT).show();
			
			// write the XML output to the zip file
			ZipEntry xmlZipEntry = new ZipEntry("data.xml");
			zippedOut.putNextEntry(xmlZipEntry);
			
			Document doc = new Document(rootElement);
			zippedOut.write(doc.toXML().getBytes());
			Toast.makeText(this, "Saved XML file...", Toast.LENGTH_SHORT).show();
			
			zippedOut.closeEntry();
			zippedOut.close();
			Toast.makeText(this, "Finished creating ZIP file...", Toast.LENGTH_SHORT).show();
			
			// let the user know that the backup was saved
			Toast.makeText(this, "Zip file saved to " + backupFile.getName() + "...", Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, "Can't create zip file, check logs for details.", Toast.LENGTH_LONG).show();
			Log.e(getClass().toString(), "Can't create backup zip file.", e);
		}
		
	}
	private void importData() {
//		ZipFile zip = new ZipFile(Constants.EXPORT_DIRECTORY + "/" + backupFilename);
		// warn about overwriting existing data
		
		// delete existing data (tts files as well)
		
		// check default location for backups
		
		// if a single file is found, prompt to restore it
		
		// if more than one file is found, open the file picker in the backup location and let the user choose
		
		// prompt for backup location (using file picker) if none is found
		
		// open the zip and display a resonable error if the file fails to open
		
		// unpack the XML file and display a reasonable error if it's not a real XML file
	
		// go through the XML file
		
		// add tabs
		
		// add buttons
		
		// if  a button includes a reference to a file, unpack that file from the zip
	}
}
