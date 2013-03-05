/**
 * Copyright 2012-2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Tony Atkins ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Tony Atkins OR
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
package com.blogspot.tonyatkins.freespeech.listeners;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Build;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.GridView;

import com.blogspot.tonyatkins.freespeech.adapter.SortButtonListAdapter;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.model.SoundButton;
import com.blogspot.tonyatkins.freespeech.view.SoundButtonView;

public class GridDragListener implements OnDragListener {
	private static final int LOWLIGHT_COLOR = Color.GRAY;
	private static final int HIGHLIGHT_COLOR = Color.GREEN;
	private final SoundButton button;
	private final Activity activity;
	private final GridView gridView;
	private final DbAdapter dbAdapter;
	private float xPos = 0;
	private float yPos = 0;
	
	public GridDragListener(SoundButton button, Activity activity, DbAdapter dbAdapter, GridView gridView) {
		this.button = button;
		this.activity = activity;
		this.dbAdapter = dbAdapter;
		this.gridView = gridView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onDrag(View view, DragEvent event) {
		final int action = event.getAction();

		SoundButton draggedButton = (SoundButton) event.getLocalState();

		
		switch (action) {
			case DragEvent.ACTION_DRAG_ENDED:
				// Someone let go of a dragged view.  Reset my background

				((SoundButtonView) view).setButtonBackgroundColor(button.getBgColor());
				view.invalidate();
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				// A view has entered my air space.

				// If the button isn't "me", change the background
				if (draggedButton.getId() != button.getId()) {
					view.getBackground().setColorFilter(HIGHLIGHT_COLOR,Mode.MULTIPLY);
					view.invalidate();
				}
				
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				// A view has left my air space
				view.getBackground().setColorFilter(LOWLIGHT_COLOR,Mode.MULTIPLY);
				view.invalidate();
				break;
			case DragEvent.ACTION_DRAG_LOCATION:
				// Used for awareness of stuff that's being dragged
				xPos = event.getX();
				yPos = event.getY();
				break;
			case DragEvent.ACTION_DRAG_STARTED:
				// Someone has picked a view up
				view.getBackground().setColorFilter(LOWLIGHT_COLOR,Mode.MULTIPLY);
				view.invalidate();
				break;
			case DragEvent.ACTION_DROP:
				// Someone has dropped a view on me.
				
				// Only reorder if I was dropped on a different button
				if (draggedButton.getId() != button.getId()) {
					// Go through the list of buttons and determine the new order.  basically, we need to set our new order, increment anything higher, and fill in the hole we left
					int droppedSortOrder = button.getSortOrder();
					int newDraggedSortOrder = xPos > (view.getWidth()/2) ? droppedSortOrder + 1 : droppedSortOrder -1;
					if (gridView.getNumColumns() == 1) {
						newDraggedSortOrder = yPos > (view.getHeight()/2) ? droppedSortOrder + 1 : droppedSortOrder -1;
					}
					
					draggedButton.setSortOrder(newDraggedSortOrder);
					dbAdapter.updateButton(draggedButton);
					
					Cursor buttonCursor = dbAdapter.fetchButtonsByTab(draggedButton.getTabId());
					buttonCursor.moveToPosition(-1);
					while (buttonCursor.moveToNext()) {
						SoundButton sortButton = dbAdapter.extractButtonFromCursor(buttonCursor);
						if (sortButton.getId() != draggedButton.getId()) {
							if (sortButton.getSortOrder() <= newDraggedSortOrder) {
								sortButton.setSortOrder(sortButton.getSortOrder() - 1);
							}
							else {
								sortButton.setSortOrder(sortButton.getSortOrder() + 1);
							}
							dbAdapter.updateButton(sortButton);
						}
					}
					
					Cursor cursor = dbAdapter.fetchButtonsByTab(draggedButton.getTabId());
					gridView.setAdapter(new SortButtonListAdapter(activity,cursor,dbAdapter));
				}

				break;
		}
		
		return true;
	}
}
