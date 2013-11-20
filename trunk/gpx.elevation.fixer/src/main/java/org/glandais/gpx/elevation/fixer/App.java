package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Hello world!
 * 
 */
public class App {

	public static void main(String[] args) throws Exception {
		File fin = new File(args[0]);
		File fout = new File(args[1]);
		File[] listFiles = fin.listFiles();
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(90, 170, 100, 10,
				50, 0.1);

		// GregorianCalendar[] starts = getTmpStarts();
		// GregorianCalendar[] starts = getSwissStarts();
		Calendar instance = Calendar.getInstance();
//		instance.set(Calendar.DAY_OF_MONTH, 6);
//		instance.set(Calendar.MONTH, 7); // Jan. = 0!
		instance.set(Calendar.DAY_OF_MONTH, 21);
		instance.set(Calendar.MONTH, 7); // Jan. = 0!
		instance.set(Calendar.YEAR, 2013);
		instance.set(Calendar.HOUR_OF_DAY, 15);
		instance.set(Calendar.MINUTE, 0);
		instance.set(Calendar.SECOND, 0);
		instance.set(Calendar.MILLISECOND, 0);
		Calendar[] starts = new Calendar[] { instance };

		bikeTimeEval.setStarts(starts);

		for (File file : listFiles) {
			if (!file.getName().startsWith(".")
					&& file.getName().toLowerCase().endsWith(".gpx")) {
				try {
					processfile(file, fout, bikeTimeEval);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static GregorianCalendar[] getTmpStarts() {
		GregorianCalendar[] starts = new GregorianCalendar[2];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 4);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2013);
		start.set(Calendar.HOUR_OF_DAY, 7);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 5);
		start.set(Calendar.MONTH, 5); // Jan. = 0!
		start.set(Calendar.YEAR, 2013);
		start.set(Calendar.HOUR_OF_DAY, 7);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[1] = start;
		return starts;
	}

	private static GregorianCalendar[] getSwissStarts() {
		GregorianCalendar[] starts = new GregorianCalendar[3];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 18);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2012);
		start.set(Calendar.HOUR_OF_DAY, 7);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 19);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2012);
		start.set(Calendar.HOUR_OF_DAY, 7);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[1] = start;

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 26);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2012);
		start.set(Calendar.HOUR_OF_DAY, 7);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[2] = start;
		return starts;
	}

	private static void processfile(File file, File fout,
			GPXBikeTimeEval bikeTimeEval) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(file);

		List<GPXPath> paths = GPXParser.parsePaths(gpxFile, true);
		for (GPXPath gpxPath : paths) {
			gpxPath.postProcess(bikeTimeEval);

			System.out.println(gpxPath.getName());
			System.out.println("min elevation : " + gpxPath.getMinElevation());
			System.out.println("max elevation : " + gpxPath.getMaxElevation());
			System.out.println("total elevation : "
					+ gpxPath.getTotalElevation());

			GPXCharter.createChartAndMap(gpxPath, fout.getAbsolutePath() + "/"
					+ file.getName(), 0);

		}
		System.out.println(file.getName());
		GPXFileWriter.writeGpxFile(paths, new File(fout.getAbsolutePath() + "/"
				+ file.getName()));
	}

}
