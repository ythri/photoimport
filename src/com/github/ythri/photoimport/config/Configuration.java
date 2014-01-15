package com.github.ythri.photoimport.config;

import java.util.Map;
import java.util.HashMap;

public class Configuration {
	public SourceConfig source;
	public Map<String, TargetConfig> targets = new HashMap<String, TargetConfig>();
}
