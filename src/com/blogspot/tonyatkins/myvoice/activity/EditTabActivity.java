package com.blogspot.tonyatkins.myvoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blogspot.tonyatkins.myvoice.R;
import com.blogspot.tonyatkins.myvoice.db.DbAdapter;
import com.blogspot.tonyatkins.myvoice.model.Tab;

public class EditTabActivity extends Activity {
	public static final int ADD_TAB = 6;
	public static final int EDIT_TAB = 7;
	
	private Tab tempTab;
	private AlertDialog alertDialog;
	private DbAdapter dbAdapter;
	private boolean isNewTab = false;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		dbAdapter = new DbAdapter(this);
		
		Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setMessage("You must enter a label.");
		alertDialogBuilder.setTitle("Required Information Missing or Incorrect");
		alertDialog = alertDialogBuilder.create();
		
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
		
		// wire up the cancel button
		Button cancelButton = (Button) findViewById(R.id.buttonPanelCancelButton);
		cancelButton.setOnClickListener(new CancelListener());
		
		// wire up the "save" button
		Button saveButton = (Button) findViewById(R.id.buttonPanelSaveButton);
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
			if (tempTab.getLabel() == null || tempTab.getLabel().length() <= 0) {
					alertDialog.show();
			}
			else {
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
					Toast.makeText(context, "There was an error saving this tab.", Toast.LENGTH_LONG);
				}
			}	
		}
	}
}
