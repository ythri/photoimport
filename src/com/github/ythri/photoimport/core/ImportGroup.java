package com.github.ythri.photoimport.core;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.File;

import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImportGroup implements Comparable<ImportGroup> {
	private static final Logger log = Logger.getLogger(ImportGroup.class.getName());
	private static List<String> exifExtensions = null;

	public static void getExifFromExtensions(List<String> extensions) {
		exifExtensions = extensions;
	}

	private List<File> files;
	private Date dateTime;
	private Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Note: This methods expects all files to be in the same directory and have the same base name 
	 * (just the extension may differ).
	 */
	public ImportGroup(List<File> files, boolean dcf) {
		this.files = files;
		properties.put("filename", FileUtils.getBaseName(files.get(0)));
		if (dcf) {
			int dcfDirNumber = Integer.parseInt(files.get(0).getParentFile().getName().substring(0, 3));
			int dcfFileNumber = Integer.parseInt(files.get(0).getName().substring(4, 8));
			int dcfNumber = 10000 * dcfDirNumber + dcfFileNumber;
			properties.put("dcfpathnumber", String.valueOf(dcfDirNumber));
			properties.put("dcffilenumber", String.valueOf(dcfFileNumber));
			properties.put("dcfnumber", String.valueOf(dcfNumber));
		}
		File first = null;
		dateTime = null;
		if (exifExtensions != null) {
			for (String extension : exifExtensions) {
				for (File file : files) {
					if (extension.equals(FileUtils.getExtension(file).toLowerCase())) {
						if (first == null) {
							first = file;
						}
						readMetadata(file);
					}
				}
			}
		}
		if (dateTime == null) {
			dateTime = new Date(((first == null) ? files.get(0) : first).lastModified());
			log.warning("Could not read DateTime from EXIF, using lastModified instead.");
		}
		putDateTimeProperties(Locale.getDefault());
	}

	private void readMetadata(File file) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ExifSubIFDDirectory subIfdDir = metadata.getDirectory(ExifSubIFDDirectory.class);
			ExifIFD0Directory ifd0Dir = metadata.getDirectory(ExifIFD0Directory.class);

			dateTime = subIfdDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			if (dateTime == null) dateTime = subIfdDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
		} catch (Exception e) {
			log.log(Level.WARNING, "Error reading metadata from file " + file.toString(), e);
		}
	}

	private void putDateTimeProperties(Locale locale) {
		properties.put("year", String.format("%tY", dateTime));
		properties.put("month", String.format("%tm", dateTime));
		properties.put("day", String.format("%td", dateTime));
		properties.put("hour", String.format("%tH", dateTime));
		properties.put("minute", String.format("%tM", dateTime));
		properties.put("second", String.format("%tS", dateTime));
		Calendar dtCal = Calendar.getInstance();
		dtCal.setTime(dateTime);
		properties.put("monthname", dtCal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale));
		properties.put("dayname", dtCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale));
		properties.put("monthshortname", dtCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale));
		properties.put("dayshortname", dtCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale));
	}

	public String getProperty(String variable) {
		return properties.get(variable);
	}

	public Date getDateTime() {
		return dateTime;
	}

	public List<File> getFiles() {
		return files;
	}

	public int compareTo(ImportGroup other) {
		return dateTime.compareTo(other.dateTime);
	}
}
