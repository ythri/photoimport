package com.github.ythri.photoimport.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class Option {
	public List<String> readexiffrom = new ArrayList<String>();
	public String locale;
	public Suffix suffix;

	public Locale getLocale() {
		if (locale == null) {
			return Locale.getDefault();
		} else if (locale.contains("_")) {
			String[] parts = locale.split("_");
			return new Locale(parts[0], parts[1]);
		} else {
			return new Locale(locale);
		}
	}
}
