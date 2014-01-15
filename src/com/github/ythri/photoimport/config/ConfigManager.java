package com.github.ythri.photoimport.config;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.JsonParser;

public class ConfigManager {
	private static final Logger log = Logger.getLogger(ConfigManager.class.getName());

	private ObjectMapper mapper;

	public ConfigManager() {
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		// mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	}

	public Configuration load(String fileName) {
		try {
			Configuration config = mapper.readValue(new File(fileName), Configuration.class);
			if (config != null && config.options != null) {
				Suffix suffix = config.options.suffix;
				if (suffix != null) {
					for (TargetConfig target : config.targets.values()) {
						if (target.suffix == null) {
							target.suffix = suffix;
						}
					}
				}
			}
			return config;
		} catch (IOException e) {
			log.log(Level.WARNING, "Unable to load configuration file.", e);
			return null;
		}
	}

	public Set<String> getExtensions(Configuration config, List<String> activeTargets) {
		Set<String> extensions = new HashSet<String>();
		for (String target : activeTargets) {
			extensions.addAll(config.targets.get(target).extensions);
		}
		return extensions;
	}

	public Set<String> getVariables(Configuration config, List<String> activeTargets) {
		Set<String> variables = new HashSet<String>();
		for (String targetName : activeTargets) {
			TargetConfig target = config.targets.get(targetName);
			addVariables(target.path, variables);
			addVariables(target.file, variables);
		}
		return variables;
	}

	private void addVariables(String path, Set<String> variables) {
		final Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
		Matcher m = pattern.matcher(path);
		while (m.find()) {
			variables.add(m.group(1).toLowerCase());
		}
	}

	public boolean isValid(Configuration config, List<String> activeTargets) {
		boolean valid = true;

		// check source declaration
		if (config.source == null || config.source.path == null || config.source.path == "") {
			log.warning("No source path given in the configuration file.");
			valid = false;
		} else {
			File sourcepath = new File(config.source.path);
			if (!sourcepath.exists() || !sourcepath.isDirectory()) {
				log.warning("Source path does not exist or is not a valid directory.");
				valid = false;
			}
		}

		// check active targets 
		for (String key : activeTargets) {
			if (!config.targets.containsKey(key)) {
				log.warning("Target " + key + " does not exist.");
				valid = false;
			} else {
				TargetConfig target = config.targets.get(key);
				// check root directory
				if (target.root == null || target.root == "") {
					log.warning("Target " + key + " does not specify a root directory.");
					valid = false;
				} else {
					File rootpath = new File(target.root);
					if (!rootpath.exists() || !rootpath.isDirectory()) {
						log.warning("Target " + key + " root path does not exist or is not a valid directory.");
						valid = false;
					}
				}

				// check filename
				if (target.file == null || target.file == "") {
					log.warning("Target " + key + " does not specify a file name.");
					valid = false;
				}

				// check extensions
				if (target.extensions == null || target.extensions.size() == 0) {
					log.warning("Target " + key + " does not specify any extensions.");
					valid = false;
				} else {
					for (String ext : target.extensions) {
						if (!ext.matches("\\w+")) {
							log.warning(ext + " in target " + key + " is not a valid file extension.");
							valid = false;
						}
					}
				}
			}
		}
		return valid;
	}
}
