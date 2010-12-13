package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.model.ColorWheelListAdapter;

public class ColorPickerActivity extends Activity {
	private String selectedColor;
	private String originalColor;
	
	private LinearLayout mainView;
	
	final static String COLOR_BUNDLE = "color";
	final static int COLOR_SELECTED = 321;
	public static final int REQUEST_CODE = 321;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.color_picker);
		mainView = (LinearLayout) findViewById(R.id.colorPickerMain);
		
		// get the existing color from the bundle
		if (savedInstanceState != null) {
			String tempColor = savedInstanceState.getString(COLOR_BUNDLE);
			if (tempColor != null) {
				try {
					Color.parseColor(tempColor);
					setSelectedColor(tempColor);
					setOriginalColor(tempColor);
				} catch (IllegalArgumentException e) {
					Toast.makeText(this, "Ignoring illegal color '" + tempColor + "'", Toast.LENGTH_LONG);
				}
			}
			else {
				setSelectedColor(null);
			}
		}
		
		// wire up the list adapter for the colors
		GridView colorWheel = (GridView) findViewById(R.id.ColorPalette);
		colorWheel.setAdapter(new ColorWheelListAdapter(this));
		
		// wire up the link to set the color to null
		TextView textView = (TextView) findViewById(R.id.SetColorToTransparent);
		textView.setOnClickListener(new SetColorToNullListener());
		
		// wire up the reset button
		Button resetButton = (Button) findViewById(R.id.ResetColor);
		resetButton.setOnClickListener(new ResetColorListener());
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.CancelColor);
		cancelButton.setOnClickListener(new ActivityCancelListener());
		
		// wire up the save button
		Button selectButton = (Button) findViewById(R.id.SelectColor);
		selectButton.setOnClickListener(new SelectColorListener(this));
	}

	public void setSelectedColor(String selectedColor) {
		this.selectedColor = selectedColor;
		
		
		// FIXME: Replace background color with a "preview" swatch
		
		if (selectedColor != null) {
			try {
				int selectedColorInt = Color.parseColor(selectedColor);
				mainView.setBackgroundColor(selectedColorInt);
				mainView.invalidate();
			} catch (IllegalArgumentException e) {
				Toast.makeText(this, "Can't use selected color, setting to transparent instead.", Toast.LENGTH_LONG);
				this.selectedColor = null;
			}
		}
		else {
			mainView.setBackgroundColor(Color.TRANSPARENT);
			mainView.invalidate();
		}
		
		// TODO: highlight the selected color and scroll to it
	}

	public String getSelectedColor() {
		return selectedColor;
	}
	
	public String getOriginalColor() {
		return originalColor;
	}

	public void setOriginalColor(String originalColor) {
		this.originalColor = originalColor;
	}
	
	private class ActivityCancelListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
		}
	}

	private class ResetColorListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			setSelectedColor(getOriginalColor());
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
