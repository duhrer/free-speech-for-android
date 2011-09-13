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
package com.blogspot.tonyatkins.myvoice.model;

import java.util.Set;
import java.util.TreeSet;

import android.database.DataSetObserver;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.blogspot.tonyatkins.myvoice.activity.ColorPickerActivity;
import com.blogspot.tonyatkins.myvoice.view.ColorSwatch;
public class ColorWheelListAdapter implements ListAdapter {
	private String selectedColor = null;
	private ColorPickerActivity activity;
	private Set<PerceivedColor> colors = new TreeSet<PerceivedColor>();
	
	public ColorWheelListAdapter(ColorPickerActivity activity) {
		this.activity = activity;
		
		initialize();
	}

	private void initialize() {
		// go through and mix red and blue on the y axis and add green on the x axis in batches of six until you're finished
		for (int r = 0; r <= 255; r+=51) {
			for (int b = 0; b <= 255; b+=51) {
				for (int g = 0; g <=255; g+=51) {
					colors.add(new PerceivedColor(r,g,b));
				}
			}
		}
	}
	
	@Override
	public int getCount() {
		return colors.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PerceivedColor color = (PerceivedColor) colors.toArray()[position];
		
		ColorSwatch colorSwatch = new ColorSwatch(activity,color.toString());
		colorSwatch.setMinimumHeight(20);
		
		colorSwatch.setOnClickListener(new PickColorListener(activity,color.toString()));
		
		// highlight the selected color swatch
		if (selectedColor != null) {
			if (selectedColor.equals(color.toString())) {
				colorSwatch.setSelected(true);
			}
		}
		
		return colorSwatch;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	private class PickColorListener implements OnClickListener {
		private ColorPickerActivity activity;
		private String colorString;
		
		public PickColorListener(ColorPickerActivity activity, String colorString) {
			super();
			this.activity = activity;
			this.colorString = colorString;
		}

		@Override
		public void onClick(View v) {
			activity.setSelectedColor(colorString);
			if (v instanceof ColorSwatch) {
				((ColorSwatch) v).setSelected(true);
				v.invalidate();
			}
		}
	}

	public String getSelectedColor() {
		return selectedColor;
	}

	public void setSelectedColor(String selectedColor) {
		this.selectedColor = selectedColor;
	}
}
