package com.blogspot.tonyatkins.freespeech.listeners;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.activity.EditButtonActivity;
import com.blogspot.tonyatkins.freespeech.activity.MoveButtonActivity;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.model.ButtonListAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class ConfigurationLongClickListener implements OnLongClickListener {
	private static final String EDIT_BUTTON_MENU_ITEM_TITLE = "Edit";
	private static final String MOVE_BUTTON_MENU_ITEM_TITLE = "Move";
	private static final String DELETE_BUTTON_MENU_ITEM_TITLE = "Delete";
	final String[] configurationDialogOptions = { EDIT_BUTTON_MENU_ITEM_TITLE, MOVE_BUTTON_MENU_ITEM_TITLE, DELETE_BUTTON_MENU_ITEM_TITLE, "Cancel" };

	private final Dialog configureDialog;
	private final Dialog notImplementedDialog;
	private final Context context;
	private final DbAdapter dbAdapter;
	private final SoundButton soundButton;
	private final ButtonListAdapter buttonListAdapter;
	private GridView gridView;
	
	public ConfigurationLongClickListener (Context context, DbAdapter dbAdapter, SoundButton soundButton, ButtonListAdapter buttonListAdapter, GridView gridView){
		this.context = context;
		this.dbAdapter = dbAdapter;
		this.soundButton = soundButton;
		this.buttonListAdapter = buttonListAdapter;
		this.gridView = gridView;
		
		// Add a configuration dialog
		AlertDialog.Builder configurationDialogBuilder = new AlertDialog.Builder(context);
		configurationDialogBuilder.setTitle("Button Menu");
		configurationDialogBuilder.setItems(configurationDialogOptions, new ConfigurationDialogOnClickListener());
		configurationDialogBuilder.setCancelable(true);
		configureDialog = configurationDialogBuilder.create();

		// A "not implemented" dialog for functions that aren't handled at the moment
		AlertDialog.Builder notImplementedDialogBuilder = new AlertDialog.Builder(context);
		notImplementedDialogBuilder.setTitle("Not Implemented");
		notImplementedDialogBuilder.setMessage("This option hasn't been implemented yet.");
		notImplementedDialogBuilder.setCancelable(true);
		notImplementedDialog = notImplementedDialogBuilder.create();

	}
	
	@Override
	public boolean onLongClick(View v) {
		// FIXME:  generalize for tabs and buttons
		configureDialog.show();
		return true;
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
				builder.setPositiveButton("Yes", new OnConfirmDeleteListener());
				builder.setNegativeButton("No", new OnCancelDeleteListener());

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

	private class OnCancelDeleteListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}

	private class OnConfirmDeleteListener implements OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			// TODO: Generalize this to work for both buttons and tabs
			dbAdapter.deleteButton(soundButton);
			
			// FIXME:  Is this actually necessary?  Test by deleting a button.
			buttonListAdapter.refresh();
			gridView.invalidateViews();
			//			((GridView) getParent()).invalidateViews();
			Toast.makeText(context, "Button Deleted", Toast.LENGTH_LONG).show();
		}
	}
}
