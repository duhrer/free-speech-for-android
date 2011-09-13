/**
 * Copyright (C) 2011 Tony Atkins <duhrer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blogspot.tonyatkins.myvoice.listeners;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.view.View.OnClickListener;

public class ActivityQuitListener implements OnCancelListener, OnClickListener, android.content.DialogInterface.OnClickListener {
	private Activity activity;
	
	public ActivityQuitListener(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		activity.finish();
	}
	
	@Override
	public void onClick(View v) {
		activity.finish();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		activity.finish();
	}
}


