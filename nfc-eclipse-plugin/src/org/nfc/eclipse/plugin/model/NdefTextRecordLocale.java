/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package org.nfc.eclipse.plugin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 * Utility to tighten up locale handling somewhat
 *  - map from locale to locale (en-gb to en-GB etc)
 *  - map from string to locale
 * 
 * @author thomas
 *
 */

public class NdefTextRecordLocale {

	/*
	Language identifiers as specified by RFC 3066, can have the form language, language-country, 
	language-country-variant and some other specialized forms. The guidelines for choosing between 
	language and language-country are ambiguous.
	*/
	private List<Locale> locales = new ArrayList<Locale>();
	private String[] stringLocales = new String[locales.size()];
	
	private Map<Locale, String> localeStringMap = new HashMap<Locale, String>();
	private Map<String, Locale> stringLocaleMap = new HashMap<String, Locale>();
	
	public NdefTextRecordLocale() {
		for(Locale locale : Locale.getAvailableLocales()) {
			addLocale(locale);
		}
		sortLocales();
	}
	
	private void addLocale(Locale locale) {
		this.locales.add(locale);
		
		String string = getLocaleString(locale);
		
		localeStringMap.put(locale, string);
		stringLocaleMap.put(string, locale);
		stringLocaleMap.put(string.toLowerCase(), locale);
		stringLocaleMap.put(string.toUpperCase(), locale);
	}
	
	public Locale getSubstitute(Locale locale) {
		return getLocaleFromString(getLocaleString(locale));
	}
	
	public static String getLocaleString(Locale locale) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		
		if((country == null || country.length() == 0) && (variant == null || variant.length() == 0)) {
			return language.toLowerCase();
		}
		
		if(variant == null || variant.length() == 0) {
			return language.toLowerCase() + "-" + country.toUpperCase();
		}
		
		return language.toLowerCase() + "-" + country.toUpperCase() + "-" + variant;
	}
	
	private void sortLocales() {
		Collections.sort(locales, new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				return localeStringMap.get(o1).compareTo(localeStringMap.get(o2));
			}
		});
		
		stringLocales = new String[locales.size()];
		for(int i = 0; i < locales.size(); i++) {
			stringLocales[i] = localeStringMap.get(locales.get(i));
		}

	}

	public String[] getStringLocales() {
		return stringLocales;
	}

	public Locale getLocaleFromString(String stringValue) {
		// accept both - and _
		if(stringValue.indexOf('_') != -1) {
			stringValue = stringValue.replace('_', '-');
		}
		
		Locale locale = stringLocaleMap.get(stringValue);

		if(locale == null) {
			locale = stringLocaleMap.get(stringValue.toLowerCase());
		}
		if(locale == null) {
			locale = stringLocaleMap.get(stringValue.toUpperCase());
		}
		return locale;
	}
	
	public int getIndex(Locale locale) {
		if(localeStringMap.containsKey(locale)) {
			return locales.indexOf(locale);
		}
		return -1;
	}

	public Locale get(int index) {
		return locales.get(index);
	}

	public int spawnIndex(Locale locale) {
		if(!localeStringMap.containsKey(locale)) {
			Locale substitute = getLocaleFromString(getLocaleString(locale));
			
			if(substitute != null) {
				return locales.indexOf(substitute);
			} else {
				// ups, have not seen this exact locale before? Add it now.
				addLocale(locale);
				
				sortLocales();
			}
		}
		return locales.indexOf(locale);
	}
}
