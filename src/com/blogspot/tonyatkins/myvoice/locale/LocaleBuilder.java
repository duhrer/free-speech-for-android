/**
 * Copyright (C) 2011 Tony Atkins <duhrer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
