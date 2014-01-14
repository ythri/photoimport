package imagemover;

import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.CRC32;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImageGroup implements Comparable<ImageGroup> {
	public List<File> sourceFiles;
	
	public String fileName;
	String dcfPathNumber;
	String dcfFileNumber;
	Calendar dateTime;

	public Map<String, String> properties = new HashMap<String, String>();

	public ImageGroup(List<File> files, String dcfPathNumber, String dcfFileNumber) {
		sourceFiles = files;
		Date dateTime = null;

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(files.get(0));
			ExifSubIFDDirectory subIfdDir = metadata.getDirectory(ExifSubIFDDirectory.class);
			ExifIFD0Directory ifd0Dir = metadata.getDirectory(ExifIFD0Directory.class);

			dateTime = subIfdDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			if (dateTime == null) dateTime = subIfdDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
			if (dateTime == null) dateTime = ifd0Dir.getDate(ExifIFD0Directory.TAG_DATETIME);
			if (dateTime == null) dateTime = new Date(files.get(0).lastModified());
		} catch (Exception e) {}

		this.dateTime = Calendar.getInstance();
		this.dateTime.setTime(dateTime);

		this.dcfPathNumber = dcfPathNumber;
		this.dcfFileNumber = dcfFileNumber;
		fileName = getBaseName(files.get(0));
		readProperties(Locale.getDefault()); // Locale.forLanguageTag("de");
	}

	public ImageGroup(List<File> files) {
		this(files, null, null);
	}

	public ImageGroup(File file, String dcfPathNumber, String dcfFileNumber) {
		this(Collections.singletonList(file), dcfPathNumber, dcfFileNumber);
	}

	public ImageGroup(File file) {
		this(Collections.singletonList(file), null, null);
	}

	protected void readProperties(Locale locale) {
		// date and time
		properties.put("year", String.format("%tY", dateTime));
		properties.put("month", String.format("%tm", dateTime));
		properties.put("day", String.format("%td", dateTime));
		properties.put("hour", String.format("%tH", dateTime));
		properties.put("minute", String.format("%tM", dateTime));
		properties.put("second", String.format("%tS", dateTime));
		properties.put("monthname", dateTime.getDisplayName(Calendar.MONTH, Calendar.LONG, locale));
		properties.put("dayname", dateTime.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale));
		properties.put("monthshortname", dateTime.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale));
		properties.put("dayshortname", dateTime.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale));

		// file name information
		properties.put("filename", fileName);
		properties.put("dcfpathnumber", dcfPathNumber);
		properties.put("dcffilenumber", dcfFileNumber);

		// exif stuff (camera, owner, ...)
	}

	public long checksum(File file) {
		try {
			InputStream in = new FileInputStream(file);
			CRC32 crc = new CRC32();
			byte[] buffer = new byte[4096];
			int bytesRead;
			while((bytesRead = in.read(buffer)) != -1) {
				crc.update(buffer, 0, bytesRead);
			}
			return crc.getValue();
		} catch (Exception e) {
			return -1;
		}
	}

	public String getProperty(String name) {
		return properties.get(name.toLowerCase());
	}

	private String getBaseName(File file) {
		if (file == null) return null;
		String fileName = file.getName();
		int position = fileName.lastIndexOf(".");
		return (position <= 0) ? fileName : fileName.substring(0, position);
	}

	public int compareTo(ImageGroup g1) {
		return dateTime.compareTo(g1.dateTime);
	}
}
