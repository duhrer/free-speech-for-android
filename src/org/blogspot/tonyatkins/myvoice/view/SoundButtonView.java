package org.blogspot.tonyatkins.myvoice.view;

import org.blogspot.tonyatkins.myvoice.EditButtonActivity;
import org.blogspot.tonyatkins.myvoice.R;
import org.blogspot.tonyatkins.myvoice.controller.MediaPlayerReferee;
import org.blogspot.tonyatkins.myvoice.db.DbAdapter;
import org.blogspot.tonyatkins.myvoice.model.ButtonListAdapter;
import org.blogspot.tonyatkins.myvoice.model.SoundButton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class SoundButtonView extends Button {
	Context context;
	SoundButton soundButton;
	MediaPlayerReferee mediaPlayerReferee;
	ButtonListAdapter buttonListAdapter;
	private DbAdapter dbAdapter;
	
	MediaPlayer mediaPlayer;
	ButtonOnClickListener buttonListener = new ButtonOnClickListener();
	AlertDialog alertDialog;
	AlertDialog configureDialog;
	AlertDialog notImplementedDialog;
	Dialog filePickerDialog;
	Dialog labelEditDialog;
	Dialog ttsEditDialog;
	
	TextEditView labelEditView;
	TextEditView ttsEditDialogView;
	FilePickerView filePickerView;
	
	final String[] configurationDialogOptions = {"Edit Button", "Delete Button"};
	
	public SoundButtonView(Context context, SoundButton soundButton, MediaPlayerReferee mediaPlayerReferee, ButtonListAdapter buttonListAdapter, DbAdapter dbAdapter) {
		super(context);
		
		this.context = context;
		this.soundButton = soundButton;
		this.mediaPlayerReferee = mediaPlayerReferee;
		this.buttonListAdapter = buttonListAdapter;
		this.dbAdapter = dbAdapter;
		
		setText(soundButton.getLabel());
		
		setOnClickListener(buttonListener);

		this.setBackgroundResource(R.drawable.button);
		
		if (soundButton.getSoundResource() != SoundButton.NO_RESOURCE) {
			mediaPlayer = MediaPlayer.create(context, soundButton.getSoundResource());
		}
		else {
			mediaPlayer = loadSoundFromPath();
		}

		// TODO: Make a working image-based button and do something with the image data
		
		// Everyone gets a configuration dialog
		AlertDialog.Builder configurationDialogBuilder = new AlertDialog.Builder(context);
		configurationDialogBuilder.setTitle("Configure Button");
		configurationDialogBuilder.setItems(configurationDialogOptions, new ConfigurationDialogOnClickListener());
		configurationDialogBuilder.setCancelable(true);
		configureDialog = configurationDialogBuilder.create();
		
		// A "not implemented" dialog for functions that aren't handled at the moment
		AlertDialog.Builder notImplementedDialogBuilder = new AlertDialog.Builder(context);
		notImplementedDialogBuilder.setTitle("Not Implemented");
		notImplementedDialogBuilder.setMessage("This option hasn't been implemented yet.");
		notImplementedDialogBuilder.setCancelable(true);
		notImplementedDialog = notImplementedDialogBuilder.create();
				
		setOnLongClickListener(buttonListener);
	}
	

	private class ConfigurationDialogOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			String selectedOption = "";
			if (configurationDialogOptions.length > which) {
				selectedOption = configurationDialogOptions[which];
			}

			if (selectedOption.equals("Edit Button")) {
				Intent editButtonIntent = new Intent(context,EditButtonActivity.class);
				Bundle editButtonBundle = new Bundle();
				editButtonBundle.putString(SoundButton.BUTTON_BUNDLE,soundButton.getStringBundle());
				editButtonIntent.putExtras(editButtonBundle);
				Activity activity = (Activity) context;
				activity.startActivityForResult(editButtonIntent, EditButtonActivity.EDIT_BUTTON);
			}
			else if (selectedOption.equals("Delete Button")) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Delete Button?");
				builder.setCancelable(true);
				builder.setMessage("Are you sure you want to delete this button?");
				builder.setPositiveButton("Yes", new onConfirmDeleteListener());
				builder.setNegativeButton("No", new onCancelDeleteListener());
				
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
			else {
				notImplementedDialog.show();
			}
		}
	}

	private class onConfirmDeleteListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dbAdapter.deleteButton(soundButton);
			buttonListAdapter.refresh();
			((GridView) getParent()).invalidateViews();
			Toast.makeText(context, "Button Deleted", Toast.LENGTH_LONG).show();
		}
	}
	
	private class onCancelDeleteListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}
	
	public void reloadSound() {
		mediaPlayer = loadSoundFromPath();
	}
	
	
	private MediaPlayer loadSoundFromPath() {
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(soundButton.getSoundPath());
			mediaPlayer.prepare();
			setOnClickListener(buttonListener);
			return mediaPlayer;
		} catch (Exception e) {
			/*
			 * Because the buttons are loaded on startup, we don't actually have anywhere to display load errors.
			 * So, we create an AlertDialog for the object and set it up so that touching the button displays
			 * the current error.
			 */
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
			});

			builder.setMessage("Error loading file  (" + soundButton.getSoundPath() + ").  Press and hold button to reconfigure its sound preferences.");

			alertDialog = builder.create();
			this.setOnClickListener(new BrokenButtonOnClickListener());
			e.printStackTrace();
		}

		return null;
	}
	
	private class ButtonOnClickListener implements View.OnClickListener, View.OnLongClickListener {

		public void onClick(View v) {
			// If something is already playing, we don't care what it is, just stop
			if (mediaPlayerReferee.isPlaying()) {
				mediaPlayerReferee.stop();
			}
			else {
				// If our mediaPlayer isn't the active one, set it to the active one and play
				if (mediaPlayer != null && !mediaPlayer.equals(mediaPlayerReferee.getActiveMediaPlayer())) {
					mediaPlayerReferee.setActiveMediaPlayer(mediaPlayer);		
				}
				
				mediaPlayerReferee.start();
			}
		}
    	
		public boolean onLongClick(View v) {
			configureDialog.show();
			return true;
		}
    }
    
    private class BrokenButtonOnClickListener implements View.OnClickListener {

		public void onClick(View v) {
			alertDialog.show();				
		}
    	
    }
	
	private void loadImageFromPath() {
		// TODO Auto-generated method stub
		
	}

	public SoundButton getSoundButton() {
		return soundButton;
	}


	public void updateLabel(String label) {
		soundButton.setLabel(label);
	}


	public void updateTtsText(String ttsText) {
		soundButton.setTtsText(ttsText);
	}
}
