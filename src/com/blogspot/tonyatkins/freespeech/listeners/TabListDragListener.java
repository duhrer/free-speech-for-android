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
import android.widget.ListView;

import com.blogspot.tonyatkins.freespeech.adapter.SortTabListAdapter;
import com.blogspot.tonyatkins.freespeech.db.DbAdapter;
import com.blogspot.tonyatkins.freespeech.model.Tab;

public class TabListDragListener implements OnDragListener {
	private static final int LOWLIGHT_COLOR = Color.GRAY;
	private static final int HIGHLIGHT_COLOR = Color.GREEN;
	private final Tab tab;
	private final Activity activity;
	private final ListView listView;
	private final DbAdapter dbAdapter;
	private float yPos = 0;

	public TabListDragListener(Tab tab, Activity activity, DbAdapter dbAdapter, ListView listView) {
		this.tab = tab;
		this.activity = activity;
		this.dbAdapter = dbAdapter;
		this.listView = listView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onDrag(View view, DragEvent event) {
		final int action = event.getAction();

		Tab draggedTab = (Tab) event.getLocalState();

		switch (action)
		{
		case DragEvent.ACTION_DRAG_ENDED:
			// Someone let go of a dragged view. Reset my background
			view.getBackground().clearColorFilter();
			view.invalidate();
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			// A view has entered my air space.

			// If the button isn't "me", change the background
			if (draggedTab.getId() != tab.getId())
			{
				view.getBackground().setColorFilter(HIGHLIGHT_COLOR, Mode.MULTIPLY);
				view.invalidate();
			}

			break;
		case DragEvent.ACTION_DRAG_EXITED:
			// A view has left my air space
			view.getBackground().setColorFilter(LOWLIGHT_COLOR, Mode.MULTIPLY);
			view.invalidate();
			break;
		case DragEvent.ACTION_DRAG_LOCATION:
			// Used for awareness of stuff that's being dragged
			yPos = event.getY();
			break;
		case DragEvent.ACTION_DRAG_STARTED:
			// Someone has picked a view up
			view.getBackground().setColorFilter(LOWLIGHT_COLOR, Mode.MULTIPLY);
			view.invalidate();
			break;
		case DragEvent.ACTION_DROP:
			// Someone has dropped a view on me.

			// Only reorder if I was dropped on a different button
			if (draggedTab.getId() != tab.getId())
			{
				// Go through the list of buttons and determine the new order.
				// basically, we need to set our new order, increment anything
				// higher, and fill in the hole we left
				int droppedSortOrder = tab.getSortOrder();
				int newDraggedSortOrder = yPos > (view.getHeight() / 2) ? droppedSortOrder + 1 : droppedSortOrder - 1;
				draggedTab.setSortOrder(newDraggedSortOrder);
				dbAdapter.updateTab(draggedTab);

				Cursor tabCursor = dbAdapter.fetchAllTabsAsCursor();
				tabCursor.moveToPosition(-1);
				while (tabCursor.moveToNext())
				{
					Tab sortTab = dbAdapter.extractTabFromCursor(tabCursor);
					if (sortTab.getId() != draggedTab.getId())
					{
						if (sortTab.getSortOrder() <= newDraggedSortOrder)
						{
							sortTab.setSortOrder(sortTab.getSortOrder() - 1);
						}
						else
						{
							sortTab.setSortOrder(sortTab.getSortOrder() + 1);
						}
						dbAdapter.updateTab(sortTab);
					}
				}

				Cursor cursor = dbAdapter.fetchAllTabsAsCursor();
				listView.setAdapter(new SortTabListAdapter(activity, cursor, dbAdapter));
			}

			break;
		}

		return true;
	}
}
