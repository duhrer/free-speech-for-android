package com.blogspot.tonyatkins.freespeech.listeners;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.os.Build;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.blogspot.tonyatkins.freespeech.model.HasId;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DragLongClickListener implements OnLongClickListener {
	private final HasId hasId;
	
	public DragLongClickListener(HasId hasId) {
		super();
		this.hasId = hasId;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onLongClick(View v) {
		View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
		ClipData data = ClipData.newPlainText("id",String.valueOf(hasId.getId()));
		v.startDrag(data, shadowBuilder, hasId, 0);

		return true;
	}
}
