package com.github.ythri.photoimport.config;

import java.util.List;
import java.util.ArrayList;

public class TargetConfig {
	public String root;
	public String path;
	public String file;

	public Boolean protect = false;
	public Boolean verify = true;

	public Suffix suffix = Suffix.defaultSuffix;
	public List<String> extensions = new ArrayList<String>();
}
