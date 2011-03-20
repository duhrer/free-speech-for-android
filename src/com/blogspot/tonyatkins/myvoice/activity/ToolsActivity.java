package com.blogspot.tonyatkins.myvoice.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.listeners.ActivityQuitListener;
import com.blogspot.tonyatkins.myvoice.model.FileIconListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;

public class ToolsActivity extends Activity {
	public final static int BUFFER_SIZE = 2048;
	private DbAdapter dbAdapter;
	public final static String XML_DATA_FILENAME = "data.xml";
	
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
					File iconFile = new File(iconFileString);
					if (iconFile.exists()) {
						Element iconFileElement = new Element(Tab.ICON_FILE);
						iconFileElement.appendChild(iconFileString);
						tab.appendChild(iconFileElement);
						
						// If an external file exists, back it up
						addFileToZip(iconFile,"images/" + iconFile.getName(),zippedOut);
					}
					
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
							addFileToZip(soundFile,"sounds/" + soundFile.getName(),zippedOut);
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
						addFileToZip(imageFile,"images/"+imageFile.getName(), zippedOut);
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
			ZipEntry xmlZipEntry = new ZipEntry(XML_DATA_FILENAME);
			zippedOut.putNextEntry(xmlZipEntry);
			
			Document doc = new Document(rootElement);
			
			Serializer serializer = new Serializer(zippedOut, "UTF-8");
			serializer.setIndent(2);
			serializer.setMaxLength(64);
			serializer.write(doc);  
		      
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

	/**
	 * @param file The File to add to the ZipOutputStream.
	 * @param out A ZipOutputStream to add the file to.
	 * @throws IOException
	 */
	private void addFileToZip(File file, ZipOutputStream out) throws IOException {
		addFileToZip(file,file.getName(),out);
	}
	
	/**
	 * @param file The File to add to the ZipOutputStream.
	 * @param path The path to the file within the zip (i.e. the path the path will appear to be in when unpacked)
	 * @param out A ZipOutputStream to add the file to.
	 * @throws IOException
	 */
	private void addFileToZip(File file, String path, ZipOutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];

		ZipEntry entry = new ZipEntry(path);
		out.putNextEntry(entry);
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file),BUFFER_SIZE);
		int count;
		while ((count=inputStream.read(buffer,0,BUFFER_SIZE)) != -1) {
			out.write(buffer,0,count);
		}
		inputStream.close();
		out.closeEntry();
	}
	private void importData() {
		// ask whether to replace existing data
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Delete existing data?");
		builder.setPositiveButton("Yes", new RestoreChoiceListener(this,true));
		builder.setNegativeButton("No", new RestoreChoiceListener(this,false));
		Dialog dialog = builder.create();
		dialog.show();
	}
	
	private class RestoreChoiceListener implements Dialog.OnClickListener {
		private boolean result;
		private Context context;
		
		/**
		 * @param context The Context in which to display subsequent dialogs, et cetera.
		 * @param result Whether to preserve data in the resulting restore launched by the dialog.
		 */
		public RestoreChoiceListener(Context context, boolean result) {
			this.context = context;
			this.result = result;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			loadBackup(context, result);
		}
	}

	public void loadBackup(Context context, boolean preserveExistingData) {
		if (!preserveExistingData) {
			// take a backup first
			Toast.makeText(context, "Backing up data...", Toast.LENGTH_SHORT);
			exportData();
			
			Toast.makeText(context, "Deleting data...", Toast.LENGTH_SHORT);
			dbAdapter.deleteAllButtons();
			dbAdapter.deleteAllTabs();
			
			File ttsDir = new File(Constants.TTS_OUTPUT_DIRECTORY);
			if (ttsDir.exists() && ttsDir.isDirectory()) {
				recursivelyDelete(ttsDir);
			}
		}

		// prompt for backup location (using file picker)
		Intent intent = new Intent(context,FilePickerActivity.class);
		intent.putExtra(FilePickerActivity.FILE_TYPE_BUNDLE, FileIconListAdapter.BACKUP_FILE_TYPE);
		intent.putExtra(FilePickerActivity.CWD_BUNDLE, Constants.EXPORT_DIRECTORY);
		int	requestCode = FilePickerActivity.REQUEST_CODE;
		((Activity) context).startActivityForResult(intent, requestCode);
	}

	private void recursivelyDelete(File file) {
		if (! file.exists()) return;
		if (file.isDirectory()) {
			recursivelyDelete(file);
		}
		else if (file.isFile()) {
			file.delete();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null) {
			Bundle returnedBundle = data.getExtras();
			if (returnedBundle != null) {
				if (requestCode == FilePickerActivity.REQUEST_CODE) {
					if (resultCode == FilePickerActivity.FILE_SELECTED) {
						int fileType = returnedBundle.getInt(FilePickerActivity.FILE_TYPE_BUNDLE);
						String path = returnedBundle.getString(FilePickerActivity.FILE_NAME_BUNDLE);
						if (fileType != 0) {
							if (fileType == FileIconListAdapter.BACKUP_FILE_TYPE) {
								ZipFile zip;
								try {
									zip = new ZipFile(path);
									Enumeration e = zip.entries();
									
									while (e.hasMoreElements()) {
										ZipEntry entry = (ZipEntry) e.nextElement();
										
										if (entry.getName().equals(XML_DATA_FILENAME)) {
											Builder builder = new Builder();
											InputStream in = zip.getInputStream(entry);
											// unpack the XML file and display a reasonable error if it's not a real XML file
											Document doc = builder.build(in);
											in.close();
											
											// go through the XML file
											Element backup = doc.getRootElement();
											

											// We need to map existing tab IDs in the backup to their new equivalent
											Map<Long,Long> tabIds = new HashMap<Long,Long>();
											
											// add tabs
											Element tabs = backup.getFirstChildElement("tabs");
											Elements tabElements = tabs.getChildElements("tab");
											for (int a=0; a<tabElements.size(); a++) {
												Element tabElement = tabElements.get(a);
												Tab tab = new Tab(tabElement);
												Long newTabId = dbAdapter.createTab(tab);
												
												tabIds.put( (long) tab.getId(), newTabId);
											}
											
											// add buttons
											Element buttons = backup.getFirstChildElement("buttons");
											Elements buttonElements = tabs.getChildElements("tab");
											for (int a=0; a<buttonElements.size(); a++) {
												Element buttonElement = buttonElements.get(a);
												SoundButton button = new SoundButton(buttonElement);
												button.setTabId(tabIds.get(button.getTabId()));
												dbAdapter.createButton(button);
											}
										}
										else {
											// if  a button includes a reference to a file, unpack that file from the ZIP
											BufferedInputStream in = new BufferedInputStream(zip.getInputStream(entry));
											BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(Constants.HOME_DIRECTORY + "/" + entry.getName()),BUFFER_SIZE);
											byte[] buffer = new byte[BUFFER_SIZE];
											int count;
											while ((count = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
												out.write(buffer, 0, count);
											}
											out.flush();
											out.close();
											in.close();
										}
									}
									
									
								} catch (IOException e) {
									// Display a reasonable error if there's an error reading the file
									Log.e(getClass().toString(), "Error reading ZIP file", e);
									Toast.makeText(this, "Error reading zip file...",Toast.LENGTH_SHORT).show();
								} catch (ValidityException e) {
									Log.e(getClass().toString(), "Invalid XML file inside ZIP", e);
									Toast.makeText(this, "Invalid XML in backup ZIP...",Toast.LENGTH_SHORT).show();
								} catch (ParsingException e) {
									Log.e(getClass().toString(), "Error parsing XML file inside ZIP", e);
									Toast.makeText(this, "Error parsing XML from backup ZIP...",Toast.LENGTH_SHORT).show();
								}
							}
						}
					}
				}
			}
		}
	}
}
