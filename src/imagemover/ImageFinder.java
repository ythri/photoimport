package imagemover;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Logger;

import java.io.File;
import java.io.FileFilter;

import imagemover.config.Source;

/**
 * 
 */
public class ImageFinder {
	private FileFilter normalFileFilter;
	private FileFilter normalDirectoryFilter;
	private FileFilter dcfFileFilter;
	private FileFilter dcfDirectoryFilter;

	private class CustomFileFilter implements FileFilter {
		private Pattern regex;
		private Set<String> extensions;
		public CustomFileFilter(Pattern regex, Set<String> extensions) {
			this.regex = regex;
			this.extensions = extensions;
		}
		public boolean accept(File file) {
			return file.isFile() && (regex == null || regex.matcher(file.getName()).matches()) && extensions.contains(getExtension(file).toLowerCase());
		}
	}

	private class CustomDirectoryFilter implements FileFilter {
		private Pattern regex;
		public CustomDirectoryFilter(Pattern regex) {
			this.regex = regex;
		}
		public boolean accept(File file) {
			return file.isDirectory() && (regex == null || regex.matcher(file.getName()).matches());
		}
	}

	/*
	 * Creates a new image finder that can look for files with the given extensions.
	 */
	public ImageFinder(Set<String> extensions) {
		dcfDirectoryFilter = new CustomDirectoryFilter(Pattern.compile("[0-9]{3}[0-9A-Z_]{5}"));
		dcfFileFilter = new CustomFileFilter(Pattern.compile("[0-9A-Z_]{4}[0-9]{4}\\.[0-9A-Z_]+"), extensions);
		normalDirectoryFilter = new CustomDirectoryFilter(null);
		normalFileFilter = new CustomFileFilter(null, extensions);
	}

	/*
	 * Reads the source directory and returns a list of all images. If specified, the images are grouped together (images with the
	 * the filename in the same folder, but different extensions, belong to the same group), otherwise the are all returned individually.
	 */
	public List<ImageGroup> findFiles(Source sourceConfig) {
		Logger log = Logger.getLogger("imagemover");

		File root = new File(sourceConfig.path);
		List<ImageGroup> imageGroups = new ArrayList<ImageGroup>();
		if (sourceConfig.type == Source.SearchMode.dcf) {
			File dcim = new File(root, "DCIM");
			if (dcim.exists() && dcim.isDirectory()) {
				for (File subdir : dcim.listFiles(dcfDirectoryFilter)) {
					addFilesFromCurrentDirectory(subdir, sourceConfig.groups, imageGroups, true);
				}
			} else {
				log.warning("DCIM directory does not exist");
			}
		} else if (sourceConfig.type == Source.SearchMode.single) {
			addFilesFromCurrentDirectory(root, sourceConfig.groups, imageGroups, false);
		} else {
			addFilesRecursively(root, sourceConfig.groups, imageGroups);
		}
		if (imageGroups.size() == 0) {
			log.warning("No images found in source directory");
		}
		return imageGroups;
	}

	private void addFilesRecursively(File directory, boolean group, List<ImageGroup> imageGroups) {
		addFilesFromCurrentDirectory(directory, group, imageGroups, false);
		for (File subdir : directory.listFiles(normalDirectoryFilter)) {
			addFilesRecursively(subdir, group, imageGroups);
		}
	}

	private void addFilesFromCurrentDirectory(File directory, boolean group, List<ImageGroup> imageGroups, boolean dcf) {
		FileFilter fileFilter = dcf ? dcfFileFilter : normalFileFilter;
		if (group) {
			Map<String,List<File>> groups = new HashMap<String,List<File>>();
			for (File file : directory.listFiles(fileFilter)) {
				String baseName = getBaseName(file);
				if (!groups.containsKey(baseName)) {
					groups.put(baseName, new ArrayList<File>());
				}
				groups.get(baseName).add(file);
			}
			for (List<File> imageGroup : groups.values()) {
				if (dcf) {
					File f = imageGroup.get(0);
					imageGroups.add(new ImageGroup(imageGroup, f.getParentFile().getName().substring(0,3), f.getName().substring(4,8)));
				} else {
					imageGroups.add(new ImageGroup(imageGroup));
				}
			}
		} else {
			for (File file : directory.listFiles(fileFilter)) {
				imageGroups.add(new ImageGroup(file));
			}
		}
	}

	private String getBaseName(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? fileName : fileName.substring(0, position);
	}

	private String getExtension(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? "" : fileName.substring(position + 1);
	}
}
