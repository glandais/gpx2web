package org.glandais.digicamtools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.glandais.digicamtools.distance.BKTree;
import org.glandais.digicamtools.distance.ImagePHash;
import org.glandais.digicamtools.exiftool.ExifTool;
import org.glandais.digicamtools.exiftool.ExifTool.Feature;
import org.glandais.digicamtools.exiftool.ExifTool.Format;
import org.glandais.digicamtools.exiftool.ExifTool.Tag;
import org.glandais.digicamtools.model.Media;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;

public class Importer {

	public static ImagePHash IMAGE_P_HASH = new ImagePHash(64, 16);

	public static boolean FAKE = false;

	private static ExifTool exifTool = new ExifTool(Feature.STAY_OPEN);

	// 2013:05:06 18:38:57
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy:MM:dd HH:mm:ss");

	private static void safeDeleteFile(File file) {
		if (FAKE) {
			System.out.println("Fake : deleting " + file.getAbsolutePath());
		} else {
			System.out.println("Deleting " + file.getAbsolutePath());
			file.delete();
		}
	}

	private static void safeMkdirs(File folder) {
		if (FAKE) {
			System.out.println("Fake : mkdirs " + folder.getAbsolutePath());
		} else {
			System.out.println("mkdirs " + folder.getAbsolutePath());
			folder.mkdirs();
		}
	}

	private static void safeCopyFile(File mediaFile, File finalName)
			throws IOException {
		if (FAKE) {
			System.out.println("Fake : copyFile " + mediaFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
		} else {
			System.out.println("CopyFile " + mediaFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
			FileUtils.copyFile(mediaFile, finalName);
		}
	}

	private static void safeMoveFile(File mediaFile, File finalName)
			throws IOException {
		if (FAKE) {
			System.out.println("Fake : moveFile " + mediaFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
		} else {
			System.out.println("MoveFile " + mediaFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
			FileUtils.moveFile(mediaFile, finalName);
		}
	}

	// 8 hours
	private static final long MAX_LENGTH = 8 * 60 * 60 * 1000;
	private static SessionFactory sessionFactory;

	public static void main(String[] args) {
		Options options = new Options();

		options.addOption("fake", false, "do not do anything on real files");

		options.addOption("h", false, "print this help");

		options.addOption("from", true, "Source folder (photos and movies)");
		options.addOption("targetpics", true, "Target folder : pictures");
		options.addOption("targetmovies", true, "Target folder : movies");

		options.addOption("s", false, "find similar images");
		options.addOption("move", false, "move pictures during export");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelp(options);
			return;
		}
		if (cmd.hasOption("fake")) {
			FAKE = true;
		}

		// A SessionFactory is set up once for an application
		// hibernate.cfg.xml
		sessionFactory = new Configuration().configure().buildSessionFactory();

		if (cmd.hasOption("h")) {
			printHelp(options);
		} else if (cmd.hasOption("from")) {
			if (!cmd.hasOption("targetpics")) {
				printHelp(options);
				return;
			}
			if (!cmd.hasOption("targetmovies")) {
				printHelp(options);
				return;
			}

			clearIndex();
			index(new File(cmd.getOptionValue("from")));
			export(new File(cmd.getOptionValue("targetpics")),
					cmd.hasOption("move"), true);
			export(new File(cmd.getOptionValue("targetmovies")),
					cmd.hasOption("move"), false);

		} else {
			printHelp(options);
		}
		exifTool.close();
	}

	public static boolean isPicture(File file) {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
			return true;
		}
		return false;
	}

	private static boolean isMovie(File file) {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".avi")) {
			return true;
		}
		if (name.endsWith(".mov")) {
			return true;
		}
		if (name.endsWith(".wav")) {
			return true;
		}
		if (name.endsWith(".wmv")) {
			return true;
		}
		if (name.endsWith(".mpg")) {
			return true;
		}
		if (name.endsWith(".mts")) {
			return true;
		}
		if (name.endsWith(".mp4")) {
			return true;
		}
		if (name.endsWith(".3gp")) {
			return true;
		}
		if (name.endsWith(".m4v")) {
			return true;
		}
		return false;
	}

	private static void findSimilar() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Criteria criteria = session.createCriteria(Media.class);
		criteria.addOrder(Order.asc("date"));
		List<Media> photos = criteria.list();

		BKTree tree = new BKTree();
		Map<String, List<Media>> photosByHash = new HashMap<String, List<Media>>();

		for (Media photo : photos) {
			String hash = photo.getHash();
			if (hash != null && !hash.equals("")) {
				tree.add(hash);
				addHash(photosByHash, photo);
			}
		}

		List<Set<Media>> similarSets = new ArrayList<Set<Media>>();

		for (Media photo : photos) {
			if (photo.getHash() != null && !photo.getHash().equals("")) {
				List<String> result = tree.search(photo.getHash(), 20);

				Set<Media> allSimilarPhotos = new TreeSet<Media>();
				for (String hash : result) {
					List<Media> similarPhotos = photosByHash.get(hash);
					if (similarPhotos != null) {
						allSimilarPhotos.addAll(similarPhotos);
					}
				}
				allSimilarPhotos.remove(photo);

				if (allSimilarPhotos.size() > 0) {

					Set<Media> similarSet = null;
					for (Set<Media> set : similarSets) {
						if (set.contains(photo)) {
							similarSet = set;
						}
					}
					if (similarSet == null) {
						similarSet = new HashSet<Media>();
						similarSets.add(similarSet);
					}

					similarSet.add(photo);
					for (Media similarPhoto : allSimilarPhotos) {
						similarSet.add(similarPhoto);
					}
				}
			}
		}

		for (Set<Media> set : similarSets) {
			if (set.size() > 0) {
				System.out.println("**************");

				Media refPhoto = null;
				for (Media photo : set) {
					int distance = 0;
					if (refPhoto == null) {
						refPhoto = photo;
					} else {
						distance = BKTree.distance(photo.getHash(),
								refPhoto.getHash());
					}
					System.out.println(distance + " " + photo.getFullPath());
				}
				for (Media photo : set) {
					System.out.println("gnome-open \"" + photo.getFullPath()
							+ "\"&");
				}

				System.out.println("**************");
			}
		}

		session.getTransaction().commit();
		session.close();
	}

	private static void addHash(Map<String, List<Media>> photosByHash,
			Media photo) {
		String hashString = photo.getHash();
		List<Media> photos = photosByHash.get(hashString);
		if (photos == null) {
			photos = new ArrayList<Media>();
			photosByHash.put(hashString, photos);
		}
		photos.add(photo);
	}

	private static void export(File folder, boolean move, boolean photoMode) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Criteria criteria = session.createCriteria(Media.class);
		criteria.addOrder(Order.asc("date"));
		criteria.addOrder(Order.asc("fullPath"));
		List<Media> medias = criteria.list();
		exportMedias(medias, folder, move, photoMode);
		session.getTransaction().commit();
		session.close();
	}

	private static List<Media> dupes = new ArrayList<Media>();
	private static long prevDateFolder = 0;
	private static long prevDateDupe = 0;
	private static File prevFolder = null;

	private static void exportMedias(List<Media> medias, File folder,
			boolean move, boolean photoMode) {
		safeMkdirs(folder);
		dupes = new ArrayList<Media>();
		prevDateDupe = 0;
		prevDateFolder = 0;
		prevFolder = null;
		for (Media media : medias) {
			File mediaFile = new File(media.getFullPath());
			if ((photoMode && isPicture(mediaFile))
					|| (!photoMode && isMovie(mediaFile))) {
				long time = media.getDate().getTime();
				if (Math.abs(prevDateDupe - time) == 0) {
					dupes.add(media);
				} else {
					processQueue(folder, move);
					dupes = new ArrayList<Media>();
					dupes.add(media);
				}
				prevDateDupe = time;
			}
		}
		processQueue(folder, move);
	}

	private static void processQueue(File folder, boolean move) {
		if (dupes.size() > 1) {
			List<Media> differents = getDifferents();
			for (Media different : differents) {
				exportMedia(folder, different, move);
			}
		} else if (dupes.size() == 1) {
			exportMedia(folder, dupes.get(0), move);
		}
	}

	private static List<Media> getDifferents() {
		// FIXME hashcodes then equals!!! Instead of size

		Map<Long, Media> result = new HashMap<Long, Media>();
		for (Media media : dupes) {
			File f = new File(media.getFullPath());
			Long length = new Long(f.length());
			if (!result.containsKey(length)) {
				result.put(length, media);
			}
		}
		return new ArrayList<Media>(result.values());
	}

	private static void exportMedia(File folder, Media media, boolean move) {
		File mediaFolder = null;
		long time = media.getDate().getTime();
		if (time - prevDateFolder < MAX_LENGTH) {
			mediaFolder = prevFolder;
		}
		if (mediaFolder == null) {
			mediaFolder = getFolder(folder, media);
		}
		try {
			copyMedia(media, mediaFolder, move);
		} catch (IOException e) {
			System.out.println("** ERROR ** " + media + " (" + e.getMessage()
					+ ")");
		}
		prevFolder = mediaFolder;
		prevDateFolder = time;
	}

	private static File getFolder(File folder, Media media) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(media.getDate());
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

	public static String padDate(int value) {
		if (value < 10) {
			return "0" + Integer.toString(value);
		} else {
			return Integer.toString(value);
		}
	}

	private static void copyMedia(Media media, File mediaFolder, boolean move)
			throws IOException {
		String fullPath = media.getFullPath();
		int point = fullPath.lastIndexOf('.');
		String fileExtension = "jpg";
		if (point >= 0) {
			fileExtension = fullPath.substring(point + 1);
		}
		File mediaFile = new File(fullPath);

		safeMkdirs(mediaFolder);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(media.getDate());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);

		String mediaName = Integer.toString(year) + "-" + padDate(month) + "-"
				+ padDate(day);
		mediaName = mediaName + "_" + padDate(hour) + "-" + padDate(minutes)
				+ "-" + padDate(seconds);
		File finalName = new File(mediaFolder, mediaName + "." + fileExtension);
		if (finalName.exists()) {
			int i = 2;
			while (finalName.exists()) {
				if (FileUtils.contentEquals(finalName, mediaFile)) {
					if (!mediaFile.equals(finalName)) {
						System.out.println("** " + mediaFile + " == "
								+ finalName);
						if (move) {
							safeDeleteFile(mediaFile);
						}
					}
					return;
				}
				finalName = new File(mediaFolder, mediaName + "(" + i + ")."
						+ fileExtension);
				i++;
			}
		}

		if (move) {
			safeMoveFile(mediaFile, finalName);
		} else {
			safeCopyFile(mediaFile, finalName);
		}
	}

	private static void index(File folder) {
		if (folder.exists()) {
			List<Media> medias = new ArrayList<Media>();
			File[] listFiles = folder.listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						index(file);
					} else {
						indexFile(file, medias);
					}
				}
			}
			if (medias.size() > 0) {
				saveMedias(medias);
			}
		}
	}

	private static void saveMedias(List<Media> medias) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		for (Media media : medias) {
			session.persist(media);
		}
		session.getTransaction().commit();
		session.close();
	}

	private static void indexFile(File file, List<Media> media) {
		if (isPicture(file) || isMovie(file)) {
			indexMedia(file, media);
		} else {
			System.out.println("** IGNORED ** " + file.getAbsolutePath());
		}
	}

	private static void indexMedia(File file, List<Media> medias) {
		Date date;
		try {
			date = getMediaDate(file);
			if (date == null) {
				return;
			}
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			if (c.get(Calendar.YEAR) < 2000) {
				return;
			}
		} catch (Throwable e) {
			System.out.println("** ERROR ** " + file.getAbsolutePath());
			e.printStackTrace();
			return;
		}

		String hash = null;
		try {
			// hash = IMAGE_P_HASH.getHash(new FileInputStream(file));
		} catch (Exception e) {
			hash = null;
			e.printStackTrace();
		}

		Media media = new Media();
		media.setFullPath(file.getAbsolutePath());
		media.setDate(date);
		media.setHash(hash);
		medias.add(media);

		System.out.println("Indexed " + file.getAbsolutePath() + " - "
				+ media.getDate());
	}

	// private static Date getMediaDate(File file) throws
	// JpegProcessingException,
	// MetadataException, IOException {
	// Metadata metadata = JpegMetadataReader.readMetadata(file);
	// Directory exifDirectory = metadata
	// .getDirectory(ExifSubIFDDirectory.class);
	// Date date = exifDirectory
	// .getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
	// return date;
	// }

	private static Date getMediaDate(File file)
			throws IllegalArgumentException, SecurityException, IOException,
			java.text.ParseException {
		Map<Tag, String> imageMeta = exifTool.getImageMeta(file,
				Format.NUMERIC, Tag.DATE_TIME_ORIGINAL, Tag.FILE_MODIFY_DATE);
		String dateString = imageMeta.get(Tag.DATE_TIME_ORIGINAL);
		if (dateString == null) {
			dateString = imageMeta.get(Tag.FILE_MODIFY_DATE);
		}
		return sdf.parse(dateString);
	}

	private static void clearIndex() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Query query = session.createQuery("DELETE FROM Media");
		query.executeUpdate();
		session.getTransaction().commit();
		session.close();
	}

	private static void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("digicamporter", options);
		exifTool.close();
	}
}
