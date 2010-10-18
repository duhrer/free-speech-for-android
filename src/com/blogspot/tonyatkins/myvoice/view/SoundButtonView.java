package com.blogspot.tonyatkins.myvoice.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.EditButtonActivity;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.ButtonListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;

public class SoundButtonView extends Button {
	private Context context;
	private SoundButton soundButton;
	private SoundReferee soundReferee;
	private ButtonListAdapter buttonListAdapter;
	private DbAdapter dbAdapter;
	
	private ButtonOnClickListener buttonListener = new ButtonOnClickListener();
	private AlertDialog alertDialog;
	private AlertDialog configureDialog;
	private AlertDialog notImplementedDialog;
//	private Dialog filePickerDialog;
//	private Dialog labelEditDialog;
//	private Dialog ttsEditDialog;
//	
//	private TextEditView labelEditView;
//	private TextEditView ttsEditDialogView;
//	private FilePickerView filePickerView;
	
	final String[] configurationDialogOptions = {"Edit Button", "Delete Button"};
	
	public SoundButtonView(Context context, SoundButton soundButton, SoundReferee soundReferee, ButtonListAdapter buttonListAdapter, DbAdapter dbAdapter) {
		super(context);
		
		this.context = context;
		this.soundButton = soundButton;
		this.soundReferee = soundReferee;
		this.buttonListAdapter = buttonListAdapter;
		this.dbAdapter = dbAdapter;
		
		setText(soundButton.getLabel());
		
		setOnClickListener(buttonListener);

//		this.setBackgroundResource(R.drawable.button);

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

	
	// FIXME: Error handling still doesn't work for broken buttons
	private class ButtonOnClickListener implements View.OnClickListener, View.OnLongClickListener {
		public void onClick(View v) {
			if (soundButton.hasSoundError()) {
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
			else {
				if (soundButton.equals(soundReferee.getActiveSoundButton())) {
					if (soundReferee.isPlaying()) {
						soundReferee.stop();
					}
					else {
						soundReferee.start();
					}
				}
				else {
					soundReferee.setActiveSoundButton(soundButton);		
				}
			}
		}
    	
		public boolean onLongClick(View v) {
			configureDialog.show();
			return true;
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
