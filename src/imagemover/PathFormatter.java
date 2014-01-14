package imagemover;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;

public class PathFormatter {
	private Map<String, String> variables;
	private Pattern replacer;

	public PathFormatter(Map<String,String> variables) {
		this.variables = variables;
		replacer = Pattern.compile("\\{(\\w+)\\}");
	}

	public String format(String pattern, ImageGroup images) {
		Matcher m = replacer.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, variables.containsKey(m.group(1).toLowerCase()) ? variables.get(m.group(1).toLowerCase()) : images.getProperty(m.group(1).toLowerCase()));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
