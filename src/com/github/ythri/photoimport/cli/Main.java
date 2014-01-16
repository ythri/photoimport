package com.github.ythri.photoimport.cli;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;

import com.beust.jcommander.JCommander;

import com.github.ythri.photoimport.config.Configuration;
import com.github.ythri.photoimport.config.ConfigManager;
import com.github.ythri.photoimport.core.ImportSource;
import com.github.ythri.photoimport.core.ImportGroup;
import com.github.ythri.photoimport.core.CopyTask;

/**
 * Command line interface for the PhotoImport. The command line interface takes 
 * various command line arguments and operates based on these.
 * @author Andreas Ecke
 */
public class Main {
	private static final Logger log = Logger.getLogger(Main.class.getName());

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
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new Formatter() {
			public String format(LogRecord rec) {
				if (rec.getThrown() != null) {
					return String.format("[%tT] %s: %s%n%s%n", new Date(rec.getMillis()), rec.getLevel().getName(), formatMessage(rec), rec.getThrown().toString());
				} else {
					return String.format("[%tT] %s: %s%n", new Date(rec.getMillis()), rec.getLevel().getName(), formatMessage(rec));
				}
			}
		});
		Logger.getLogger("com.github.ythri.photoimport").addHandler(handler);
		Logger.getLogger("com.github.ythri.photoimport").setUseParentHandlers(false);
		log.info("PhotoImport " + version);

		// parse command line arguments
		log.info("Parsing command line arguments");
		CommandLineArguments arguments = new CommandLineArguments();
		JCommander commander = new JCommander(arguments);
		commander.setProgramName("photoimport");
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
		ConfigManager configManager = new ConfigManager();
		Configuration config = configManager.load(arguments.getConfigFile());
		if (config == null) {
			log.severe("Configuration error");
			System.exit(2);
		}
		List<String> targets = (arguments.getTargets().size() == 0) ? new ArrayList<String>(config.targets.keySet()) : arguments.getTargets();
		if (configManager.isValid(config, targets)) {
			// todo: extensions always lowercase
			Set<String> extensions = configManager.getExtensions(config, targets);
			Set<String> variables = configManager.getVariables(config, targets);
			log.info("Extensions: " + extensions.toString());

			// identify all used variables and remove those already assigned and file properties
			log.info("Variables: " + variables.toString());
			String[] properties = { "year", "month", "day", "hour", "minute", "second", "monthname", "dayname", 
				"monthshortname", "dayshortname", "filename", "dcfpathnumber", "dcffilenumber", "dcfnumber" };
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
			if (config.options != null) {
				ImportGroup.setLocale(config.options.getLocale());
				ImportGroup.getExifFromExtensions(config.options.readexiffrom);
			}
			log.info("Reading source directory");
			ImportSource finder = new ImportSource();
			finder.setSourceConfig(config.source);
			finder.setExtensionFilter(extensions);
			finder.setDcfNumberFilter(arguments.getMin(), arguments.getMax());
			finder.setDateFilter(arguments.getBegin(), arguments.getEnd());
			List<ImportGroup> files = finder.findPhotos(); //, arguments.getFrom(), arguments.getTo());
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
