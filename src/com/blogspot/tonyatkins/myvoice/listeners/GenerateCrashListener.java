package com.blogspot.tonyatkins.myvoice.listeners;

import android.view.View;
import android.view.View.OnClickListener;

public class GenerateCrashListener implements OnClickListener {

	@Override
	public void onClick(View v) {
		int foo = 0;
		
		// this will throw a java.lang.ArithmeticException
		int bar = 1 / foo;
	}

}
