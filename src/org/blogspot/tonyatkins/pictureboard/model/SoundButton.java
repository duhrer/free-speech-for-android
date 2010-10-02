package org.blogspot.tonyatkins.pictureboard.model;

import android.content.res.Resources;



public class SoundButton {
	public final static int NO_RESOURCE = -1;
	public final static String BUTTON_BUNDLE = "buttonBundle";
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
		TAB_ID + " integer" +
		");";
	
	public static final String[] COLUMNS = {
			_ID,
			LABEL,
			TTS_TEXT,
			SOUND_PATH,
			SOUND_RESOURCE,
			IMAGE_PATH,
			IMAGE_RESOURCE,
			TAB_ID
	};

	private int id;
	private String label;
	private String ttsText;
	private String soundPath;
	private int soundResource = NO_RESOURCE;
	private String imagePath;
	private int imageResource = NO_RESOURCE;
	private long tabId;
	
	/**
	 * @param savedBundle A colon-delimited string containing the flattened contents of another object 
	 * as in label:ttsText:soundPath:soundResource:imagePath:imageResource
	 */
	public SoundButton (String savedBundle) {
		loadStringBundle(savedBundle);
	}
	
	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 */
	public SoundButton(int id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundPath = soundPath;
		this.soundResource = soundResource;
		this.imagePath = imagePath;
		this.imageResource = imageResource;
		this.tabId = tabId;
	}
	
	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 */
	public SoundButton(int id, String label, String ttsText, String soundPath, String imagePath, long tabId) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundPath = soundPath;
		this.imagePath = imagePath;
		this.tabId = tabId;
	}
	
	/**
	 * @param label The text that will appear on the button face.
	 * @param ttsText The text to be spoken when the button is pressed.
	 * @param soundResource The sound resource to play when the button is pressed.
	 * @param imageResource The image resource to display in combination with the label.
	 */
	public SoundButton(int id, String label, String ttsText, int soundResource, int imageResource, long tabId) {
		super();

		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundResource = soundResource;
		this.imageResource = imageResource;
		this.tabId = tabId;
	}
	
	/**
	 * @param label The text that will appear on the button face.
	 * @param ttsText The text to be spoken when the button is pressed.
	 * @param soundResource The sound resource to play when the button is pressed.
	 * @param imagePath The image file to display in combination with the label.
	 */
	public SoundButton(int id, String label, String ttsText, int soundResource, String imagePath, long tabId) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundResource = soundResource;
		this.imagePath = imagePath;
		this.tabId = tabId;
	}
	
	/**
	 * @param label The text that will appear on the button face.
	 * @param ttsText The text to be spoken when the button is pressed.
	 * @param soundPath The sound file to play when the button is pressed.
	 * @param imageResource The image resource to display in combination with the label.
	 */
	public SoundButton(int id, String label, String ttsText, String soundPath, int imageResource, long tabId) {
		super();
		
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundPath = soundPath;
		this.imageResource = imageResource;
		this.tabId = tabId;
	}
	
	public SoundButton(SoundButton existingButton) {
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
	}

	public long getTabId() {
		return this.tabId;
	}
	private void setTabId(long tabId) {
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
	public void setTtsText(String ttsText) {
		this.ttsText = ttsText;
	}

	public String getSoundPath() {
		return soundPath;
	}
	public void setSoundPath(String path) {
		this.soundPath = path;
		this.soundResource = NO_RESOURCE;
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
		this.soundPath = null;
	}
	
	public int getImageResource() {
		return imageResource;
	}
	public void setImageResource(int imageResource) {
		this.imageResource = imageResource;
		this.imagePath = null;
	}
	
	public String getStringBundle() {
		StringBuffer outputBuffer = new StringBuffer();
		// label:ttsText:soundPath:soundResource:imagePath:imageResource
		outputBuffer.append(id);
		outputBuffer.append(":");
		if (label != null) { outputBuffer.append(label); }
		outputBuffer.append(":");
		if (ttsText != null) { outputBuffer.append(ttsText); }
		outputBuffer.append(":");
		if (soundPath != null) { outputBuffer.append(soundPath); }
		outputBuffer.append(":");
		outputBuffer.append(soundResource); 
		outputBuffer.append(":");
		if (imagePath != null) { outputBuffer.append(imagePath); }
		outputBuffer.append(":");
		outputBuffer.append(imageResource);
		
		return outputBuffer.toString();
	}
	
	public void loadStringBundle(String savedBundle) {
		// label:ttsText:soundPath:soundResource:imagePath:imageResource
		String[] savedBundleParts = savedBundle.split(":");
		id            = Integer.parseInt(savedBundleParts[0]);
		label         = savedBundleParts[1];
		ttsText       = savedBundleParts[2];
		soundPath     = savedBundleParts[3];
		soundResource = Integer.parseInt(savedBundleParts[4]);
		imagePath     = savedBundleParts[5];
		imageResource = Integer.parseInt(savedBundleParts[6]);
	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
}
