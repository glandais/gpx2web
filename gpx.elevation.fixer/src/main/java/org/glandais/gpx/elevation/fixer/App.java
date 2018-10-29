package org.glandais.gpx.elevation.fixer;

import java.io.File;
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
		// GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(85, 250, 100, 10, 50,
		// 0.1);

		GregorianCalendar[] starts = getTmpStarts();

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

	private static GregorianCalendar[] getTmpStarts() {
		GregorianCalendar[] starts = new GregorianCalendar[1];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 23);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2019);
		start.set(Calendar.HOUR_OF_DAY, 8);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		return starts;
	}

	private static void processfile(File file, File fout, GPXBikeTimeEval bikeTimeEval) throws Exception {
		System.out.println("Processing " + file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(file);

		List<GPXPath> paths = GPXParser.parsePaths(gpxFile, true, null);
		for (GPXPath gpxPath : paths) {
			gpxPath.postProcess(bikeTimeEval);
			System.out.println("stats for " + gpxPath.getName());
			System.out.println("min elevation : " + gpxPath.getMinElevation());
			System.out.println("max elevation : " + gpxPath.getMaxElevation());
			System.out.println("total elevation : " + gpxPath.getTotalElevation());
			GPXFileWriter.writeGpxFile(Collections.singletonList(gpxPath),
					new File(fout.getAbsolutePath() + "/" + file.getName() + "/" + gpxPath.getName() + ".gpx"));
			GPXCharter.createChartAndMap(gpxPath, fout.getAbsolutePath() + "/" + file.getName() + "/", 2048);
		}
		System.out.println(file.getName());
		GPXFileWriter.writeGpxFile(paths,
				new File(fout.getAbsolutePath() + "/" + file.getName() + "/" + file.getName()));
	}

}
