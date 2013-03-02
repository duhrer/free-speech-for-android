package com.blogspot.tonyatkins.freespeech.adapter;

import java.util.Set;

import com.blogspot.tonyatkins.freespeech.model.HasId;

import android.content.Context;
import android.database.DataSetObserver;
import android.widget.SpinnerAdapter;

public abstract class SetSpinnerAdapter implements SpinnerAdapter {
	protected final Context context;
	private final Set<? extends HasId> data;
	
	public SetSpinnerAdapter(Context context, Set<? extends HasId> data) {
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public HasId getItem(int position) {
		return (HasId) data.toArray()[position];
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
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
}
