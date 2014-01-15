package com.github.ythri.photoimport.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class TargetConfig {
	public String root;
	public String path;
	public String file;

	public Boolean protect = false;
	public Boolean verify = true;

	public Suffix suffix;
	public List<String> extensions = new ArrayList<String>();
	public Map<String, String> subfolders = new HashMap<String, String>();
}
