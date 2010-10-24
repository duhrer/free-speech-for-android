package com.blogspot.tonyatkins.myvoice.locale;

import java.util.Locale;

public class LocaleBuilder {
	public static Locale localeFromString(String localeString) {
		String[] localeParts = localeString.split("-");
		Locale locale;
		if (localeParts.length == 1){
			locale = new Locale(localeParts[0]);
		}
		else if (localeParts.length == 2) {
			locale = new Locale(localeParts[0],localeParts[1]);
		}
		else if (localeParts.length == 3) {
			locale = new Locale(localeParts[0],localeParts[1], localeParts[2]);
		}
		else {
			locale = Locale.US;
		}
		
		return locale;
	}
}
