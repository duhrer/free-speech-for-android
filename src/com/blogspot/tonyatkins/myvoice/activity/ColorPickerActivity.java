package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.Constants;
import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.model.ColorWheelListAdapter;
import com.blogspot.tonyatkins.myvoice.model.SoundButton;
import com.blogspot.tonyatkins.myvoice.view.ColorSwatch;
import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

public class ColorPickerActivity extends Activity {
	private GridView gridView; 
	private SoundButtonView previewButton;
	private SoundButton tempButton;
	
	private ColorWheelListAdapter colorWheelListAdapter;
	private ColorSwatch previewSwatch;
	
	final static String COLOR_BUNDLE = "color";
	final static int COLOR_SELECTED = 321;
	public static final int REQUEST_CODE = 321;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean fullScreen = preferences.getBoolean(Constants.FULL_SCREEN_PREF, false);
		if (fullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.color_picker);

		// We use a preview button if we're dealing with a button, or a swatch otherwise.
		previewButton = (SoundButtonView) findViewById(R.id.colorPickerPreviewButton);
		
		previewSwatch = (ColorSwatch) findViewById(R.id.colorPickerPreviewSwatch);
		
		Bundle bundle = this.getIntent().getExtras();
		// get the existing color from the bundle
		if (bundle != null) {
			SoundButton.SerializableSoundButton tempSerializableSoundButton = (SoundButton.SerializableSoundButton) bundle.get(SoundButton.BUTTON_BUNDLE);
			if (tempSerializableSoundButton != null) {
				tempButton = tempSerializableSoundButton.getSoundButton();
				previewButton.setSoundButton(tempButton);
				previewButton.initialize();
				previewButton.setVisibility(View.VISIBLE);
				previewSwatch.setVisibility(View.GONE);
			}
			else {
				tempButton = previewButton.getSoundButton();
			}
			
			String tabColorString = bundle.getString(ColorPickerActivity.COLOR_BUNDLE);
			if (tabColorString != null) { 
				try {
					int tabColor = Color.parseColor(tabColorString);
					previewSwatch.setBackgroundColor(tabColor);
				} 
				catch (IllegalArgumentException e) {
					// This is normal if we've been passed a bogus color.  Just ignore it and use the default color.
				}
			}
		}
		else {
			tempButton = previewButton.getSoundButton();
		}
		
		// wire up the list adapter for the colors
		gridView = (GridView) findViewById(R.id.ColorPalette);
		colorWheelListAdapter = new ColorWheelListAdapter(this);
		gridView.setAdapter(colorWheelListAdapter);
		
		// wire up the link to set the color to null
		TextView textView = (TextView) findViewById(R.id.SetColorToTransparent);
		textView.setOnClickListener(new SetColorToNullListener());
				
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.CancelColor);
		cancelButton.setOnClickListener(new ActivityCancelListener());
		
		// wire up the save button
		Button selectButton = (Button) findViewById(R.id.SelectColor);
		selectButton.setOnClickListener(new SelectColorListener(this));
	}

	public void setSelectedColor(String selectedColor) {
		if (selectedColor != null) {
			try {
				int selectedColorInt = Color.parseColor(selectedColor);
				previewButton.setButtonBackgroundColor(selectedColor);
				previewButton.invalidate();
				previewSwatch.setBackgroundColor(selectedColorInt);
				previewSwatch.invalidate();
			} catch (IllegalArgumentException e) {
				Toast.makeText(this, "Can't use selected color, setting to transparent instead.", Toast.LENGTH_LONG).show();
				previewSwatch.setBackgroundColor(Color.TRANSPARENT);
				previewSwatch.invalidate();
				previewButton.setButtonBackgroundColor(null);
				previewButton.invalidate();
			}
		}
		else {
			previewSwatch.setBackgroundColor(Color.TRANSPARENT);
			previewSwatch.invalidate();
			previewButton.setButtonBackgroundColor(null);
			previewButton.invalidate();
		}
		
		// highlight the selected color
		colorWheelListAdapter.setSelectedColor(selectedColor);
		gridView.invalidateViews();
	}
	
	private class ActivityCancelListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
		}
	}
	
	private class SetColorToNullListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			setSelectedColor(null);
		}
	}
	
	private class SelectColorListener implements OnClickListener {
		private Activity activity;
		
		public SelectColorListener(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onClick(View v) {
			Intent returnedIntent = new Intent();
			if (tempButton.getBgColor() != null) {
				Bundle bundle = new Bundle();
				bundle.putString(ColorPickerActivity.COLOR_BUNDLE, tempButton.getBgColor());
				returnedIntent.putExtras(bundle);
			}
			activity.setResult(ColorPickerActivity.COLOR_SELECTED, returnedIntent);
			activity.finish();
		}
	}
}
