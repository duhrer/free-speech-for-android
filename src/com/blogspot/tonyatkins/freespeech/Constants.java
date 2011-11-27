/**
 * Copyright 2011 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
package com.blogspot.tonyatkins.freespeech;

public class Constants {
	// FIXME: When we upgrade to API Level 8 or beyond, switch to getExternalFilesDir() so that data
	// will be removed on an uninstall.
	public static final String HOME_DIRECTORY = "/sdcard/com.blogspot.tonyatkins.freespech";
	public static final String IMAGE_DIRECTORY = HOME_DIRECTORY + "/images";
	public static final String SOUND_DIRECTORY = HOME_DIRECTORY + "/sounds";
	public static final String TTS_OUTPUT_DIRECTORY = SOUND_DIRECTORY + "/tts";
	public static final String EXPORT_DIRECTORY = HOME_DIRECTORY + "/export";
	
	// Preference keys
	public static final String COLUMNS_PREF = "columns";
	public static final String DEV_OPTIONS_PREF = "enableDevOptions";
	public static final String FULL_SCREEN_PREF = "fullScreen";
	public static final String HIDE_TAB_CONTROLS_PREF = "hideTabControls";
	public static final String SCALE_TEXT_PREF = "scaleTextWidth";
	public static final String SWIPE_TAB_PREF = "swipeTabs";
	public static final String TTS_VOICE_PREF = "tts_voice";
	public static final String TTS_SAVE_PREF = "saveTTS";
	
	// Defaults
	public static final String DEFAULT_COLUMNS = "3";
	public static final int MAX_LABEL_LENGTH = 15;
	
	// JIRA Mobile Connect
	public static final String JMC_URL     = "http://connect.onjira.com/";
	public static final String JMC_PROJECT = "FS";
}
