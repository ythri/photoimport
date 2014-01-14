package imagemover.cli;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;

import com.beust.jcommander.JCommander;

import imagemover.config.Configuration;
import imagemover.config.ConfigurationManager;
import imagemover.ImageFinder;
import imagemover.ImageGroup;
import imagemover.CopyTask;

/**
 * Command line interface for the ImageMover. The command line interface takes 
 * various command line arguments and operates based on these.
 * @author Andreas Ecke
 */
public class Main {
	/**
	 * Version string for the current version of this program.
	 */
	public static final String version = "0.1.0";

	/**
	 * Main method, that runs at startup, parses the command line arguments and 
	 * runs the specified tasks.
	 * @param args command line arguments
	 * @see CommandLineArguments
	 */
	public static void main(String... args) {
		// create logger
		Logger log = Logger.getLogger("imagemover");
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new Formatter() {
			public String format(LogRecord rec) {
				return String.format("[%tT] %s: %s%n", new Date(rec.getMillis()), rec.getLevel().getName(), formatMessage(rec));
			}
		});
		log.addHandler(handler);
		log.setUseParentHandlers(false);
		log.info("ImageMover " + version);

		// parse command line arguments
		log.info("Parsing command line arguments");
		CommandLineArguments arguments = new CommandLineArguments();
		JCommander commander = new JCommander(arguments);
		commander.setProgramName("imagemover");
		try {
			commander.parse(args);
		} catch (Exception e) {
			log.severe("Argument error: " + e.getMessage() + "\n");
			commander.usage();
			System.exit(1);
		}

		// display help
		if (arguments.isHelpNeeded()) {
			commander.usage();
			System.exit(0);
		}

		// Load the configuration file
		log.info("Reading configuration file");
		ConfigurationManager configManager = new ConfigurationManager();
		Configuration config = configManager.load(arguments.getConfigFile());
		if (config == null) {
			log.severe("Configuration error");
			System.exit(2);
		}
		List<String> targets = (arguments.getTargets().size() == 0) ? new ArrayList<String>(config.targets.keySet()) : arguments.getTargets();
		if (configManager.isValid(config, targets)) {
			Set<String> extensions = configManager.getExtensions(config, targets);
			Set<String> variables = configManager.getVariables(config, targets);
			log.info("Extensions: " + extensions.toString());

			// identify all used variables and remove those already assigned and file properties
			log.info("Variables: " + variables.toString());
			String[] properties = { "year", "month", "day", "hour", "minute", "second", "monthname", "dayname", 
				"monthshortname", "dayshortname", "filename", "dcfpathnumber", "dcffilenumber" };
			variables.removeAll(Arrays.asList(properties));
			variables.removeAll(arguments.getVariables().keySet());
			
			// read remaining variables
			Map<String, String> assignments = arguments.getVariables();
			if (variables.size() > 0) {
				Scanner scanner = new Scanner(System.in);
				for (String var : variables) {
					System.out.print(var + ": ");
 					assignments.put(var, scanner.nextLine());
				}
			}
			log.info(assignments.toString());

			// read source directory
			log.info("Reading source directory");
			ImageFinder finder = new ImageFinder(extensions);
			List<ImageGroup> files = finder.findFiles(config.source); //, arguments.getFrom(), arguments.getTo());
			Collections.sort(files);
			log.info("Found " + files.size() + " image groups.");

			// copy files for each active target
			for (String target : targets) {
				CopyTask task = new CopyTask(config.targets.get(target), assignments);
				task.copyFiles(files);
			}
		} else {
			log.severe("Configuration error");
			System.exit(2);
		}
	}
}
