package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(95, 140, 100, 10, 40, 0.1);
		//		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(85, 250, 100, 10, 50, 0.1);

		GregorianCalendar[] starts = getTmpStarts();
		//		GregorianCalendar[] starts = getFixStarts();
		// GregorianCalendar[] starts = getSwissStarts();
		//		Calendar instance = Calendar.getInstance();
		//		instance.set(Calendar.DAY_OF_MONTH, 23);
		//		instance.set(Calendar.MONTH, 6); // Jan. = 0!
		//		instance.set(Calendar.YEAR, 2016);
		//		instance.set(Calendar.HOUR_OF_DAY, 7);
		//		instance.set(Calendar.MINUTE, 0);
		//		instance.set(Calendar.SECOND, 0);
		//		instance.set(Calendar.MILLISECOND, 0);
		//		Calendar[] starts = new Calendar[] { instance };

		bikeTimeEval.setStarts(starts);

		for (File file : listFiles) {
			if (!file.getName().startsWith(".") && file.getName().toLowerCase().endsWith(".gpx")) {
				try {
					processfile(file, fout, bikeTimeEval);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static GregorianCalendar[] getTdfStarts() {
		List<GregorianCalendar> starts = new ArrayList<GregorianCalendar>();

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 10);
		start.set(Calendar.MONTH, 5); // Jan. = 0!
		start.set(Calendar.YEAR, 2017);
		start.set(Calendar.HOUR_OF_DAY, 5);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts.add(start);

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 22);
		start.set(Calendar.MONTH, 7); // Jan. = 0!
		start.set(Calendar.YEAR, 2016);
		start.set(Calendar.HOUR_OF_DAY, 12);
		start.set(Calendar.MINUTE, 29);
		start.set(Calendar.SECOND, 48);
		start.set(Calendar.MILLISECOND, 0);
		starts.add(start);

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 22);
		start.set(Calendar.MONTH, 7); // Jan. = 0!
		start.set(Calendar.YEAR, 2016);
		start.set(Calendar.HOUR_OF_DAY, 15);
		start.set(Calendar.MINUTE, 47);
		start.set(Calendar.SECOND, 52);
		start.set(Calendar.MILLISECOND, 0);
		starts.add(start);

		return starts.toArray(new GregorianCalendar[starts.size()]);
	}

	private static GregorianCalendar[] getTmpStarts() {
		GregorianCalendar[] starts = new GregorianCalendar[1];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 23);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2017);
		start.set(Calendar.HOUR_OF_DAY, 8);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		//		start = new GregorianCalendar();
		//		start.set(Calendar.DAY_OF_MONTH, 21);
		//		start.set(Calendar.MONTH, 5); // Jan. = 0!
		//		start.set(Calendar.YEAR, 2016);
		//		start.set(Calendar.HOUR_OF_DAY, 5);
		//		start.set(Calendar.MINUTE, 0);
		//		start.set(Calendar.SECOND, 0);
		//		start.set(Calendar.MILLISECOND, 0);
		//		starts[1] = start;
		return starts;
	}

	private static GregorianCalendar[] getFixStarts() {
		List<GregorianCalendar> starts = new ArrayList<GregorianCalendar>();

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 27);
		start.set(Calendar.MONTH, 7); // Jan. = 0!
		start.set(Calendar.YEAR, 2016);
		start.set(Calendar.HOUR_OF_DAY, 12);
		start.set(Calendar.MINUTE, 1);
		start.set(Calendar.SECOND, 13);
		start.set(Calendar.MILLISECOND, 0);
		starts.add(start);

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 22);
		start.set(Calendar.MONTH, 7); // Jan. = 0!
		start.set(Calendar.YEAR, 2016);
		start.set(Calendar.HOUR_OF_DAY, 12);
		start.set(Calendar.MINUTE, 29);
		start.set(Calendar.SECOND, 48);
		start.set(Calendar.MILLISECOND, 0);
		starts.add(start);

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 22);
		start.set(Calendar.MONTH, 7); // Jan. = 0!
		start.set(Calendar.YEAR, 2016);
		start.set(Calendar.HOUR_OF_DAY, 15);
		start.set(Calendar.MINUTE, 47);
		start.set(Calendar.SECOND, 52);
		start.set(Calendar.MILLISECOND, 0);
		starts.add(start);

		return starts.toArray(new GregorianCalendar[starts.size()]);
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

	private static void processfile(File file, File fout, GPXBikeTimeEval bikeTimeEval) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(file);

		List<GPXPath> paths = GPXParser.parsePaths(gpxFile, true, null);
		for (GPXPath gpxPath : paths) {
			gpxPath.postProcess(bikeTimeEval);
			System.out.println(gpxPath.getName());
			System.out.println("min elevation : " + gpxPath.getMinElevation());
			System.out.println("max elevation : " + gpxPath.getMaxElevation());
			System.out.println("total elevation : " + gpxPath.getTotalElevation());
			GPXFileWriter.writeGpxFile(Collections.singletonList(gpxPath),
					new File(fout.getAbsolutePath() + "/" + gpxPath.getName() + "-" + file.getName()));
		}
		System.out.println(file.getName());
		GPXFileWriter.writeGpxFile(paths, new File(fout.getAbsolutePath() + "/" + file.getName()));
		for (GPXPath gpxPath : paths) {
			GPXCharter.createChartAndMap(gpxPath, fout.getAbsolutePath() + "/" + file.getName(), 0);
		}
	}

}
