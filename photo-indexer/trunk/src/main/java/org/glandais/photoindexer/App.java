package org.glandais.photoindexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.glandais.photoindexer.model.Photo;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;

/**
 * Hello world!
 * 
 */
public class App {

	public static boolean FAKE = false;

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

	private static void safeCopyFile(File photoFile, File finalName)
			throws IOException {
		if (FAKE) {
			System.out.println("Fake : copyFile " + photoFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
		} else {
			FileUtils.copyFile(photoFile, finalName);
		}
	}

	private static void safeMoveFile(File photoFile, File finalName)
			throws IOException {
		if (FAKE) {
			System.out.println("Fake : moveFile " + photoFile.getAbsolutePath()
					+ " to " + finalName.getAbsolutePath());
		} else {
			FileUtils.moveFile(photoFile, finalName);
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
		options.addOption("i", true, "index directory");
		options.addOption("d", true, "delete empty folders");
		options.addOption("l", false, "list index");
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
				|| cmd.hasOption("e")) {
			// A SessionFactory is set up once for an application
			// hibernate.cfg.xml
			sessionFactory = new Configuration().configure()
					.buildSessionFactory();
		}

		if (cmd.hasOption("h")) {
			printHelp(options);
		} else if (cmd.hasOption("c")) {
			clearIndex();
		} else if (cmd.hasOption("l")) {
			listIndex();
		} else if (cmd.hasOption("d")) {
			deleteEmpty(new File(cmd.getOptionValue("d")));
		} else if (cmd.hasOption("i")) {
			index(new File(cmd.getOptionValue("i")));
		} else if (cmd.hasOption("e")) {
			export(new File(cmd.getOptionValue("e")), cmd.hasOption("move"));
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
		return false;
	}

	private static void listIndex() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Criteria criteria = session.createCriteria(Photo.class);
		criteria.addOrder(Order.asc("date"));
		List<Photo> photos = criteria.list();
		for (Photo photo : photos) {
			System.out.println(photo);
		}
		session.getTransaction().commit();
		session.close();
	}

	private static void export(File folder, boolean move) {
		// try {
		// if (!move) {
		// deleteDirectory(folder);
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Criteria criteria = session.createCriteria(Photo.class);
		criteria.addOrder(Order.asc("date"));
		List<Photo> photos = criteria.list();
		exportPhotos(photos, folder, move);
		session.getTransaction().commit();
		session.close();
	}

	private static List<Photo> dupes = new ArrayList<Photo>();
	private static long prevDateFolder = 0;
	private static long prevDateDupe = 0;
	private static File prevFolder = null;

	private static void exportPhotos(List<Photo> photos, File folder,
			boolean move) {
		safeMkdirs(folder);

		dupes = new ArrayList<Photo>();
		prevDateDupe = 0;
		prevDateFolder = 0;
		prevFolder = null;
		for (Photo photo : photos) {
			long time = photo.getDate().getTime();
			if (Math.abs(prevDateDupe - time) == 0) {
				dupes.add(photo);
			} else {
				processQueue(folder, move);
				dupes = new ArrayList<Photo>();
				dupes.add(photo);
			}
			prevDateDupe = time;
		}
		processQueue(folder, move);
	}

	private static void processQueue(File folder, boolean move) {
		if (dupes.size() > 1) {
			List<Photo> differents = getDifferents();
			for (Photo different : differents) {
				exportPhoto(folder, different, move);
			}
		} else if (dupes.size() == 1) {
			exportPhoto(folder, dupes.get(0), move);
		}
	}

	private static List<Photo> getDifferents() {
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

	private static void exportPhoto(File folder, Photo photo, boolean move) {
		File photoFolder = null;
		long time = photo.getDate().getTime();
		if (time - prevDateFolder < MAX_LENGTH) {
			photoFolder = prevFolder;
		}
		if (photoFolder == null) {
			photoFolder = getFolder(folder, photo);
		}
		try {
			copyPhoto(photo, photoFolder, move);
		} catch (IOException e) {
			System.out.println("** ERROR ** " + photo + " (" + e.getMessage()
					+ ")");
		}
		prevFolder = photoFolder;
		prevDateFolder = time;
	}

	private static File getFolder(File folder, Photo photo) {
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

	public static String padDate(int value) {
		if (value < 10) {
			return "0" + Integer.toString(value);
		} else {
			return Integer.toString(value);
		}
	}

	private static void copyPhoto(Photo photo, File photoFolder, boolean move)
			throws IOException {
		File photoFile = new File(photo.getFullPath());

		safeMkdirs(photoFolder);

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
		File finalName = new File(photoFolder, photoName + ".jpg");
		if (finalName.exists()) {
			int i = 2;
			while (finalName.exists()) {
				if (move && finalName.length() == photoFile.length()) {
					System.out.println("** " + photoFile + " == " + finalName);
					safeDeleteFile(photoFile);
					return;
				}
				finalName = new File(photoFolder, photoName + "(" + i + ").jpg");
				i++;
			}
		}

		if (move) {
			safeMoveFile(photoFile, finalName);
		} else {
			safeCopyFile(photoFile, finalName);
		}
	}

	private static void index(File folder) {
		if (folder.exists()) {
			List<Photo> photos = new ArrayList<Photo>();
			File[] listFiles = folder.listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						index(file);
					} else {
						indexFile(file, photos);
					}
				}
			}
			if (photos.size() > 0) {
				savePhotos(photos);
			}
		}
	}

	private static void savePhotos(List<Photo> photos) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		for (Photo photo : photos) {
			session.persist(photo);
		}
		session.getTransaction().commit();
		session.close();
	}

	private static void indexFile(File file, List<Photo> photos) {
		String lc = file.getName().toLowerCase();
		if (lc.endsWith(".jpg") || lc.endsWith(".jpeg")) {
			indexJPEG(file, photos);
		} else {
			System.out.println("** IGNORED ** " + file.getAbsolutePath());
		}
	}

	private static void indexJPEG(File file, List<Photo> photos) {
		Date date;
		try {
			date = getJPEGDate(file);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			if (c.get(Calendar.YEAR) < 2000) {
				return;
			}
		} catch (Exception e) {
			System.out.println("** ERROR ** " + file.getAbsolutePath());
			return;
		}
		Photo photo = new Photo();
		photo.setFullPath(file.getAbsolutePath());
		photo.setDate(date);
		photos.add(photo);
	}

	private static Date getJPEGDate(File file) throws JpegProcessingException,
			MetadataException, IOException {
		Metadata metadata = JpegMetadataReader.readMetadata(file);
		Directory exifDirectory = metadata
				.getDirectory(ExifSubIFDDirectory.class);
		Date date = exifDirectory
				.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		return date;
	}

	private static void clearIndex() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Query query = session.createQuery("DELETE FROM Photo");
		query.executeUpdate();
		session.getTransaction().commit();
		session.close();
	}

	private static void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("photoindexer", options);
	}
}
