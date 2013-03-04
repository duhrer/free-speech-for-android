package com.blogspot.tonyatkins.freespeech.listeners;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
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
			view.setBackgroundColor(Color.TRANSPARENT);
			view.invalidate();
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			// A view has entered my air space.

			// If the button isn't "me", change the background
			if (draggedTab.getId() != tab.getId())
			{
				view.setBackgroundColor(HIGHLIGHT_COLOR);
				view.invalidate();
			}

			break;
		case DragEvent.ACTION_DRAG_EXITED:
			// A view has left my air space
			view.setBackgroundColor(LOWLIGHT_COLOR);
			view.invalidate();
			break;
		case DragEvent.ACTION_DRAG_LOCATION:
			// Used for awareness of stuff that's being dragged
			yPos = event.getY();
			break;
		case DragEvent.ACTION_DRAG_STARTED:
			// Someone has picked a view up
			view.setBackgroundColor(LOWLIGHT_COLOR);
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
