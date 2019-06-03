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
package com.blogspot.tonyatkins.freespeech.model;

import java.io.File;
import java.io.Serializable;

import org.w3c.dom.Node;

import android.graphics.Color;
import android.os.Environment;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.utils.XmlUtils;

public class SoundButton implements HasId, Comparable<SoundButton>{
	public final static int NO_RESOURCE = -1;
	public static final String BUTTON_BUNDLE = "buttonBundle";
	public static final String BUTTON_ID_BUNDLE = "buttonIdBundle";
//	public static final int LABEL_TEXT_TYPE = 0;
//	public static final int TTS_TEXT_TYPE   = 1;
	
	// Handles to keep column names in the db consistent
	public static final String _ID            = "_id";
	public static final String LABEL          = "label";
	public static final String TTS_TEXT       = "tts_text";
	public static final String SOUND_PATH     = "sound_path";
	public static final String SOUND_RESOURCE = "sound_resource";
	public static final String IMAGE_PATH     = "image_path";
	public static final String IMAGE_RESOURCE = "image_resource";
	public static final String TAB_ID         = "tab_id";
	public static final String LINKED_TAB_ID  = "linked_tab_id";
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
		LINKED_TAB_ID + " integer," +
		BG_COLOR + " integer, " +
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
			LINKED_TAB_ID,
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
	private long linkedTabId;
	private int bgColor;
	private int sortOrder;

	// local override for tts-to-file service, used with disposable button objects used during adding/editing
//	private boolean saveTtsToFile = true;

	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 * @param bgColor The background color to use for this button
	 * @param sortOrder The order in which to display this button
	 */
	public SoundButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, int bgColor, int sortOrder) {
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
	}
	
	/**
	 * @param label The text that will appear on the button face
	 * @param ttsText The text to be spoken when the button is pressed
	 * @param soundPath The sound file to play when the button is pressed
	 * @param soundResource The sound resource to play when the button is pressed
	 * @param imagePath The image file to display in combination with the label
	 * @param imageResource The image resource to display in combination with the label
	 * @param tabId The id of the tab that contains this button
	 * @param linkedTabId The id of the tab to open when this button is pressed
	 * @param bgColor The background color to use for this button
	 * @param sortOrder The order in which to display this button
	 */
	public SoundButton(long id, String label, String ttsText, String soundPath, int soundResource, String imagePath, int imageResource, long tabId, long linkedTabId, int bgColor, int sortOrder) {
		super();
		this.id = id;
		this.label = label;
		this.ttsText = ttsText;
		this.soundPath = soundPath;
		this.soundResource = soundResource;
		this.imagePath = imagePath;
		this.imageResource = imageResource;
		this.tabId = tabId;
		this.linkedTabId = linkedTabId;
		this.sortOrder = sortOrder;
		this.bgColor = bgColor;
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
			this.linkedTabId = existingButton.getLinkedTabId();
		}
		
	}

	public SoundButton(Node buttonNode) {
		Node idNode = XmlUtils.getFirstChildElement(buttonNode,_ID);
		String idNodeValue = XmlUtils.getNodeValue(idNode);
		if (idNodeValue != null) {
			this.id = Integer.parseInt(idNodeValue);
		}
		
		Node labelNode = XmlUtils.getFirstChildElement(buttonNode,LABEL);
		String labelNodeValue = XmlUtils.getNodeValue(labelNode);
		if (labelNodeValue != null) {
			this.label = labelNodeValue;
		}
		
		Node ttsTextNode = XmlUtils.getFirstChildElement(buttonNode,TTS_TEXT);
		String ttxTextNodeValue = XmlUtils.getNodeValue(ttsTextNode);
		if (ttxTextNodeValue != null) {
			this.ttsText = ttxTextNodeValue;
		}

		Node soundPathNode = XmlUtils.getFirstChildElement(buttonNode,SOUND_PATH);
		String soundPathNodeValue = XmlUtils.getNodeValue(soundPathNode);
		if (soundPathNodeValue != null) {
			if (soundPathNodeValue.startsWith("/")) {
				this.soundPath = soundPathNodeValue;
			}
			else {
				this.soundPath = "/" + soundPathNodeValue;
			}
		}
		
		Node soundResourceNode = XmlUtils.getFirstChildElement(buttonNode,SOUND_RESOURCE);
		String soundResourceNodeValue = XmlUtils.getNodeValue(soundResourceNode);
		if (soundResourceNodeValue != null) {
			this.soundResource = Integer.parseInt(soundResourceNodeValue);
		}
		
		Node imagePathNode = XmlUtils.getFirstChildElement(buttonNode,IMAGE_PATH);
		String imagePathNodeValue = XmlUtils.getNodeValue(imagePathNode);
		if (imagePathNodeValue != null) {
			if (imagePathNodeValue.startsWith("/")) {
				this.imagePath = imagePathNodeValue;
			}
			else {
				this.imagePath = "/" + imagePathNodeValue;
			}
		}
		
		Node imageResourceNode = XmlUtils.getFirstChildElement(buttonNode,IMAGE_RESOURCE);
		String imageResourceNodeValue = XmlUtils.getNodeValue(imageResourceNode);
		if (imageResourceNodeValue != null) this.imageResource = Integer.parseInt(imageResourceNodeValue);
		
		Node bgColorNode = XmlUtils.getFirstChildElement(buttonNode,BG_COLOR);
		String bgColorNodeValue = XmlUtils.getNodeValue(bgColorNode);
		if (bgColorNodeValue != null) {
			if (bgColorNodeValue.startsWith("#")) {
				this.bgColor = Color.parseColor(bgColorNodeValue);
			}
			else {
				this.bgColor = Integer.valueOf(bgColorNodeValue);
			}
		}
		
		Node sortOrderNode = XmlUtils.getFirstChildElement(buttonNode,SORT_ORDER);
		String sortOrderNodeValue = XmlUtils.getNodeValue(sortOrderNode,String.valueOf(id));
		this.sortOrder = Integer.parseInt(sortOrderNodeValue);
		
		Node linkedTabIdNode = XmlUtils.getFirstChildElement(buttonNode,LINKED_TAB_ID);
		String linkedTabIdNodeValue = XmlUtils.getNodeValue(linkedTabIdNode);
		if (linkedTabIdNodeValue != null) this.linkedTabId = Integer.parseInt(linkedTabIdNodeValue);
		
		Node tabIdNode = XmlUtils.getFirstChildElement(buttonNode,TAB_ID);
		String tabIdNodeValue = XmlUtils.getNodeValue(tabIdNode);
		if (tabIdNodeValue != null) this.tabId = Integer.parseInt(tabIdNodeValue);
	}

	public SoundButton(SerializableSoundButton serializableSoundButton) {
		id = serializableSoundButton.id;
		label = serializableSoundButton.label;
		ttsText = serializableSoundButton.ttsText;
		soundPath = serializableSoundButton.soundPath;
		soundResource = serializableSoundButton.soundResource;
		imagePath = serializableSoundButton.imagePath;
		imageResource = serializableSoundButton.imageResource;
		tabId = serializableSoundButton.tabId;
		linkedTabId = serializableSoundButton.linkedTabId;
		bgColor = serializableSoundButton.bgColor;
		sortOrder = serializableSoundButton.sortOrder;
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
		File ttsOutput = new File(Environment.getExternalStorageDirectory(), getTtsOutputFile());

        return ttsOutput.exists();
	}
	
	public String getTtsOutputFile() {
		return Constants.TTS_OUTPUT_DIRECTORY + "/" + getId() + "/" + getId() + ".wav";
	}

	public void setTtsText(String ttsText) {
		if ((this.ttsText != null && !this.ttsText.equals(ttsText)) || (this.ttsText == null && ttsText != null)) {
			
			this.ttsText = ttsText;
			soundPath = null;
			soundResource = SoundButton.NO_RESOURCE;
		}
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
//	public void setSoundResource(int soundResource) {
//		this.soundResource = soundResource;
//		soundPath = null;
//		ttsText = null;
//	}
	
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
	


//	public void setTabId(String currentTabTag) {
//		setTabId(Long.getLong(currentTabTag));
//	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

//	public String getImageFileName() {
//		if (imagePath != null) {
//			return (String) imagePath.subSequence(imagePath.lastIndexOf("/")+1, imagePath.length());
//		}
//
//		return "";
//	}
		

	
	public static class SerializableSoundButton implements Serializable {
		private static final long serialVersionUID = 2082144180030673969L;
		private long id;
		private String label;
		private String ttsText;
		private String soundPath;
		private int soundResource = NO_RESOURCE;
		private String imagePath;
		private int imageResource = NO_RESOURCE;
		private long tabId;
		private long linkedTabId;
		private int bgColor;
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
			linkedTabId = button.linkedTabId;
			bgColor = button.bgColor;
			sortOrder = button.sortOrder;
		}
		
//		public SoundButton getSoundButton() {
//			return new SoundButton(this);
//		}
	}

//	public void setSaveTtsToFile(boolean saveTtsToFile) {
//		this.saveTtsToFile = saveTtsToFile;
//	}

    // Because of the way TreeSet operates, we have to make this support natural sorting by all elements.
    //
    // Elements where compareTo returns 0 are implicitly treated as equal.
    @Override
	public int compareTo(SoundButton other) {
		if (other.equals(this)) return 0;
        else if (getSortOrder() != other.getSortOrder()) {
    		return getSortOrder() - other.getSortOrder();
        }
        else if (!getLabel().equals(other.getLabel())) {
            return getLabel().compareTo(other.getLabel());
        }
        else if (!getTtsText().equals(other.getTtsText())) {
            return getTtsText().compareTo(other.getTtsText());
        }
        else if (getId() != other.getId()) {
            return Math.round(getId() - other.getId());
        }
        else if (!getImagePath().equals(other.getImagePath())) {
            return getImagePath().compareTo(other.getImagePath());
        }
        else if (getImageResource() != other.getImageResource()) {
            return getImageResource() - other.getImageResource();
        }
        else if (!getSoundPath().equals(other.getSoundPath())) {
            return getSoundPath().compareTo(other.getSoundPath());
        }
        else if (getSoundResource() != other.getSoundResource()) {
            return getSoundResource() - other.getSoundResource();
        }

        return 0;
	}

	public boolean hasSound() {
		return getSoundPath() != null && getSoundFileName() != null || getSoundResource() != NO_RESOURCE;
	}

	public long getLinkedTabId() {
		return linkedTabId;
	}

	public void setLinkedTabId(long linkedTabId) {
		this.linkedTabId = linkedTabId;
	}
}
