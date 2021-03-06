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
package com.blogspot.tonyatkins.freespeech.adapter;

import java.util.Set;
import java.util.TreeSet;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.blogspot.tonyatkins.freespeech.model.PerceivedColor;
import com.blogspot.tonyatkins.freespeech.view.ColorSwatch;
import com.blogspot.tonyatkins.freespeech.activity.ColorPickerActivity;
public class ColorWheelListAdapter implements ListAdapter {
	private int selectedColor = Color.TRANSPARENT;
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
	
	public int getCount() {
		return colors.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		PerceivedColor color = (PerceivedColor) colors.toArray()[position];
		
		ColorSwatch colorSwatch = new ColorSwatch(activity,color.toString());
		colorSwatch.setMinimumHeight(20);
		
		colorSwatch.setOnClickListener(new PickColorListener(activity,color.getColor()));
		
		if (selectedColor == color.getColor()) {
			colorSwatch.setSelected(true);
		}

		return colorSwatch;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		return false;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	private class PickColorListener implements OnClickListener {
		private ColorPickerActivity activity;
		private int color;
		
		public PickColorListener(ColorPickerActivity activity, int color) {
			super();
			this.activity = activity;
			this.color = color;
		}

		public void onClick(View v) {
			activity.setSelectedColor(color);
			if (v instanceof ColorSwatch) {
				v.setSelected(true);
				v.invalidate();
			}
		}
	}

	public int getSelectedColor() {
		return selectedColor;
	}

	public void setSelectedColor(int selectedColor) {
		this.selectedColor = selectedColor;
	}
}
