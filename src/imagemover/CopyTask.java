package imagemover;

import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.io.IOException;

import imagemover.config.Target;

public class CopyTask {
	PathFormatter formatter;
	Target target;

	public CopyTask(Target target, Map<String,String> variables) {
		this.target = target;
		formatter = new PathFormatter(variables);
	}

	public void copyFiles(List<ImageGroup> files) {
		Logger log = Logger.getLogger("imagemover");
		for (ImageGroup group : files) {
			File path = new File(target.root, formatter.format(target.path, group));
			String file = formatter.format(target.file, group);
			if (!path.exists()) {
				log.info("Creating directory " + path.toString());
				path.mkdirs();
			}
			// TODO: find common suffix for all files!
			for (File from : group.sourceFiles) {
				File to = new File(path, file + "." + getExtension(from));
				if (target.suffix.alwaysAppend || to.exists()) {
					int i = 1;
					do {
						to = new File(path, file + target.suffix.format(i) + "." + getExtension(from));
						i++;
					} while (to.exists());
				}
				log.info("Copying file " + from.toString() + " to " + to.toString());
				try {
					copyFile(from, to);
				} catch (IOException e) {
					log.log(Level.WARNING, "Could not move file", e);
				}
			}
		}
	}

	private void copyFile(File sourceFile, File destFile) throws IOException {
		if (destFile.exists()) {
			Logger.getLogger("imagemover").severe(destFile.toString() + " already exists, should not occur; skipping copy.");
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

	private String getExtension(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? "" : fileName.substring(position + 1);
	}
}
