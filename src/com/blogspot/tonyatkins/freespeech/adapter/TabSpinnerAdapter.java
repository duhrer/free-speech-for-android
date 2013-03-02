package com.blogspot.tonyatkins.freespeech.adapter;

import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.tonyatkins.freespeech.R;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class TabSpinnerAdapter extends SetSpinnerAdapter {
	public TabSpinnerAdapter(Context context, Set<Tab> data) {
		super(context, data);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Tab tab = (Tab) getItem(position);
		LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.tab_list_entry, parent, false);

		TextView textView = (TextView) view.findViewById(R.id.tab_list_entry);
		textView.setText(tab.getLabel());
		
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position,convertView,parent);
	}
}
