package com.blogspot.tonyatkins.myvoice.view;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.activity.EditButtonActivity;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.ButtonListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;

public class SoundButtonView extends LinearLayout {
	private static final CharSequence EDIT_BUTTON_MENU_ITEM_TITLE = "Edit Button";
	private static final CharSequence DELETE_BUTTON_MENU_ITEM_TITLE = "Delete Button";
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
	
	final String[] configurationDialogOptions = {"Edit Button", "Delete Button"};
	
	public SoundButtonView(Context context, SoundButton soundButton, SoundReferee soundReferee, ButtonListAdapter buttonListAdapter, DbAdapter dbAdapter) {
		super(context);
		
		this.context = context;
		this.soundButton = soundButton;
		this.soundReferee = soundReferee;
		this.buttonListAdapter = buttonListAdapter;
		this.dbAdapter = dbAdapter;
		
		setOrientation(LinearLayout.VERTICAL);
		
		imageLayer = new ImageView(context);
		loadImage();
		addView(imageLayer);
		
		textLayer = new TextView(context);
		textLayer.setGravity(Gravity.CENTER);
		textLayer.setTextColor(Color.BLACK);
		addView(textLayer);

		setText(soundButton.getLabel());
		setBackgroundResource(android.R.drawable.btn_default);
		if (soundButton.getBgColor() != null && soundButton.getBgColor().startsWith("#")) {
			// Praise be to StackOverflow for this tip: http://stackoverflow.com/questions/1521640/standard-android-button-with-a-different-color
			int bgColor = Color.parseColor(soundButton.getBgColor());
			getBackground().setColorFilter(bgColor,PorterDuff.Mode.MULTIPLY);
			if (getPerceivedBrightness(bgColor) < 125) {
				setTextColor(Color.WHITE);
			}
		}
		
		setOnClickListener(buttonListener);
		
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

	private int getPerceivedBrightness(int bgColor) {
		// Adapted from http://www.nbdtech.com/Blog/archive/2008/04/27/Calculating-the-Perceived-Brightness-of-a-Color.aspx
		int r = Color.red(bgColor);
		int g = Color.green(bgColor);
		int b = Color.blue(bgColor);
		return (int)Math.sqrt(
				(r * r * .241) + 
				(g * g * .691) + 
				(b * b * .068));
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
			if (configurationDialogOptions.length > which) {
				selectedOption = configurationDialogOptions[which];
			}

			if (selectedOption.equals("Edit Button")) {
				Intent editButtonIntent = new Intent(context,EditButtonActivity.class);
				Bundle editButtonBundle = new Bundle();
				editButtonBundle.putString(SoundButton.BUTTON_ID_BUNDLE,String.valueOf(soundButton.getId()));
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
    	
	private void loadImage() {
		if (soundButton.getImageResource() != SoundButton.NO_RESOURCE) {
			imageLayer.setImageResource(soundButton.getImageResource());
		}
		else if (soundButton.getImagePath() != null && new File(soundButton.getImagePath()).exists()) {
			BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),soundButton.getImagePath());
			imageLayer.setImageDrawable(bitmapDrawable);
		}
		else {
			imageLayer.setImageResource(android.R.drawable.ic_media_play);
		}
		
		imageLayer.setScaleType(ScaleType.FIT_CENTER);
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

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		menu.add(EDIT_BUTTON_MENU_ITEM_TITLE);
		menu.add(DELETE_BUTTON_MENU_ITEM_TITLE);
		super.onCreateContextMenu(menu);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int centerX = getMeasuredWidth()/2;
		
		int startImageY = getPaddingTop();
		imageLayer.layout(centerX - (imageLayer.getMeasuredWidth()/2), startImageY, centerX + (imageLayer.getMeasuredWidth()/2) , startImageY + imageLayer.getMeasuredHeight());
		int startTextY = getMeasuredHeight() - getPaddingBottom() - textLayer.getMeasuredHeight();
		textLayer.layout(centerX - (textLayer.getMeasuredWidth()/2), startTextY, centerX + (textLayer.getMeasuredWidth()/2) , startTextY + textLayer.getMeasuredHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, 2*widthMeasureSpec/3);
		setMeasuredDimension(getMeasuredWidth(), 2*getMeasuredWidth()/3);

		int sideWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int sideHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

		textLayer.measure(sideWidth, sideHeight/4);
		imageLayer.measure(sideWidth, 3*sideHeight/4);
		imageLayer.setMinimumHeight(3*sideHeight/4);
		imageLayer.setMaxHeight(3*sideHeight/4);
		imageLayer.setMinimumWidth(3*sideHeight/4);
		imageLayer.setMaxWidth(3*sideHeight/4);
	}
}
