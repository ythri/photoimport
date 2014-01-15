package com.github.ythri.photoimport.core;

import com.github.ythri.photoimport.config.SourceConfig;

import java.io.File;
import java.io.FileFilter;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ImportSource {
	private static final Logger log = Logger.getLogger(ImportSource.class.getName());

	private File path = null;
	private SourceConfig.SearchMode searchMode = null;
	private Set<String> extensions = null;
	private Date begin = null;
	private Date end = null;
	private Integer min = null;
	private Integer max = null;
	private FileFilter fileFilter = new ImportFileFilter();
	private FileFilter dirFilter = new ImportDirectoryFilter();

	public void setSourceConfig(SourceConfig config) {
		path = new File(config.path);
		searchMode = config.searchMode;
	}

	/**
	 * Makes sure the import source only returns files with the specified file extensions. If the 
	 * extension filter was previously set, this method will override the old value. Note that all 
	 * extensions in this set need to be lower case, or this filter will not work.
	 * 
	 * @param extensions The set of allowed extensions. A value of {@code null} will disable the 
	 * extension filter.
	 */
	public void setExtensionFilter(Set<String> extensions) {
		this.extensions = extensions;
	}

	/**
	 * Sets the date filter, such that the method only returns files with a date and time between 
	 * {@code begin} and {@code end}. If the date filter was previously set, this method will 
	 * override the old interval.
	 * <p>
	 * Note that this filter does not speed up the process of searching files, since it needs to 
	 * check each file group for its datetime by reading its EXIF data. This is in contrast to the 
	 * {@link #setDcfNumberFilter(Integer,Integer)}.
	 *
	 * @param begin The minimal datetime the file should have. A value of {@code null} indicates 
	 * that there is no lower bound on the datetime.
	 * @param end The maximal datetime the file should have. A value of {@code null} indicates 
	 * that there is no upper bound on the datetime.
	 */
	public void setDateFilter(Date begin, Date end) {
		this.begin = begin;
		this.end = end;
	}

	/**
	 * Enables the filtering of files by their DCF number, only allowing DCF numbers between 
	 * {@code min} and {@code max}. This 7-digit number consists of the DCF directory number and 
	 * the DCF file number, e.g., the file DCIM/123FOLDR/FILE4567.JPG will have the DCF number 
	 * 1234567. If the number filter was previously set, this method will override the old 
	 * interval.
	 * <p>
	 * Note that this filter may speed up the file search process. A minimal DCF number of 1234567 
	 * will allow to skip any folders will DCF directory numbers less than 123, e.g., 101FOLDR.
	 *
	 * @param min The minimal DCF number the file should have. A value of {@code null} indicates 
	 * that there is no lower bound on the DCF number.
	 * @param max The maximal DCF number the file should have. A value of {@code null} indicates 
	 * that there is no upper bound on the DCF number.
	 */
	public void setDcfNumberFilter(Integer min, Integer max) {
		this.min = min;
		this.max = max;
	}

	/**
	 * Searches the source directory according to the search mode for all files that satisfy the 
	 * specified filters and returns a list of all these files grouped by their base name.
	 * 
	 * @return list of all file groups that were found
	 */
	public List<ImportGroup> findPhotos() {
		List<ImportGroup> importGroups = new ArrayList<ImportGroup>();

		if (searchMode == SourceConfig.SearchMode.dcf) {
			File dcim = new File(path, "DCIM");
			if (dcim.exists() && dcim.isDirectory()) {
				for (File subdir : dcim.listFiles(dirFilter)) {
					addFilesFromCurrentDirectory(subdir, importGroups);
				}
			} else {
				log.warning("DCIM directory does not exist");
			}
		} else if (searchMode == SourceConfig.SearchMode.single) {
			addFilesFromCurrentDirectory(path, importGroups);
		} else {
			addFilesRecursively(path, importGroups);
		}

		if (importGroups.size() == 0) {
			log.warning("No images found in source directory");
		}
		return importGroups;
	}

	private void addFilesRecursively(File directory, List<ImportGroup> importGroups) {
		addFilesFromCurrentDirectory(directory, importGroups);

		// recursively check all subdirectories
		for (File subdir : directory.listFiles(dirFilter)) {
			addFilesRecursively(subdir, importGroups);
		}
	}

	private void addFilesFromCurrentDirectory(File directory, List<ImportGroup> importGroups) {
		// group all matching files by their basename
		Map<String, List<File>> groups = new HashMap<String, List<File>>();
		for (File file : directory.listFiles(fileFilter)) {
			String baseName = FileUtils.getBaseName(file);
			if (!groups.containsKey(baseName)) {
				groups.put(baseName, new ArrayList<File>());
			}
			groups.get(baseName).add(file);
		}

		// check the date for each file group and add them to the list
		for (List<File> imageGroup : groups.values()) {
			ImportGroup group = new ImportGroup(imageGroup, searchMode == SourceConfig.SearchMode.dcf);
			Date dateTime = group.getDateTime();
			if (begin == null || dateTime.compareTo(begin) >= 0) {
				if (end == null || dateTime.compareTo(end) <= 0) {
					importGroups.add(group);
				}
			}
		}
	}

	private class ImportFileFilter implements FileFilter {
		private final Pattern dcfFilePattern = Pattern.compile("[0-9A-Z_]{4}[0-9]{4}\\.[0-9A-Z_]+");

		public boolean accept(File file) {
			if (file.isFile()) {
				if (extensions != null) {
					String ext = FileUtils.getExtension(file).toLowerCase();
					if (!extensions.contains(ext)) return false;
				} 
				if (searchMode == SourceConfig.SearchMode.dcf) {
					if (!dcfFilePattern.matcher(file.getName()).matches()) return false;
					if (min != null || max != null) {
						int dcfDirNumber = Integer.parseInt(file.getParentFile().getName().substring(0, 3));
						int dcfFileNumber = Integer.parseInt(file.getName().substring(4, 8));
						int dcfNumber = dcfDirNumber * 10000 + dcfFileNumber;
						if (min != null && dcfNumber < min) return false;
						if (max != null && dcfNumber > max) return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	private class ImportDirectoryFilter implements FileFilter {
		private final Pattern dcfDirPattern = Pattern.compile("[0-9]{3}[0-9A-Z_]{5}");

		public boolean accept(File file) {
			if (file.isDirectory()) {
				if (searchMode == SourceConfig.SearchMode.dcf) {
					if (!dcfDirPattern.matcher(file.getName()).matches()) return false;
					if (min != null || max != null) {
						int dcfDirNumber = Integer.parseInt(file.getName().substring(0, 3));
						if (min != null && dcfDirNumber < min.intValue() / 10000) return false;
						if (max != null && dcfDirNumber > max.intValue() / 10000) return false;
					}
				}
				return true;
			}
			return false;
		}
	}
}
