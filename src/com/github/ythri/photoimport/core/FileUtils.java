package com.github.ythri.photoimport.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.CRC32;

public final class FileUtils {
	private static final Logger log = Logger.getLogger(FileUtils.class.getName());

	private FileUtils() {}

	public static String getBaseName(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? fileName : fileName.substring(0, position);
	}

	public static String getExtension(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? "" : fileName.substring(position + 1);
	}

	public static long checksum(File file) {
		CRC32 crc = new CRC32();
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int bytesRead;
			while((bytesRead = in.read(buffer)) != -1) {
				crc.update(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			crc.reset();
			log.log(Level.WARNING, "Error computing the crc of file " + file.toString(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
		return crc.getValue();
	}
}