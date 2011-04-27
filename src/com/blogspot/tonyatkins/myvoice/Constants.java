package com.blogspot.tonyatkins.myvoice;

public class Constants {
	// FIXME: When we upgrade to API Level 8 or beyond, switch to getExternalFilesDir() so that data
	// will be removed on an uninstall.
	public static final String HOME_DIRECTORY = "/sdcard/com.blogspot.tonyatkins.myvoice";
	public static final String IMAGE_DIRECTORY = HOME_DIRECTORY + "/images";
	public static final String SOUND_DIRECTORY = HOME_DIRECTORY + "/sounds";
	public static final String TTS_OUTPUT_DIRECTORY = SOUND_DIRECTORY + "/tts";
	public static final String EXPORT_DIRECTORY = HOME_DIRECTORY + "/export";
	
	// Preference keys
	public static final String FULL_SCREEN_PREF = "fullScreen";
	public static final String TTS_VOICE_PREF = "tts_voice";
	public static final String TTS_SAVE_PREF = "saveTTS";
	public static final String COLUMNS_PREF = "columns";
	public static final String SCALE_TEXT_PREF = "scaleTextWidth";
	
	// Defaults
	public static final String DEFAULT_COLUMNS = "3";
	public static final int MAX_LABEL_LENGTH = 15;
}
