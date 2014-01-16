package com.github.ythri.photoimport.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.CRC32;

/**
 * This class provides several utility functions related to files, like extracting the base name 
 * and extension of a file and computing its checksum. This class is static and cannot be 
 * instantiated.
 */
public final class FileUtils {
	private static final Logger log = Logger.getLogger(FileUtils.class.getName());

	private FileUtils() {}

	/**
	 * Computes the base name of a file, i.e., the name without its directories and without the 
	 * file extension.
	 * @param file The file to extract the base name from. If it is {@code null}, the function will 
	 * also return {@code null}.
	 * @return base name of the the file
	 */
	public static String getBaseName(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? fileName : fileName.substring(0, position);
	}

	/**
	 * Computes the extension of a file, i.e., the part of the file after the last {@code .} (dot). 
	 * If the file contains no {@code .}, or only as the first character, it will return the empty 
	 * string.
	 * @param file The file to extract the extension from. If it is {@code null}, the function will 
	 * also return {@code null}.
	 * @return extension of the the file
	 */
	public static String getExtension(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? "" : fileName.substring(position + 1);
	}

	/**
	 * Computes the checksum of the file using cyclic redundancy checks (CRC32). Files with the 
	 * exact same content always have the same checksum. If an error occurs while reading the file, 
	 * the method returns the value {@code -1}.
	 * @param file file, for which the CRC checksum is computed
	 * @return CRC checksum of the file
	 */
	public static long checksum(File file) {
		CRC32 crc = new CRC32();
		InputStream in = null;
		long value = -1L;
		try {
			in = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int bytesRead;
			while((bytesRead = in.read(buffer)) != -1) {
				crc.update(buffer, 0, bytesRead);
			}
			value = crc.getValue();
		} catch (Exception e) {
			log.log(Level.WARNING, "Error computing the crc of file " + file.toString(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
		return value;
	}
}