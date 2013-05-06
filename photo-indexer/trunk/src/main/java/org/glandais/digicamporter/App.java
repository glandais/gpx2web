package org.glandais.digicamporter;

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
import org.glandais.digicamporter.distance.BKTree;
import org.glandais.digicamporter.distance.ImagePHash;
import org.glandais.digicamporter.exiftool.ExifTool;
import org.glandais.digicamporter.exiftool.ExifTool.Feature;
import org.glandais.digicamporter.exiftool.ExifTool.Format;
import org.glandais.digicamporter.exiftool.ExifTool.Tag;
import org.glandais.digicamporter.model.Media;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;

public class App {

	public static ImagePHash IMAGE_P_HASH = new ImagePHash(64, 16);

	public static boolean FAKE = false;

	private static ExifTool exifTool = new ExifTool(Feature.STAY_OPEN);

	// 2013:05:06 18:38:57
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy:MM:dd HH:mm:ss");

	private static void safeDeleteDirectory(File toClear) throws IOException {
		if (FAKE) {
			System.out.println("Fake : deleting " + toClear.getAbsolutePath());
		} else {
			FileUtils.deleteDirectory(toClear);
		}
	}

	private static void safeDeleteFile(File file) {
		if (FAKE) {
			System.out.println("Fake : deleting " + file.getAbsolutePath());
		} else {
			file.delete();
		}
	}

	private static void safeMoveFileToDirectory(File file, File mt)
			throws IOException {
		if (FAKE) {
			System.out.println("Fake : moving " + file.getAbsolutePath()
					+ " to " + mt.getAbsolutePath());
		} else {
			FileUtils.moveFileToDirectory(file, mt, true);
		}
	}

	private static void safeMkdirs(File folder) {
		if (FAKE) {
			System.out.println("Fake : mkdirs " + folder.getAbsolutePath());
		} else {
			folder.mkdirs();
		}
	}

	private static void safeCopyFile(File mediaFile, File finalName)
			throws IOException {
		if (FAKE) {
			System.out.println("Fake : copyFile " + mediaFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
		} else {
			FileUtils.copyFile(mediaFile, finalName);
		}
	}

	private static void safeMoveFile(File mediaFile, File finalName)
			throws IOException {
		if (FAKE) {
			System.out.println("Fake : moveFile " + mediaFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
		} else {
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
		options.addOption("c", false, "clear index");

		options.addOption("autofrom", true, "Source folder (photos and movies)");
		options.addOption("autopics", true, "Target folder : pictures");
		options.addOption("automovies", true, "Target folder : movies");

		options.addOption("i", true, "index directory");
		options.addOption("d", true, "delete empty folders");
		options.addOption("l", false, "list index");
		options.addOption("s", false, "find similar images");
		options.addOption("e", true, "export index to folder");
		options.addOption("move", false, "move pictures during export");
		options.addOption("mt", true, "move movies to");
		options.addOption("mf", true, "move movies from");

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

		if (cmd.hasOption("c") || cmd.hasOption("i") || cmd.hasOption("l")
				|| cmd.hasOption("s") || cmd.hasOption("e")
				|| cmd.hasOption("autofrom")) {
			// A SessionFactory is set up once for an application
			// hibernate.cfg.xml
			sessionFactory = new Configuration().configure()
					.buildSessionFactory();
		}

		if (cmd.hasOption("h")) {
			printHelp(options);
		} else if (cmd.hasOption("autofrom")) {
			if (!cmd.hasOption("autopics")) {
				printHelp(options);
				return;
			}
			if (!cmd.hasOption("automovies")) {
				printHelp(options);
				return;
			}

			clearIndex();
			index(new File(cmd.getOptionValue("autofrom")));
			export(new File(cmd.getOptionValue("autopics")),
					cmd.hasOption("move"), true);
			export(new File(cmd.getOptionValue("automovies")),
					cmd.hasOption("move"), false);

		} else if (cmd.hasOption("c")) {
			clearIndex();
		} else if (cmd.hasOption("l")) {
			listIndex();
		} else if (cmd.hasOption("l")) {
			findSimilar();
		} else if (cmd.hasOption("d")) {
			deleteEmpty(new File(cmd.getOptionValue("d")));
		} else if (cmd.hasOption("i")) {
			index(new File(cmd.getOptionValue("i")));
		} else if (cmd.hasOption("e")) {
			export(new File(cmd.getOptionValue("e")), cmd.hasOption("move"),
					true);
		} else if (cmd.hasOption("mt")) {
			if (!cmd.hasOption("mf")) {
				printHelp(options);
				return;
			}
			moveMovies(new File(cmd.getOptionValue("mf")),
					new File(cmd.getOptionValue("mt")));
		} else {
			printHelp(options);
		}
		exifTool.close();
	}

	private static boolean deleteEmpty(File toClear) {
		boolean toRemove = true;
		if (toClear.exists()) {
			File[] listFiles = toClear.listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						if (!deleteEmpty(file)) {
							toRemove = false;
						}
					} else {
						if (isEssential(file)) {
							toRemove = false;
						} else {
							safeDeleteFile(file);
						}
					}
				}
			} else {
				toRemove = true;
			}
			if (toRemove) {
				try {
					safeDeleteDirectory(toClear);
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

	private static void moveMovies(File mf, File mt) {
		if (mf.exists()) {
			File[] listFiles = mf.listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						moveMovies(file, new File(mt, file.getName()));
					} else {
						if (isMovie(file)) {
							File newFile = new File(mt, file.getName());
							if (newFile.exists()) {
								if (newFile.length() != file.length()) {
									System.out
											.println("** ERROR ** Can't move "
													+ file.getAbsolutePath()
													+ " to "
													+ newFile.getAbsolutePath());
								} else {
									safeDeleteFile(file);
								}
							} else {
								try {
									safeMoveFileToDirectory(file, mt);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}

	private static boolean isPicture(File file) {
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

	private static void listIndex() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Criteria criteria = session.createCriteria(Media.class);
		criteria.addOrder(Order.asc("date"));
		List<Media> medias = criteria.list();
		for (Media media : medias) {
			System.out.println(media);
		}
		session.getTransaction().commit();
		session.close();
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
				if (move && finalName.length() == mediaFile.length()) {
					if (!mediaFile.equals(finalName)) {
						System.out.println("** " + mediaFile + " == "
								+ finalName);
						safeDeleteFile(mediaFile);
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
				Format.NUMERIC, Tag.DATE_TIME_ORIGINAL);
		String dateString = imageMeta.get(Tag.DATE_TIME_ORIGINAL);
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
