package com.blogspot.tonyatkins.freespeech.listeners;

import android.content.ClipData;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.blogspot.tonyatkins.freespeech.model.SoundButton;

public class DragLongClickListener implements OnLongClickListener {
	private final SoundButton button;
	
	public DragLongClickListener(SoundButton button) {
		super();
		this.button = button;
	}

	@Override
	public boolean onLongClick(View v) {
		View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
		ClipData data = ClipData.newPlainText("id",String.valueOf(button.getId()));
		v.startDrag(data, shadowBuilder, button, 0);

		return true;
	}

}
