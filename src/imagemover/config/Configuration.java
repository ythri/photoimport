package imagemover.config;

import java.util.Map;
import java.util.HashMap;

public class Configuration {
	public Source source;
	public Map<String, Target> targets = new HashMap<String, Target>();
}
