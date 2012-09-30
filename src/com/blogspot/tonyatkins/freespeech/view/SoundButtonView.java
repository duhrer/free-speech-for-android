/**
 * Copyright 2012 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
package com.blogspot.tonyatkins.freespeech.view;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.Constants;
import com.blogspot.tonyatkins.freespeech.activity.EditButtonActivity;
import com.blogspot.tonyatkins.freespeech.activity.MoveButtonActivity;
import com.blogspot.tonyatkins.freespeech.controller.SoundReferee;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.model.ButtonListAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class SoundButtonView extends LinearLayout {
	private static final String EDIT_BUTTON_MENU_ITEM_TITLE = "Edit";
	private static final String MOVE_BUTTON_MENU_ITEM_TITLE = "Move";
	private static final String DELETE_BUTTON_MENU_ITEM_TITLE = "Delete";
	final String[] configurationDialogOptions = { EDIT_BUTTON_MENU_ITEM_TITLE, MOVE_BUTTON_MENU_ITEM_TITLE, DELETE_BUTTON_MENU_ITEM_TITLE, "Cancel" };

	private Context context;
	private SoundButton soundButton;
	private SoundReferee soundReferee;
	private ButtonListAdapter buttonListAdapter;
	private DbAdapter dbAdapter;

	private ButtonOnClickListener buttonListener = new ButtonOnClickListener();
	private AlertDialog alertDialog;
	private AlertDialog configureDialog;
	private AlertDialog notImplementedDialog;

	private ImageView imageLayer;
	private TextView textLayer;

	private MediaPlayer mediaPlayer;
	private boolean soundError = false;

	// used for preview buttons
	public SoundButtonView(Activity activity) {
		super(activity);
		this.context = activity;
		this.soundReferee = new SoundReferee(activity);
		this.soundButton = new SoundButton(Long.parseLong("98765"), "Preview", "Preview Button", null, null, Long.parseLong("98765"));
		this.buttonListAdapter = null;

		initialize();
	}

	// Required for use in XML previews within Eclipse, not used otherwise
	public SoundButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		String label = "Preview";
		if (attrs != null)
		{
			for (int a = 0; a < attrs.getAttributeCount(); a++)
			{
				if ("text".equals(attrs.getAttributeName(a)))
				{
					label = attrs.getAttributeValue(a);
				}
			}
		}

		this.soundReferee = new SoundReferee(context);
		this.soundButton = new SoundButton(Long.parseLong("98765"), label, "Preview Button", null, null, Long.parseLong("98765"));
		this.buttonListAdapter = null;

		initialize();
	}

	public SoundButtonView(Activity activity, SoundButton soundButton, SoundReferee soundReferee,
			ButtonListAdapter buttonListAdapter, DbAdapter dbAdapter) {
		super(activity);

		this.context = activity;
		this.soundButton = soundButton;
		this.soundReferee = soundReferee;
		this.buttonListAdapter = buttonListAdapter;
		this.dbAdapter = dbAdapter;

		initialize();
	}

	public void setButtonBackgroundColor(String selectedColor) {
		if (selectedColor != null)
		{
			try
			{
				Color.parseColor(selectedColor);

				// Praise be to StackOverflow for this tip:
				// http://stackoverflow.com/questions/1521640/standard-android-button-with-a-different-color
				int bgColor = Color.parseColor(selectedColor);
				getBackground().setColorFilter(bgColor, PorterDuff.Mode.MULTIPLY);
				if (getPerceivedBrightness(bgColor) < 125)
				{
					setTextColor(Color.WHITE);
				}
				else
				{
					setTextColor(Color.BLACK);
				}
			}
			catch (IllegalArgumentException e)
			{
				Toast.makeText(context, "Can't set background color to '" + selectedColor + "'", Toast.LENGTH_LONG).show();
			}
			if (!selectedColor.equals(soundButton.getBgColor()))
			{
				soundButton.setBgColor(selectedColor);
			}
		}
		else
		{
			setTextColor(Color.BLACK);
			soundButton.setBgColor(null);
			getBackground().clearColorFilter();
		}
	}

	public void initialize() {
		if (context != null)
		{
			setOrientation(LinearLayout.VERTICAL);

			imageLayer = new ImageView(context);
			addView(imageLayer);

			textLayer = new TextView(context);
			textLayer.setGravity(Gravity.CENTER);
			addView(textLayer);

			reload();

			mediaPlayer = loadSound();

			// Only buttons that are wired into the sound harness get a listener
			// Other buttons are dummy buttons used for visual previews.
			if (soundReferee != null && buttonListAdapter != null)
			{
				setOnClickListener(buttonListener);

				// Add a configuration dialog
				AlertDialog.Builder configurationDialogBuilder = new AlertDialog.Builder(context);
				configurationDialogBuilder.setTitle("Button Menu");
				configurationDialogBuilder.setItems(configurationDialogOptions, new ConfigurationDialogOnClickListener());
				configurationDialogBuilder.setCancelable(true);
				configureDialog = configurationDialogBuilder.create();

				// A "not implemented" dialog for functions that aren't handled
				// at the
				// moment
				AlertDialog.Builder notImplementedDialogBuilder = new AlertDialog.Builder(context);
				notImplementedDialogBuilder.setTitle("Not Implemented");
				notImplementedDialogBuilder.setMessage("This option hasn't been implemented yet.");
				notImplementedDialogBuilder.setCancelable(true);
				notImplementedDialog = notImplementedDialogBuilder.create();

				setOnLongClickListener(buttonListener);
			}
		}
	}

	public static int getPerceivedBrightness(int bgColor) {
		// Adapted from
		// http://www.nbdtech.com/Blog/archive/2008/04/27/Calculating-the-Perceived-Brightness-of-a-Color.aspx
		int r = Color.red(bgColor);
		int g = Color.green(bgColor);
		int b = Color.blue(bgColor);
		return (int) Math.sqrt((r * r * .241) + (g * g * .691) + (b * b * .068));
	}

	private void setTextColor(int color) {
		textLayer.setTextColor(color);
	}

	private void setText(String label) {
		textLayer.setText(label);
	}

	private class ConfigurationDialogOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			String selectedOption = "";
			if (configurationDialogOptions.length > which)
			{
				selectedOption = configurationDialogOptions[which];
			}

			if (selectedOption.equals(EDIT_BUTTON_MENU_ITEM_TITLE))
			{
				Intent editButtonIntent = new Intent(context, EditButtonActivity.class);
				editButtonIntent.putExtra(SoundButton.BUTTON_ID_BUNDLE, String.valueOf(soundButton.getId()));
				if (context instanceof Activity)
				{
					((Activity) context).startActivityForResult(editButtonIntent, EditButtonActivity.EDIT_BUTTON);
				}
			}
			else if (selectedOption.equals(MOVE_BUTTON_MENU_ITEM_TITLE))
			{
				Intent moveButtonIntent = new Intent(context, MoveButtonActivity.class);
				moveButtonIntent.putExtra(SoundButton.BUTTON_ID_BUNDLE, String.valueOf(soundButton.getId()));
				moveButtonIntent.putExtra(Tab.TAB_ID_BUNDLE, String.valueOf(soundButton.getTabId()));

				if (context instanceof Activity)
				{
					((Activity) context).startActivityForResult(moveButtonIntent, MoveButtonActivity.MOVE_BUTTON);
				}
			}
			else if (selectedOption.equals(DELETE_BUTTON_MENU_ITEM_TITLE))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Delete Button?");
				builder.setCancelable(true);
				builder.setMessage("Are you sure you want to delete this button?");
				builder.setPositiveButton("Yes", new onConfirmDeleteListener());
				builder.setNegativeButton("No", new onCancelDeleteListener());

				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
			else if (selectedOption.equals("Cancel"))
			{
				// do nothing, just let the dialog close
			}
			else
			{
				notImplementedDialog.show();
			}
		}
	}

	private class onConfirmDeleteListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			dbAdapter.deleteButton(soundButton);
			buttonListAdapter.refresh();
			((GridView) getParent()).invalidateViews();
			Toast.makeText(context, "Button Deleted", Toast.LENGTH_LONG).show();
		}
	}

	private class onCancelDeleteListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}

	// FIXME: Error handling still doesn't work for broken buttons
	private class ButtonOnClickListener implements View.OnClickListener, View.OnLongClickListener {
		public void onClick(View v) {
			if (hasSoundError())
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setCancelable(true);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

				builder.setMessage("Error loading sound.  Press and hold button to reconfigure.");

				alertDialog = builder.create();
				alertDialog.show();
			}
			else
			{
				soundReferee.playSoundButton((SoundButtonView) v);
			}
		}

		public boolean onLongClick(View v) {
			configureDialog.show();
			return true;
		}
	}

	private void loadImage() {
		if (soundButton.getImageResource() != SoundButton.NO_RESOURCE)
		{
			imageLayer.setImageResource(soundButton.getImageResource());
		}
		else if (soundButton.getImagePath() != null && new File(soundButton.getImagePath()).exists())
		{
			BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), soundButton.getImagePath());
			imageLayer.setImageDrawable(bitmapDrawable);
		}
		else
		{
			imageLayer.setImageResource(android.R.drawable.ic_media_play);
		}

		scaleImageLayer();
		imageLayer.invalidate();
	}

	public SoundButton getSoundButton() {
		return soundButton;
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		menu.add(EDIT_BUTTON_MENU_ITEM_TITLE);
		menu.add(DELETE_BUTTON_MENU_ITEM_TITLE);
		super.onCreateContextMenu(menu);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int centerX = getMeasuredWidth() / 2;

		int startImageY = getPaddingTop();
		imageLayer.layout(centerX - (imageLayer.getMeasuredWidth() / 2), startImageY, centerX + (imageLayer.getMeasuredWidth() / 2), startImageY + imageLayer.getMeasuredHeight());
		int startTextY = getMeasuredHeight() - getPaddingBottom() - textLayer.getMeasuredHeight();
		textLayer.layout(centerX - (textLayer.getMeasuredWidth() / 2), startTextY, centerX + (textLayer.getMeasuredWidth() / 2), startTextY + textLayer.getMeasuredHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		int sideWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int sideHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

		boolean scaleTextWidth = preferences.getBoolean(Constants.SCALE_TEXT_PREF, false);
		if (scaleTextWidth)
		{
			// Scale the size of the text to match the button width
			Rect bounds = new Rect();
			textLayer.getPaint().getTextBounds((String) textLayer.getText(), 0, textLayer.getText().length(), bounds);
			float currentTextWidth = bounds.right - bounds.left;

			// FIXME: Find a better way to correct this.
			float fudgeFactor = 0.95f;
			float textScale = textLayer.getTextScaleX() * fudgeFactor;
			float correctedTextScale = ((sideWidth - textLayer.getPaddingLeft() - textLayer.getPaddingRight()) / currentTextWidth) * textScale;
			textLayer.setTextScaleX(correctedTextScale);
		}

		// Measure the text layer after changing the font so that the bounds
		// will be
		// adjusted
		int textHeight = sideHeight / 4;
		int textWidth = sideWidth;
		textLayer.measure(textWidth, textHeight);

		scaleImageLayer();
	}

	private void scaleImageLayer() {
		int textHeight = textLayer.getMeasuredHeight();

		int sideWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int sideHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

		Drawable imageDrawable = imageLayer.getDrawable();
		if (imageDrawable == null || imageDrawable.getIntrinsicHeight() == 0 || imageDrawable.getIntrinsicWidth() == 0)
		{
			Log.w(Constants.TAG, "Button '" + soundButton.getLabel() + "' (id:" + soundButton.getId() + ") has an invalid image, and won't appear correctly.");
			return;
		}

		float imageFudgeFactor = 0.25f;
		int maxImageHeight = (int) ((sideHeight - textHeight) * imageFudgeFactor);
		int maxImageWidth = (int) (sideWidth * imageFudgeFactor);
		int imageWidth = imageDrawable.getIntrinsicWidth();
		int imageHeight = imageDrawable.getIntrinsicHeight();
		float imageRatio = imageWidth / imageHeight;
		float maxImageRatio = maxImageWidth / maxImageHeight;
		int scaledImageWidth = imageWidth;
		int scaledImageHeight = imageHeight;

		if (imageWidth > maxImageWidth || imageHeight > maxImageHeight)
		{
			if (imageRatio < maxImageRatio)
			{
				// crop to the height of the image
				scaledImageHeight = maxImageHeight;
				scaledImageWidth = scaledImageHeight * (imageWidth / imageHeight);
			}
			else
			{
				// crop to the width of the image
				scaledImageWidth = maxImageWidth;
				scaledImageHeight = scaledImageWidth * (imageHeight / imageWidth);
			}
		}

		imageLayer.setMaxHeight(scaledImageHeight);
		imageLayer.setMaxWidth(scaledImageWidth);
		imageLayer.setMinimumHeight(scaledImageHeight);
		imageLayer.setMinimumWidth(scaledImageWidth);
		imageLayer.measure(scaledImageWidth, scaledImageHeight);
	}

	public void setSoundButton(SoundButton soundButton) {
		this.soundButton = soundButton;
		initialize();
	}

	public void reload() {
		setBackgroundResource(android.R.drawable.btn_default);
		loadImage();
		imageLayer.invalidate();
		setText(soundButton.getLabel());
		textLayer.invalidate();
		setButtonBackgroundColor(soundButton.getBgColor());
		loadSound();
	}

	public void reloadSound() {
		if (mediaPlayer != null)
		{
			mediaPlayer.release();
		}
		mediaPlayer = loadSound();
	}

	private MediaPlayer loadSoundFromPath(String path) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		try
		{
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();

			return mediaPlayer;
		}
		catch (Exception e)
		{
			setSoundError(true);
			Log.e(getClass().toString(), "Error loading file", e);
		}

		return null;
	}

	private MediaPlayer loadSound() {
		if (soundButton.getSoundPath() != null)
		{
			return loadSoundFromPath(soundButton.getSoundPath());
		}

		// For TTS buttons with no cached sound file, return null
		return null;
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

	public String getTtsText() {
		return soundButton.getTtsText();
	}
}
