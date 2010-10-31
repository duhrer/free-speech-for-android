package com.blogspot.tonyatkins.myvoice.model;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundButton {
	public final static int NO_RESOURCE = -1;
	public static final String BUTTON_ID_BUNDLE = null;
	public static final int LABEL_TEXT_TYPE = 0;
	public static final int TTS_TEXT_TYPE   = 1;
	public static final int BG_COLOR_TEXT_TYPE = 2;
	
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

	private MediaPlayer mediaPlayer;
	private boolean soundError = false;
	
	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 * @param sortOrder The order in which to display this button
	 * @param bgColor The background color to use for this button
	 */
	public SoundButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, String bgColor, int sortOrder) {
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
		
		mediaPlayer = loadSound();
	}
	
	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 */
	public SoundButton(long id, String label, String ttsText, String soundPath, String imagePath, long tabId) {
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
	public SoundButton(long id, String label, String ttsText, int soundResource, int imageResource, long tabId) {
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
	public SoundButton(long id, String label, String ttsText, int soundResource, String imagePath, long tabId) {
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
	public SoundButton(long id, String label, String ttsText, String soundPath, int imageResource, long tabId) {
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
		mediaPlayer = null;
		soundPath = null;
		soundResource = SoundButton.NO_RESOURCE;
	}

	public String getSoundPath() {
		return soundPath;
	}
	public void setSoundPath(String path) {
		this.soundPath = path;
		mediaPlayer = null;
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
		mediaPlayer = null;
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
	
	public void reloadSound() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		mediaPlayer = loadSound();
	}
	
	
	private MediaPlayer loadSoundFromPath() {
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
			mediaPlayer.setDataSource(soundPath);
			mediaPlayer.prepare();
			
			return mediaPlayer;
		} catch (Exception e) {
			setSoundError(true);
			Log.e(getClass().toString(), "Error loading file", e);
		}

		return null;
	}
	
	private MediaPlayer loadSound() {
		// Don't even try to create a media player if there's TTS text
		if (ttsText != null && ttsText.length() > 0) return null;
		
		MediaPlayer mediaPlayer = new MediaPlayer();
		if (soundResource != SoundButton.NO_RESOURCE) {
			// FIXME: Either get sound resources working again or completely remove them
//			try {
//				mediaPlayer = MediaPlayer.create(context, soundButton.getSoundResource());
//				mediaPlayer.prepare();
//				mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
//			} catch (Exception e) {
//				Log.e(getClass().toString(), "Error loading file", e);
//			}
		}
		else {
			mediaPlayer = loadSoundFromPath();
		}
		
		return mediaPlayer;
	}
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	public void setSoundError(boolean soundError) {
		this.soundError = soundError;
	}

	public boolean hasSoundError() {
		return soundError;
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
}
