package org.blogspot.tonyatkins.pictureboard.storage;

import android.content.Intent;
import android.content.IntentFilter;

public class StorageUnavailableFilter extends IntentFilter {
	public StorageUnavailableFilter() {
		super();
		initialize();
	}

	public StorageUnavailableFilter(IntentFilter o) {
		super(o);
		initialize();
	}

	public StorageUnavailableFilter(String action, String dataType)
			throws MalformedMimeTypeException {
		super(action, dataType);
		initialize();
	}

	public StorageUnavailableFilter(String action) {
		super(action);
		initialize();
	}

	private void initialize() {
		addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		addAction(Intent.ACTION_MEDIA_NOFS);
		addAction(Intent.ACTION_MEDIA_REMOVED);
		addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
	}
}
