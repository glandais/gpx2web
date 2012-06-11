package org.glandais.photoindexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.glandais.photoindexer.model.Photo;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

public class PhotoIndexer {

	// 8 hours
	private static final long MAX_LENGTH = 8 * 60 * 60 * 1000;
	private boolean simu;
	private List<Photo> photos;
	private List<Photo> videos;
	private List<Photo> dupes;
	private Map<Long, List<Photo>> all;
	private long prevDateDupe;
	private long prevDateFolder;
	private File prevFolder;
	private boolean nodel;

	public PhotoIndexer(boolean simu, boolean nodel) {
		super();
		this.simu = simu;
		this.nodel = nodel;
	}

	public static void main(String[] args) {
		Options options = getOptions();

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);

			boolean simu = cmd.hasOption("simu");
			boolean nodel = cmd.hasOption("nodel");
			boolean clean = cmd.hasOption("clean");

			File source = (File) cmd.getParsedOptionValue("source");
			File videos = (File) cmd.getParsedOptionValue("videos");
			File photos = (File) cmd.getParsedOptionValue("photos");

			PhotoIndexer photoIndexer = new PhotoIndexer(simu, nodel);
			photoIndexer.scan(source);
			photoIndexer.processPhotos(photos);
			photoIndexer.processVideos(videos);

			if (clean) {
				photoIndexer.clean(source);
				photoIndexer.clean(photos);
				photoIndexer.clean(videos);

				photoIndexer.showDupes();
			}

		} catch (ParseException e) {
			printHelp(options);
			return;
		}

	}

	private void showDupes() {
		Set<Entry<Long, List<Photo>>> entrySet = all.entrySet();
		for (Entry<Long, List<Photo>> entry : entrySet) {
			List<Photo> list = entry.getValue();
			if (list.size() > 1) {
				Map<Long, List<Photo>> dupes = new HashMap<Long, List<Photo>>();
				for (Photo photo : list) {
					try {
						Long crc = FileUtils.checksumCRC32(new File(photo
								.getFullPath()));
						List<Photo> list2 = dupes.get(crc);
						if (list2 == null) {
							list2 = new ArrayList<Photo>();
							dupes.put(crc, list2);
						}
						list2.add(photo);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Set<Entry<Long, List<Photo>>> entrySet2 = dupes.entrySet();
				for (Entry<Long, List<Photo>> entry2 : entrySet2) {
					System.out.println("** DUPES with CRC " + entry2.getKey());
					List<Photo> value = entry2.getValue();
					for (Photo photo : value) {
						System.out.println(photo.getFullPath());
					}
				}
			}
		}
	}

	private boolean clean(File toClear) {
		boolean toRemove = true;
		if (toClear.exists()) {
			File[] listFiles = toClear.listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						if (!clean(file)) {
							toRemove = false;
						}
					} else {
						if (isEssential(file)) {
							toRemove = false;
						} else {
							file.delete();
						}
					}
				}
			} else {
				toRemove = true;
			}
			if (toRemove) {
				try {
					FileUtils.deleteDirectory(toClear);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return toRemove;
	}

	private static boolean isEssential(File file) {
		if (file.getName().equals(".picasa.ini")) {
			return false;
		}
		if (file.getName().equals("Picasa.ini")) {
			return false;
		}
		if (file.getName().equals("desktop.ini")) {
			return false;
		}
		if (file.getName().endsWith(".url")) {
			return false;
		}
		if (file.getName().equals("Thumbs.db")) {
			return false;
		}
		return true;
	}

	private void processVideos(File videosFolder) {
		process(videosFolder, videos);
	}

	private void processPhotos(File photosFolder) {
		process(photosFolder, photos);
	}

	private void process(File photosFolder, List<Photo> items) {
		Collections.sort(items);
		dupes = new ArrayList<Photo>();
		prevDateDupe = 0;
		prevDateFolder = 0;
		prevFolder = null;
		for (Photo photo : items) {
			long time = photo.getDate().getTime();
			if (Math.abs(prevDateDupe - time) == 0) {
				dupes.add(photo);
			} else {
				processQueue(photosFolder);
				dupes = new ArrayList<Photo>();
				dupes.add(photo);
			}
			prevDateDupe = time;
		}
		processQueue(photosFolder);
	}

	private void processQueue(File folder) {
		if (dupes.size() > 1) {
			List<Photo> differents = getDifferents();
			for (Photo different : differents) {
				exportPhoto(folder, different);
			}
		} else if (dupes.size() == 1) {
			exportPhoto(folder, dupes.get(0));
		}
	}

	private List<Photo> getDifferents() {
		Map<Long, Photo> result = new HashMap<Long, Photo>();
		for (Photo photo : dupes) {
			File f = new File(photo.getFullPath());
			Long length = new Long(f.length());
			if (!result.containsKey(length)) {
				result.put(length, photo);
			}
		}
		return new ArrayList<Photo>(result.values());
	}

	private void exportPhoto(File folder, Photo photo) {
		File photoFolder = null;
		long time = photo.getDate().getTime();
		if (time - prevDateFolder < MAX_LENGTH) {
			photoFolder = prevFolder;
		}
		if (photoFolder == null) {
			photoFolder = getFolder(folder, photo);
		}
		try {
			copyPhoto(photo, photoFolder);
		} catch (IOException e) {
			System.out.println("** ERROR ** " + photo + " (" + e.getMessage()
					+ ")");
		}
		prevFolder = photoFolder;
		prevDateFolder = time;
	}

	private File getFolder(File folder, Photo photo) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(photo.getDate());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		File y = new File(folder, Integer.toString(year));
		return new File(y, Integer.toString(year) + "-" + padDate(month) + "-"
				+ padDate(day));

		// File ym = null;
		// ym = new File(folder, Integer.toString(year) + "-" + padDate(month));
		// return new File(ym, padDate(day));
	}

	public String padDate(int value) {
		if (value < 10) {
			return "0" + Integer.toString(value);
		} else {
			return Integer.toString(value);
		}
	}

	private void copyPhoto(Photo photo, File photoFolder) throws IOException {
		File photoFile = new File(photo.getFullPath());
		String oldPhotoName = photoFile.getName();
		String extension = oldPhotoName.substring(
				oldPhotoName.lastIndexOf('.') + 1).toLowerCase();

		if (!simu) {
			photoFolder.mkdirs();
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(photo.getDate());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);

		String photoName = Integer.toString(year) + "-" + padDate(month) + "-"
				+ padDate(day);
		photoName = photoName + "_" + padDate(hour) + "-" + padDate(minutes)
				+ "-" + padDate(seconds);
		File finalName = new File(photoFolder, photoName + "." + extension);

		if (finalName.exists()) {
			int i = 2;
			while (finalName.exists()) {
				if (finalName.getAbsolutePath().equals(
						photoFile.getAbsolutePath())) {
					System.out.println("** " + photoFile + " is already good!");
					return;
				}
				if (FileUtils.contentEquals(finalName, photoFile)) {
					System.out.println("** " + photoFile + " == " + finalName);
					deleteFile(photoFile);
					return;
				}
				finalName = new File(photoFolder, photoName + "(" + i + ")."
						+ extension);
				i++;
			}
		}

		if (nodel) {
			copyFile(photoFile, finalName);
		} else {
			moveFile(photoFile, finalName);
		}
	}

	private void copyFile(File photoFile, File finalName) throws IOException {
		if (simu) {
			System.out.println("** SIMU : copying "
					+ photoFile.getAbsolutePath() + " to "
					+ finalName.getAbsolutePath());
		} else {
			System.out.println("** Copying " + photoFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
			FileUtils.copyFile(photoFile, finalName);
		}
	}

	private void moveFile(File photoFile, File finalName) throws IOException {
		if (simu) {
			System.out.println("** SIMU : moving "
					+ photoFile.getAbsolutePath() + " to "
					+ finalName.getAbsolutePath());
		} else {
			System.out.println("** Moving " + photoFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
			FileUtils.moveFile(photoFile, finalName);
		}
	}

	private void deleteFile(File photoFile) {
		if (!nodel) {
			if (simu) {
				System.out.println("** SIMU : deleting "
						+ photoFile.getAbsolutePath());
			} else {
				System.out
						.println("** Deleting " + photoFile.getAbsolutePath());
				photoFile.delete();
			}
		}
	}

	private void scan(File folder) {
		photos = new ArrayList<Photo>();
		videos = new ArrayList<Photo>();
		all = new HashMap<Long, List<Photo>>();
		scanRecursion(folder);
	}

	private void scanRecursion(File folder) {
		if (folder.exists()) {
			File[] listFiles = folder.listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						scanRecursion(file);
					} else {
						addFile(file);
					}
				}
			}
		}
	}

	private void addFile(File file) {
		String lc = file.getName().toLowerCase();
		try {
			if (lc.endsWith(".jpg") || lc.endsWith(".jpeg")) {
				addPhoto(file);
			} else if (lc.endsWith(".avi") || lc.endsWith(".mov")
					|| lc.endsWith(".3gp")) {
				addVideo(file);
			} else {
				System.out.println("Unknown type " + file.getAbsolutePath());
			}
		} catch (Exception e) {
			System.out.println("Failed to get date for "
					+ file.getAbsolutePath());
		}
	}

	private void addVideo(File file) throws Exception {
		Date date = getFileDate(file);
		Photo video = new Photo();
		video.setFullPath(file.getAbsolutePath());
		video.setDate(date);
		videos.add(video);

		addToAll(video);
	}

	private void addPhoto(File file) throws Exception {
		Date date = getFileDate(file);
		Photo photo = new Photo();
		photo.setFullPath(file.getAbsolutePath());
		photo.setDate(date);
		photos.add(photo);

		addToAll(photo);
	}

	private void addToAll(Photo photo) {
		Long length = new Long(new File(photo.getFullPath()).length());
		List<Photo> list = all.get(length);
		if (list == null) {
			list = new ArrayList<Photo>();
			all.put(length, list);
		}
		list.add(photo);
	}

	private Date getFileDate(File file) throws Exception {
		Date date;
		String lc = file.getName().toLowerCase();
		if (lc.endsWith(".jpg") || lc.endsWith(".jpeg")) {
			date = getJPEGDate(file);
		} else {
			date = new Date(file.lastModified());
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if (c.get(Calendar.YEAR) < 1990) {
			throw new Exception("Invalid date");
		}
		return date;
	}

	private Date getJPEGDate(File file) throws JpegProcessingException,
			MetadataException {
		Metadata metadata = JpegMetadataReader.readMetadata(file);
		Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
		Date date = exifDirectory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
		return date;
	}

	private static Options getOptions() {
		Options options = new Options();

		Option simuOption = new Option("simu", false,
				"Run as simulation (no file touched)");
		options.addOption(simuOption);

		Option nodelOption = new Option("nodel", false, "No deletion in source");
		options.addOption(nodelOption);

		Option cleanOption = new Option("clean", false, "Delete empty folders");
		options.addOption(cleanOption);

		Option photosOption = new Option("photos", true, "Root photo folder");
		photosOption.setRequired(true);
		photosOption.setType(PatternOptionBuilder.EXISTING_FILE_VALUE);
		options.addOption(photosOption);

		Option videosOption = new Option("videos", true, "Root video folder");
		videosOption.setRequired(true);
		videosOption.setType(PatternOptionBuilder.EXISTING_FILE_VALUE);
		options.addOption(videosOption);

		Option sourceOption = new Option("source", true, "Source folder");
		sourceOption.setRequired(true);
		sourceOption.setType(PatternOptionBuilder.EXISTING_FILE_VALUE);
		options.addOption(sourceOption);

		return options;
	}

	private static void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("photoindexer", options);
	}

}
