package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.controller.SoundReferee;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.Tab;
import com.blogspot.tonyatkins.myvoice.view.ColorSwatch;
import com.blogspot.tonyatkins.myvoice.watchers.TabLabelTextUpdateWatcher;

public class EditTabActivity extends Activity {
	public static final int ADD_TAB = 6;
	public static final int EDIT_TAB = 7;
	
	private Tab tempTab;
	private DbAdapter dbAdapter;
	private boolean isNewTab = false;
	private ColorSwatch colorSwatch;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		dbAdapter = new DbAdapter(this, new SoundReferee(this));
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String existingTabId = bundle.getString(Tab.TAB_ID_BUNDLE);
			
			// create a temporary button that we will only return on a successful save.
			if (existingTabId != null && existingTabId.length() > 0) {
				tempTab = dbAdapter.fetchTabById(existingTabId);
			}
		}
		
		if (tempTab == null) {
			isNewTab = true;
			tempTab = new Tab(0, "");
		}
				
		setContentView(R.layout.edit_tab);

		// wire up the label editing
		EditText labelEditText = (EditText) findViewById(R.id.tabLabelEditText);
		labelEditText.setText(tempTab.getLabel());
		labelEditText.addTextChangedListener(new TabLabelTextUpdateWatcher(tempTab, Tab.LABEL_TEXT_TYPE));
		
		// wire up the background color editing
		colorSwatch = (ColorSwatch) findViewById(R.id.tabBgColorColorSwatch);
		colorSwatch.setBackgroundColor(Color.TRANSPARENT);
		try {
			if (tempTab.getBgColor() != null) {
				colorSwatch.setBackgroundColor(Color.parseColor(tempTab.getBgColor()));
			}
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, "The current color is invalid and will not be displayed.", Toast.LENGTH_LONG).show();
		}
		
		// launch a color picker activity when this view is clicked
		Bundle pickColorBundle = new Bundle();
		pickColorBundle.putString(ColorPickerActivity.COLOR_BUNDLE, tempTab.getBgColor());
		colorSwatch.setOnClickListener(new LaunchIntentListener(this, ColorPickerActivity.class, pickColorBundle));

		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.tabButtonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());
		
		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.tabButtonPanelSaveButton);
		saveButton.setOnClickListener(new SaveListener(this));
	}

	private class CancelListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			finish();
		}
	}
	
	private class SaveListener implements OnClickListener {
		private final Context context;
		
		public SaveListener(Context context) {
			this.context = context;
		}
		@Override
		public void onClick(View arg0) {
			// Sanity check the data and open a dialog if there are problems
			if (tempTab.getLabel() == null || tempTab.getLabel().length() <= 0) 
			{
					Toast.makeText(context, "Can't continue without a tab label", Toast.LENGTH_LONG).show();
			}
			else 
			{
				try 
				{
					if (tempTab.getBgColor() != null) {
						Color.parseColor(tempTab.getBgColor());
					}
					Intent returnedIntent = new Intent();
					boolean saveSuccessful;
					if (isNewTab) {
						Long tabId = dbAdapter.createTab(tempTab);
						saveSuccessful = tabId != -1;
						Bundle bundle = new Bundle();
						bundle.putString(Tab.TAB_ID_BUNDLE, String.valueOf(tabId));
						returnedIntent.putExtras(bundle);
					}
					else {
						saveSuccessful = dbAdapter.updateTab(tempTab);
					}
					
					if (saveSuccessful) {
						setResult(RESULT_OK,returnedIntent);
						finish();
					}
					else {
						Toast.makeText(context, "There was an error saving this tab.", Toast.LENGTH_LONG).show();
					}
				} 
				catch (IllegalArgumentException e) 
				{
					// catch an exception if we've been passed an invalid color
					Toast.makeText(context, "You chose an invalid color, can't continue.", Toast.LENGTH_LONG).show();
				}
			}	
		}
	}
	
	
	
	private class LaunchIntentListener implements OnClickListener {
		private Context context;
		private Class launchActivityClass;
		private Bundle bundle;
		
		public LaunchIntentListener(Context context, Class launchActivityClass, Bundle bundle) {
			this.context = context;
			this.launchActivityClass = launchActivityClass;
			this.bundle = bundle;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context,launchActivityClass);
			intent.putExtras(bundle);
			int requestCode = 0;
			
			if (launchActivityClass.equals(RecordSoundActivity.class)) {
				requestCode = RecordSoundActivity.REQUEST_CODE;
			}
			else if (launchActivityClass.equals(FilePickerActivity.class)) {
				requestCode = FilePickerActivity.REQUEST_CODE;
			}
			else if (launchActivityClass.equals(ColorPickerActivity.class)) {
				requestCode = ColorPickerActivity.REQUEST_CODE;
			}
			
			startActivityForResult(intent, requestCode);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Bundle returnedBundle = data.getExtras();
			if (returnedBundle != null) {
				if (requestCode == ColorPickerActivity.REQUEST_CODE && resultCode == ColorPickerActivity.COLOR_SELECTED) {
						String selectedColorString = returnedBundle.getString(ColorPickerActivity.COLOR_BUNDLE);
						try {
							// This will throw an exception if the color isn't valid
							int selectedColor = Color.parseColor(selectedColorString);
							colorSwatch.setBackgroundColor(selectedColor);
							tempTab.setBgColor(selectedColorString);
						} catch (IllegalArgumentException e) {
							Toast.makeText(this, "Invalid color returned from color picker, ignoring.", Toast.LENGTH_LONG).show();
						}
				}
			}
			else {
				// If no data is returned from the color picker, but the result is OK, it means the color is set to transparent (null)
				if (requestCode == ColorPickerActivity.REQUEST_CODE && resultCode == ColorPickerActivity.COLOR_SELECTED) {
					colorSwatch.setBackgroundColor(Color.TRANSPARENT);
					tempTab.setBgColor(null);
				}
			}
		}
	}

}
