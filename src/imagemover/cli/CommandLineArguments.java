package imagemover.cli;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.beust.jcommander.Parameter;

/**
 * This class specifies the command line arguments of the command line interface.
 * These values get initialized by the JCommander library with the values of the 
 * actual command line arguments, or keep their default value if no matching argument
 * is provided.
 * @author Andreas Ecke
 */
class CommandLineArguments {
	/**
	 * Name of the configuration file that should be loaded and processed.
	 */
	@Parameter(names = { "-c", "--config" }, description = "Configuration file")
	private String config = "default.config";

	/**
	 * A set of variables assignments of the form "name=value". The command line can 
	 * contain multiple of these arguments, and each argument can contain a comma-
	 * separated list of assignments, checked by the {@link AssignmentValidator}.
	 */
	@Parameter(names = { "-v", "--variable" }, description = "Variable assignment of type \"Name=Value\"", validateWith = AssignmentValidator.class)
	private List<String> variables = new ArrayList<String>();

	/**
	 * A set of targets from the configuration file that should be executed. The command 
	 * line can contain multiple of these arguments, and each argument can contain a 
	 * comma-separated list of targets.
	 */
	@Parameter(names = { "-t", "--targets" }, description = "Comma-separated list of targets")
	private List<String> targets = new ArrayList<String>();

	/**
	 * The help argument that displays basic information about this program and its usage.
	 */
	@Parameter(names = { "-h", "--help" }, description = "Show this help", help = true)
	private Boolean help = false;

	@Parameter(names = "--from", description = "copy files from a DCF file system starting only at a given number")
	private Integer from = null;

	@Parameter(names = "--to", description = "copy files from a DCF file system up to a given number")
	private Integer to = null;

	/**
	 * Returns the name of the configuration file that should be loaded and processed.
	 * @return name of the configuration file
	 */
	public String getConfigFile() {
		return config;
	}

	/**
	 * Returns a map of assigned variables. For each assignment "name=value" given as
	 * a parameter, the map will contain an entry "value" for the key "name".
	 * @return map of assigned variables 
	 */
	public Map<String, String> getVariables() {
		Map<String, String> vars = new HashMap<String, String>();
		for (String assignment : variables) {
			String[] keyValue = assignment.split("=", 2);
			vars.put(keyValue[0].toLowerCase(), keyValue[1]);
		}
		return vars;
	}

	/**
	 * Returns the list of all targets from the configuration file that should be executed.
	 * @return list of active target names
	 */
	public List<String> getTargets() {
		return targets;
	}

	/**
	 * Checks if the help argument was given and thus the usage should be displayed.
	 * @return true, if usage should be displayed, otherwise false
	 */
	public boolean isHelpNeeded() {
		return help;
	}

	public Integer getFrom() {
		return from;
	}

	public Integer getTo() {
		return to;
	}
}
