/**
 * Copyright 2011 Tony Atkins <duhrer@gmail.com>. All rights reserved.
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
 */
package com.blogspot.tonyatkins.freespech.storage;

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
