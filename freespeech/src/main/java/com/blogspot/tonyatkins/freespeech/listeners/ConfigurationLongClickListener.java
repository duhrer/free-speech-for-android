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
package com.blogspot.tonyatkins.freespeech.listeners;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.blogspot.tonyatkins.freespeech.activity.EditButtonActivity;
import com.blogspot.tonyatkins.freespeech.activity.MoveButtonActivity;
import com.blogspot.tonyatkins.freespeech.adapter.ButtonListAdapter;
import com.blogspot.tonyatkins.freespeech.db.DbOpenHelper;
import com.blogspot.tonyatkins.freespeech.db.SoundButtonDbAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.model.Tab;

import java.util.Collection;

public class ConfigurationLongClickListener implements OnLongClickListener {
	private static final String EDIT_BUTTON_MENU_ITEM_TITLE = "Edit Button";
	private static final String MOVE_BUTTON_MENU_ITEM_TITLE = "Move Button to Another Tab";
	private static final String DELETE_BUTTON_MENU_ITEM_TITLE = "Delete Button";
	final String[] configurationDialogOptions = { EDIT_BUTTON_MENU_ITEM_TITLE, MOVE_BUTTON_MENU_ITEM_TITLE, DELETE_BUTTON_MENU_ITEM_TITLE, "Cancel" };

	private final Dialog configureDialog;
	private final Dialog notImplementedDialog;
	private final Activity activity;
	private final SoundButton soundButton;
	private final ButtonListAdapter buttonListAdapter;
	private GridView gridView;
	
	public ConfigurationLongClickListener (Activity activity, SoundButton soundButton, ButtonListAdapter buttonListAdapter, GridView gridView){
		this.activity = activity;
		this.soundButton = soundButton;
		this.buttonListAdapter = buttonListAdapter;
		this.gridView = gridView;
		
		// Add a configuration dialog
		AlertDialog.Builder configurationDialogBuilder = new AlertDialog.Builder(activity);
		configurationDialogBuilder.setTitle("Button Menu");
		configurationDialogBuilder.setItems(configurationDialogOptions, new ConfigurationDialogOnClickListener());
		configurationDialogBuilder.setCancelable(true);
		configureDialog = configurationDialogBuilder.create();

		// A "not implemented" dialog for functions that aren't handled at the moment
		AlertDialog.Builder notImplementedDialogBuilder = new AlertDialog.Builder(activity);
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
				Intent editButtonIntent = new Intent(activity, EditButtonActivity.class);
				editButtonIntent.putExtra(SoundButton.BUTTON_ID_BUNDLE, String.valueOf(soundButton.getId()));
				if (activity instanceof Activity)
				{
					((Activity) activity).startActivityForResult(editButtonIntent, EditButtonActivity.EDIT_BUTTON);
				}
			}
			else if (selectedOption.equals(MOVE_BUTTON_MENU_ITEM_TITLE))
			{
				Intent moveButtonIntent = new Intent(activity, MoveButtonActivity.class);
				moveButtonIntent.putExtra(SoundButton.BUTTON_ID_BUNDLE, String.valueOf(soundButton.getId()));
				moveButtonIntent.putExtra(Tab.TAB_ID_BUNDLE, String.valueOf(soundButton.getTabId()));

				if (activity instanceof Activity)
				{
					((Activity) activity).startActivityForResult(moveButtonIntent, MoveButtonActivity.MOVE_BUTTON);
				}
			}
			else if (selectedOption.equals(DELETE_BUTTON_MENU_ITEM_TITLE))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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

            DbOpenHelper helper = new DbOpenHelper(activity);
            SQLiteDatabase db = helper.getWritableDatabase();
			SoundButtonDbAdapter.deleteButton(soundButton, db);
            db.close();

            buttonListAdapter.refresh();

			// FIXME:  Is this actually necessary?  Test by deleting a button.
            // TODO:  Do we need to refresh the button list?
//			buttonListAdapter.refresh();
			gridView.invalidateViews();
			//			((GridView) getParent()).invalidateViews();
			Toast.makeText(activity, "Button Deleted", Toast.LENGTH_LONG).show();
		}
	}
}
