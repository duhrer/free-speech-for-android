package org.blogspot.tonyatkins.pictureboard.storage;

import org.blogspot.tonyatkins.pictureboard.R;
import org.blogspot.tonyatkins.pictureboard.ViewBoardActivity;
import org.blogspot.tonyatkins.pictureboard.R.drawable;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StorageUnavailableReceiver extends BroadcastReceiver {
	private static final int PICTURE_BOARD_STORAGE_ERROR_ID = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

		int icon = R.drawable.icon;
		CharSequence tickerText = "Hello";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = "My notification";
		CharSequence contentText = "Hello World!";
		Intent notificationIntent = new Intent(context, ViewBoardActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(PICTURE_BOARD_STORAGE_ERROR_ID, notification);
		
		// If we were launched from an activity (and we always should be), kill it when storage becomes unavailable.
		if (context instanceof Activity) {
			((Activity) context).finish();
		}
	}

}
