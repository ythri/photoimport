package com.github.ythri.photoimport.core;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;

/**
 * This class is responsible for formatting path and file names from the configuration files by 
 * replacing all variables with the actual values. Variables in the configuration path names have 
 * the form {@code \{VarName\}}. The values for the variables come from two sources: First those 
 * variables that are read from the files that were found (like the date from its EXIF data or the 
 * file name), and some values are given as command line options or queried during from the CLI: 
 * those variables are generally custom variables like {@code EventName} or {@code Client}.
 * <p>
 * Since the custom variables are fixed during one run of the application, while the file specific 
 * variables can change values for each file, the path formatter will take the custom variables 
 * in the constructor and the file specific variables, as well as the path, in the actual 
 * {@link #format(String,ImportGroup)} method.
 */
public class PathFormatter {
	private Map<String, String> variables;
	private Pattern replacer;

	public PathFormatter(Map<String, String> variables) {
		this.variables = variables;
		replacer = Pattern.compile("\\{(\\w+)\\}");
	}

	public String format(String pattern, ImportGroup group) {
		Matcher m = replacer.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String var = m.group(1).toLowerCase();
			// m.appendReplacement(sb, variables.containsKey(var) ? variables.get(var) : group.getProperty(var));
			if (variables.containsKey(var)) {
				m.appendReplacement(sb, variables.get(var));
			} else {
				m.appendReplacement(sb, group.getProperty(var));
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
