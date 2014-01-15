package com.github.ythri.photoimport.config;

public class Suffix {
	public String seperator = "-";
	public Integer digits = 1;
	public boolean alwaysAppend = false;

	public static final Suffix defaultSuffix = new Suffix();

	public String format(int number) {
		return (number <= 0) ? "" : seperator + String.format("%0" + digits + "d", number);
	}
}
