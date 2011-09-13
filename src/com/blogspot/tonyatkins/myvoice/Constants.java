/**
 * Copyright (C) 2011 Tony Atkins <duhrer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
