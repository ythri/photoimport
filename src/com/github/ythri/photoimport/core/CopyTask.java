package com.github.ythri.photoimport.core;

import com.github.ythri.photoimport.config.TargetConfig;

import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class CopyTask {
	private static final Logger log = Logger.getLogger(CopyTask.class.getName());

	private PathFormatter formatter;
	private TargetConfig target;

	public CopyTask(TargetConfig target, Map<String, String> variables) {
		this.target = target;
		formatter = new PathFormatter(variables);
	}

	public void copyFiles(List<ImportGroup> files) {
		for (ImportGroup group : files) {
			File path = new File(target.root, formatter.format(target.path, group));
			String file = formatter.format(target.file, group);
			if (!path.exists()) {
				log.info("Creating directory " + path.toString());
				path.mkdirs();
			}

			// find common suffix for all files
			int suffix = (target.suffix.alwaysAppend) ? 1 : 0;
			while (!checkIfFilenameIsFree(path, file + target.suffix.format(suffix), group, target.subfolders)) {
				suffix++;
			}

			// copy files
			for (File from : group.getFiles()) {
				String ext = FileUtils.getExtension(from).toLowerCase();
				File fullPath = path;
				if (target.subfolders.containsKey(ext)) {
					fullPath = new File(path, target.subfolders.get(ext));
					if (!fullPath.exists()) {
						log.info("Creating directory " + fullPath.toString());
						fullPath.mkdirs();
					}
				}
				File to = new File(fullPath, file + target.suffix.format(suffix) + "." + FileUtils.getExtension(from));
				log.info("Copying file " + from.toString() + " to " + to.toString());
				try {
					copyFile(from, to);
					if (target.verify) {
						long c1 = FileUtils.checksum(from);
						long c2 = FileUtils.checksum(to);
						if (c1 == -1L || c2 == -1L) {
							log.warning("Could not verify files " + from + " and " + to + " as the checksum computation failed");
						} else if (c1 != c2) {
							log.warning("Files " + from + " and " + to + " have different checksums");
						}
					}
				} catch (IOException e) {
					log.log(Level.WARNING, "Could not move file", e);
				}
			}
		}
	}

	private boolean checkIfFilenameIsFree(File path, String fileName, ImportGroup group, Map<String, String> subfolders) {
		for (File from : group.getFiles()) {
			String ext = FileUtils.getExtension(from).toLowerCase();
			File to = new File(subfolders.containsKey(ext) ? new File(path, subfolders.get(ext)) : path, fileName + "." + FileUtils.getExtension(from));
			if (to.exists()) {
				return false;
			}
		}
		return true;
	}

	private void copyFile(File sourceFile, File destFile) throws IOException {
		if (destFile.exists()) {
			log.severe(destFile.toString() + " already exists, should not occur; skipping copy.");
			return;
		}
		destFile.createNewFile();

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			// destination.transferFrom(source, 0, source.size());
			long count = 0;
			long size = source.size();
			while((count += destination.transferFrom(source, count, size-count))<size);
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}
}
