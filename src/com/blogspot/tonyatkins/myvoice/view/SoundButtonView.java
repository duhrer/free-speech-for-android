package com.blogspot.tonyatkins.myvoice.view;

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
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.activity.EditButtonActivity;
import com.blogspot.tonyatkins.myvoice.activity.MoveButtonActivity;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.ButtonListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.model.Tab;

public class SoundButtonView extends LinearLayout {
	private static final String EDIT_BUTTON_MENU_ITEM_TITLE = "Edit";
	private static final String MOVE_BUTTON_MENU_ITEM_TITLE = "Move";
	private static final String DELETE_BUTTON_MENU_ITEM_TITLE = "Delete";
	private static final String REFRESH_BUTTON_MENU_ITEM_TITLE = "Refresh";
	final String[] configurationDialogOptions = { EDIT_BUTTON_MENU_ITEM_TITLE, MOVE_BUTTON_MENU_ITEM_TITLE, DELETE_BUTTON_MENU_ITEM_TITLE, REFRESH_BUTTON_MENU_ITEM_TITLE, "Cancel" };
	
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
		this.context=activity;
		this.soundReferee = new SoundReferee(activity);
		this.soundButton=new SoundButton(Long.parseLong("98765"),"Preview","Preview Button",null,null,Long.parseLong("98765"), soundReferee);
		this.buttonListAdapter = null;
		
		initialize();
	}
	
	// Required for use in XML previews within Eclipse, not used otherwise
	public SoundButtonView(Context context, AttributeSet attrs) {
		super(context,attrs);
		this.context=context;
		
		String label = "Preview";
		if (attrs != null) {
			for (int a=0; a<attrs.getAttributeCount(); a++) {
				if ("text".equals(attrs.getAttributeName(a))) {
					label = attrs.getAttributeValue(a);
				}
			}
		}
		
		this.soundReferee = new SoundReferee(context);
		this.soundButton=new SoundButton(Long.parseLong("98765"),label,"Preview Button",null,null,Long.parseLong("98765"),soundReferee);
		this.buttonListAdapter = null;
		
		initialize();
	}

	public SoundButtonView(Activity activity, SoundButton soundButton, SoundReferee soundReferee, ButtonListAdapter buttonListAdapter, DbAdapter dbAdapter) {
		super(activity);
		
		this.context = activity;
		this.soundButton = soundButton;
		this.soundReferee = soundReferee;
		this.buttonListAdapter = buttonListAdapter;
		this.dbAdapter = dbAdapter;
		
		initialize();
	}

	public void setButtonBackgroundColor(String selectedColor) {
		if (selectedColor != null) {			
			try {
				Color.parseColor(selectedColor);

				// Praise be to StackOverflow for this tip: http://stackoverflow.com/questions/1521640/standard-android-button-with-a-different-color
				int bgColor = Color.parseColor(selectedColor);
				getBackground().setColorFilter(bgColor,PorterDuff.Mode.MULTIPLY);
				if (getPerceivedBrightness(bgColor) < 125) {
					setTextColor(Color.WHITE);
				}
				else {
					setTextColor(Color.BLACK);
				}
			} catch (IllegalArgumentException e) {
				Toast.makeText(context, "Can't set background color to '" + selectedColor + "'", Toast.LENGTH_LONG).show();
			}
			if (!selectedColor.equals(soundButton.getBgColor())) {
				soundButton.setBgColor(selectedColor);
			}
		}
		else {
			setTextColor(Color.BLACK);
			soundButton.setBgColor(null);
			getBackground().clearColorFilter();
		}
	}

	public void initialize() {
		if (context != null) {
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
			if (soundReferee != null && buttonListAdapter != null) {
				setOnClickListener(buttonListener);
				
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
				
				setOnLongClickListener(buttonListener);
			}
		}
	}

	public static int getPerceivedBrightness(int bgColor) {
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

			if (selectedOption.equals(EDIT_BUTTON_MENU_ITEM_TITLE)) {
				Intent editButtonIntent = new Intent(context,EditButtonActivity.class);
				editButtonIntent.putExtra(SoundButton.BUTTON_ID_BUNDLE,String.valueOf(soundButton.getId()));
				if (context instanceof Activity) {
					((Activity) context).startActivityForResult(editButtonIntent, EditButtonActivity.EDIT_BUTTON);
				}
			}
			else if (selectedOption.equals(MOVE_BUTTON_MENU_ITEM_TITLE)) {
				Intent moveButtonIntent = new Intent(context,MoveButtonActivity.class);
				moveButtonIntent.putExtra(SoundButton.BUTTON_ID_BUNDLE,String.valueOf(soundButton.getId()));
				moveButtonIntent.putExtra(Tab.TAB_ID_BUNDLE,String.valueOf(soundButton.getTabId()));
				
				if (context instanceof Activity) {
					((Activity) context).startActivityForResult(moveButtonIntent, MoveButtonActivity.MOVE_BUTTON);
				}
			}
			else if (selectedOption.equals(DELETE_BUTTON_MENU_ITEM_TITLE)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Delete Button?");
				builder.setCancelable(true);
				builder.setMessage("Are you sure you want to delete this button?");
				builder.setPositiveButton("Yes", new onConfirmDeleteListener());
				builder.setNegativeButton("No", new onCancelDeleteListener());
				
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
			else if (selectedOption.equals(REFRESH_BUTTON_MENU_ITEM_TITLE)) {
				boolean buttonSaved = soundButton.saveTtsToFile();
				if (buttonSaved) {
					Toast.makeText(context, "Button refreshed.", Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(context, "Unable to refresh button, check logs for details.", Toast.LENGTH_LONG).show();
				}
			}
			else if (selectedOption.equals("Cancel")) {
				// do nothing, just let the dialog close
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
			if (hasSoundError()) {
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
				if (((SoundButtonView) v).equals(soundReferee.getActiveSoundButton())) {
					if (soundReferee.isPlaying()) {
						soundReferee.stop();
					}
					else {
						soundReferee.start();
					}
				}
				else {
					soundReferee.setActiveSoundButton((SoundButtonView) v);		
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
		imageLayer.setScaleType(ScaleType.CENTER_INSIDE);
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
		menu.add(REFRESH_BUTTON_MENU_ITEM_TITLE);
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
		super.onMeasure(widthMeasureSpec, 3*widthMeasureSpec/5);
		setMeasuredDimension(getMeasuredWidth(), 3*getMeasuredWidth()/5);

		int sideWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int sideHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean scaleTextWidth = preferences.getBoolean(Constants.SCALE_TEXT_PREF, false);
    	if (scaleTextWidth) {
    		// Scale the size of the text to match the button width
    		Rect bounds = new Rect();
    		textLayer.getPaint().getTextBounds((String) textLayer.getText(), 0, textLayer.getText().length(), bounds);
    		float currentTextWidth = bounds.right-bounds.left;
    		
    		float textScale = textLayer.getTextScaleX();
    		float correctedTextScale = (sideWidth/currentTextWidth) * textScale;
    		textLayer.setTextScaleX(correctedTextScale);
    	}
		
		// Measure the text layer after changing the font so that the bounds will be adjusted
		textLayer.measure(sideWidth, sideHeight/4);
		
		imageLayer.measure(sideWidth, 3*sideHeight/4);
		imageLayer.setMaxHeight(3*sideHeight/4);
		imageLayer.setMaxWidth(3*sideHeight/4);
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
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		mediaPlayer = loadSound();
	}
	
	
	private MediaPlayer loadSoundFromPath(String path) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			
			return mediaPlayer;
		} catch (Exception e) {
			setSoundError(true);
			Log.e(getClass().toString(), "Error loading file", e);
		}

		return null;
	}
	
	private MediaPlayer loadSound() {
		// Don't even try to create a media player if there's TTS text
		if (soundButton.getTtsText() != null && soundButton.getTtsText().length() > 0)  {
			return null;
		}
		else {
			if (soundButton.getSoundResource() != SoundButton.NO_RESOURCE) {
				MediaPlayer mediaPlayer = new MediaPlayer();
				// FIXME: Either get sound resources working again or completely remove them
//				try {
//					mediaPlayer = MediaPlayer.create(context, soundButton.getSoundResource());
//					mediaPlayer.prepare();
//					mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
//				} catch (Exception e) {
//					Log.e(getClass().toString(), "Error loading file", e);
//				}
				return mediaPlayer;			
			}
			else {
				return loadSoundFromPath(soundButton.getSoundPath());
			}
		}
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
