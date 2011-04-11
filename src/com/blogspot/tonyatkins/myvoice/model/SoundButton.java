package com.blogspot.tonyatkins.myvoice.model;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import nu.xom.Element;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;

public class SoundButton {
	private static final long serialVersionUID = 1L;
	public final static int NO_RESOURCE = -1;
	public static final String BUTTON_BUNDLE = "buttonBundle";
	public static final String BUTTON_ID_BUNDLE = "buttonIdBundle";
	public static final int LABEL_TEXT_TYPE = 0;
	public static final int TTS_TEXT_TYPE   = 1;
	
	// Handles to keep column names in the db consistent
	public static final String _ID            = "id";
	public static final String LABEL          = "label";
	public static final String TTS_TEXT       = "tts_text";
	public static final String SOUND_PATH     = "sound_path";
	public static final String SOUND_RESOURCE = "sound_resource";
	public static final String IMAGE_PATH     = "image_path";
	public static final String IMAGE_RESOURCE = "image_resource";
	public static final String TAB_ID         = "tab_id";
	public static final String BG_COLOR  	  = "background_color";
	public static final String SORT_ORDER	  = "sort_order";


	public static final String TABLE_NAME = "button";
	public static final String TABLE_CREATE = 
		"CREATE TABLE " +
		TABLE_NAME + " (" +
		_ID + " integer primary key, " +
		LABEL + " varchar(20), " +
		TTS_TEXT + " varchar(255), " +
		SOUND_RESOURCE + " integer, " +
		SOUND_PATH + " text, " +
		IMAGE_RESOURCE + " integer, " +
		IMAGE_PATH + " text," +
		TAB_ID + " integer," +
		BG_COLOR + " varchar(255), " +
		SORT_ORDER + " integer" +
		");";
	
	public static final String[] COLUMNS = {
			_ID,
			LABEL,
			TTS_TEXT,
			SOUND_PATH,
			SOUND_RESOURCE,
			IMAGE_PATH,
			IMAGE_RESOURCE,
			TAB_ID,
			BG_COLOR,
			SORT_ORDER
	};

	private long id;
	private String label;
	private String ttsText;
	private String soundPath;
	private int soundResource = NO_RESOURCE;
	private String imagePath;
	private int imageResource = NO_RESOURCE;
	private long tabId;
	private String bgColor;
	private int sortOrder;
	private final SoundReferee soundReferee;
	// local override for tts-to-file service, used with disposable button objects used during adding/editing
	private boolean saveTtsToFile = true;
	
	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 * @param sortOrder The order in which to display this button
	 * @param bgColor The background color to use for this button
	 */
	public SoundButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, String bgColor, int sortOrder, SoundReferee soundReferee) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundPath = soundPath;
		this.soundResource = soundResource;
		this.imagePath = imagePath;
		this.imageResource = imageResource;
		this.tabId = tabId;
		this.sortOrder = sortOrder;
		this.bgColor = bgColor;
		this.soundReferee = soundReferee;
	}
	
	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 */
	public SoundButton(long id, String label, String ttsText, String soundPath, String imagePath, long tabId, SoundReferee soundReferee) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundPath = soundPath;
		this.imagePath = imagePath;
		this.tabId = tabId;
		this.soundReferee = soundReferee;
	}
	
	/**
	 * @param label The text that will appear on the button face.
	 * @param ttsText The text to be spoken when the button is pressed.
	 * @param soundResource The sound resource to play when the button is pressed.
	 * @param imageResource The image resource to display in combination with the label.
	 */
	public SoundButton(long id, String label, String ttsText, int soundResource, int imageResource, long tabId, SoundReferee soundReferee) {
		super();

		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundResource = soundResource;
		this.imageResource = imageResource;
		this.tabId = tabId;
		this.soundReferee = soundReferee;
	}
	
	/**
	 * @param label The text that will appear on the button face.
	 * @param ttsText The text to be spoken when the button is pressed.
	 * @param soundResource The sound resource to play when the button is pressed.
	 * @param imagePath The image file to display in combination with the label.
	 */
	public SoundButton(long id, String label, String ttsText, int soundResource, String imagePath, long tabId, SoundReferee soundReferee) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundResource = soundResource;
		this.imagePath = imagePath;
		this.tabId = tabId;
		this.soundReferee = soundReferee;
	}
	
	/**
	 * @param label The text that will appear on the button face.
	 * @param ttsText The text to be spoken when the button is pressed.
	 * @param soundPath The sound file to play when the button is pressed.
	 * @param imageResource The image resource to display in combination with the label.
	 */
	public SoundButton(long id, String label, String ttsText, String soundPath, int imageResource, long tabId, SoundReferee soundReferee) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundPath = soundPath;
		this.imageResource = imageResource;
		this.tabId = tabId;
		this.soundReferee = soundReferee;
	}
	
	public SoundButton(SoundButton existingButton, SoundReferee soundReferee) {
		super();
		
		if (existingButton != null) {
			this.id = existingButton.getId();
			this.label = existingButton.getLabel();
			this.ttsText = existingButton.getTtsText();
			this.soundPath = existingButton.getSoundPath();
			this.soundResource = existingButton.getSoundResource();
			this.imagePath = existingButton.getImagePath();
			this.imageResource = existingButton.getImageResource();
			this.tabId = existingButton.getTabId();
		}
		
		this.soundReferee = soundReferee;
	}

	public SoundButton(SerializableSoundButton button) {
			id = button.id;
			label = button.label;
			ttsText = button.ttsText;
			soundPath = button.soundPath;
			soundResource = button.soundResource;
			imagePath = button.imagePath;
			imageResource = button.imageResource;
			tabId = button.tabId;
			bgColor = button.bgColor;
			sortOrder = button.sortOrder;
			
			soundReferee = null;
	}

	public SoundButton(Element element) {
		this.id = Integer.parseInt(element.getFirstChildElement(_ID).getValue());
		
		Element labelElement = element.getFirstChildElement(LABEL);
		if (labelElement != null) this.label = labelElement.getValue();
		
		Element ttsTextElement = element.getFirstChildElement(TTS_TEXT);
		if (ttsTextElement != null) this.ttsText = ttsTextElement.getValue();

		Element soundPathElement = element.getFirstChildElement(SOUND_PATH);
		if (soundPathElement != null) {
			if (soundPathElement.getValue().startsWith("/")) {
				this.soundPath = soundPathElement.getValue();
			}
			else {
				this.soundPath = Constants.HOME_DIRECTORY + "/" + soundPathElement.getValue();
			}
		}
		
		Element soundResourceElement = element.getFirstChildElement(SOUND_RESOURCE);
		if (soundResourceElement != null) this.soundResource = Integer.parseInt(soundResourceElement.getValue());
		
		Element imagePathElement = element.getFirstChildElement(IMAGE_PATH);
		if (imagePathElement != null) { 
			if (imagePathElement.getValue().startsWith("/")) {
				this.imagePath = imagePathElement.getValue();
			}
			else {
				this.imagePath = Constants.HOME_DIRECTORY + "/" + imagePathElement.getValue();
			}
		}
		
		Element imageResourceElement = element.getFirstChildElement(IMAGE_RESOURCE);
		if (imageResourceElement != null) this.imageResource = Integer.parseInt(imageResourceElement.getValue());
		
		Element bgColorElement = element.getFirstChildElement(BG_COLOR);
		if (bgColorElement != null) this.bgColor = bgColorElement.getValue();
		
		Element sortOrderElement = element.getFirstChildElement(SORT_ORDER);
		if (sortOrderElement == null) {
			// TODO: When sort order is implemented, this handling will need to be improved.
			this.sortOrder = (int) id;
		}
		else {
			this.sortOrder = Integer.parseInt(sortOrderElement.getValue());
		}
		
		Element tabIdElement = element.getFirstChildElement(TAB_ID);
		if (tabIdElement != null) this.tabId = Integer.parseInt(tabIdElement.getValue());
		
		soundReferee = null;
	}

	public long getTabId() {
		return this.tabId;
	}
	public void setTabId(long tabId) {
		this.tabId = tabId;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public String getTtsText() {
		return ttsText;
	}
	
	public boolean hasTtsOutput() {
		File ttsOutput = new File(getTtsOutputFile());
		if (ttsOutput.exists()) return true;
		
		return false;
	}
	
	public String getTtsOutputFile() {
		return Constants.TTS_OUTPUT_DIRECTORY + "/" + getId() + "/" + getId() + ".wav";
	}

	public void setTtsText(String ttsText) {
		this.ttsText = ttsText;
		soundPath = null;
		soundResource = SoundButton.NO_RESOURCE;
		
		saveTtsToFile();
	}

	public String getSoundPath() {
		return soundPath;
	}
	public void setSoundPath(String path) {
		this.soundPath = path;
		ttsText = null;
		soundResource = SoundButton.NO_RESOURCE;
	}
	
	public String getSoundFileName() {
		if (soundPath != null) {
			return (String) soundPath.subSequence(soundPath.lastIndexOf("/")+1, soundPath.length());
		}

		return "";
	}

	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String path) {
		this.imagePath = path;
		this.imageResource = NO_RESOURCE;
	}
	
	public int getSoundResource() {
		return soundResource;
	}
	public void setSoundResource(int soundResource) {
		this.soundResource = soundResource;
		soundPath = null;
		ttsText = null;
	}
	
	public int getImageResource() {
		return imageResource;
	}
	public void setImageResource(int imageResource) {
		this.imageResource = imageResource;
		this.imagePath = null;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Serializable getSerializable() {
		return new SerializableSoundButton(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + id);
		result = prime * result
				+ ((imagePath == null) ? 0 : imagePath.hashCode());
		result = prime * result + imageResource;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result
				+ ((soundPath == null) ? 0 : soundPath.hashCode());
		result = prime * result + soundResource;
		result = prime * result + ((ttsText == null) ? 0 : ttsText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SoundButton other = (SoundButton) obj;
		if (id != other.id)
			return false;
		if (imagePath == null) {
			if (other.imagePath != null)
				return false;
		} else if (!imagePath.equals(other.imagePath))
			return false;
		if (imageResource != other.imageResource)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (soundPath == null) {
			if (other.soundPath != null)
				return false;
		} else if (!soundPath.equals(other.soundPath))
			return false;
		if (soundResource != other.soundResource)
			return false;
		if (ttsText == null) {
			if (other.ttsText != null)
				return false;
		} else if (!ttsText.equals(other.ttsText))
			return false;
		return true;
	}
	


	public void setTabId(String currentTabTag) {
		setTabId(Long.getLong(currentTabTag));
	}

	public String getBgColor() {
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getImageFileName() {
		if (imagePath != null) {
			return (String) imagePath.subSequence(imagePath.lastIndexOf("/")+1, imagePath.length());
		}

		return "";
	}
		
	public boolean saveTtsToFile() {
		try {
			// If we have a sound Referee, we can use the context to look up our preferences.
			boolean saveTTS = false;
			if (soundReferee != null) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(soundReferee.getContext());
				saveTTS = preferences.getBoolean("saveTTS", false) && saveTtsToFile;
				Log.d(getClass().getCanonicalName(),"Retrieved preferences, saveTTS is set to " + String.valueOf(saveTTS) + ".");
			}
			
			if ((getTtsText() == null || getTtsText().length() == 0 || !saveTTS) && getTtsOutputFile() != null) {
				// remove the existing sound file if we have no TTS
				File existingFile = new File(getTtsOutputFile());
				if (existingFile.exists()) { 
					existingFile.delete(); 
				}
				
				return true;
			}
			else {
				// Create the directory if it doesn't exist
				File outputDir = new File(Constants.TTS_OUTPUT_DIRECTORY + "/" + getId());
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}
				
				TextToSpeech tts = soundReferee.getTts();
				if (tts != null) {
					// Save the file
					HashMap<String, String> myHashRender = new HashMap<String,String>();
					myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(getId()));
					int returnCode = tts.synthesizeToFile(getTtsText(), myHashRender, getTtsOutputFile());
					if (returnCode == TextToSpeech.SUCCESS) {
						return true;
					}
					else {
						Log.e("TTS Error", "Can't save TTS output for button.  ID: (" + getId() + "), TTS Text: (" + getTtsText() + ").  The error code was: " + returnCode);
					}
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getCanonicalName(), "Exception while saving file to TTS:", e);
		}
		
		return false;
	}
	
	public static class SerializableSoundButton implements Serializable {
		private long id;
		private String label;
		private String ttsText;
		private String soundPath;
		private int soundResource = NO_RESOURCE;
		private String imagePath;
		private int imageResource = NO_RESOURCE;
		private long tabId;
		private String bgColor;
		private int sortOrder;

		public SerializableSoundButton (SoundButton button) {
			id = button.id;
			label = button.label;
			ttsText = button.ttsText;
			soundPath = button.soundPath;
			soundResource = button.soundResource;
			imagePath = button.imagePath;
			imageResource = button.imageResource;
			tabId = button.tabId;
			bgColor = button.bgColor;
			sortOrder = button.sortOrder;
		}
		
		public SoundButton getSoundButton() {
			return new SoundButton(this);
		}
	}

	public void setSaveTtsToFile(boolean saveTtsToFile) {
		this.saveTtsToFile = saveTtsToFile;
	}
}
