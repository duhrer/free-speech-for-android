package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.model.ColorWheelListAdapter;
import com.blogspot.tonyatkins.myvoice.view.SoundButtonView;

public class ColorPickerActivity extends Activity {
	private String selectedColor;
	
	private GridView gridView; 
	private SoundButtonView previewButton;
	
	private ColorWheelListAdapter colorWheelListAdapter;
	
	final static String COLOR_BUNDLE = "color";
	final static int COLOR_SELECTED = 321;
	public static final int REQUEST_CODE = 321;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.color_picker);
		
		// get the existing color from the bundle
		if (savedInstanceState != null) {
			String tempColor = savedInstanceState.getString(COLOR_BUNDLE);
			if (tempColor != null) {
				setSelectedColor(tempColor);
			}
			else {
				setSelectedColor(null);
			}
		}
		
		previewButton = (SoundButtonView) findViewById(R.id.colorPickerPreviewButton);
		
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
		this.selectedColor = selectedColor;
		
		if (selectedColor != null) {
			try {
				Color.parseColor(selectedColor);
				previewButton.setButtonBackgroundColor(selectedColor);
				previewButton.invalidate();
			} catch (IllegalArgumentException e) {
				Toast.makeText(this, "Can't use selected color, setting to transparent instead.", Toast.LENGTH_LONG);
				this.selectedColor = null;
				previewButton.setButtonBackgroundColor(null);
				previewButton.invalidate();
			}
		}
		else {
			previewButton.setButtonBackgroundColor(null);
			previewButton.invalidate();
		}
		
		// highlight the selected color
		colorWheelListAdapter.setSelectedColor(selectedColor);
		gridView.invalidateViews();
	}

	public String getSelectedColor() {
		return selectedColor;
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
			super();
			this.activity = activity;
		}

		@Override
		public void onClick(View v) {
			Intent returnedIntent = new Intent();
			if (getSelectedColor() != null) {
				Bundle bundle = new Bundle();
				bundle.putString(ColorPickerActivity.COLOR_BUNDLE, getSelectedColor());
				returnedIntent.putExtras(bundle);
			}
			activity.setResult(ColorPickerActivity.COLOR_SELECTED, returnedIntent);
			activity.finish();
		}
	}
}
