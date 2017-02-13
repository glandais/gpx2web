package org.glandais.gpx.router;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.glandais.gpx.elevation.fixer.GPXFileWriter;
import org.glandais.gpx.elevation.fixer.GPXParser;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.glandais.srtm.loader.Point;
import org.w3c.dom.Document;

public class App {

	public static void main(String[] args) throws Exception {
		List<Point> wpts = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(args[0]);
		GPXParser.parsePaths(gpxFile, false, wpts);
		Collections.sort(wpts, (p1, p2) -> p1.getCaption().compareTo(p2.getCaption()));
		for (int i = 0; i < wpts.size(); i++) {
			computeRoute(i, wpts.get(i), wpts.get((i + 1) % wpts.size()));
		}
	}

	private static void computeRoute(int i, Point p1, Point p2) throws Exception {
		System.out.println(p1.getCaption() + " -> " + p2.getCaption());
		String url = "http://gabriel.landais.org:8990/route?point=" + pointToString(p1) + "&point=" + pointToString(p2)
				+ "&type=gpx&vehicle=racingbike2";
		System.out.println(url);
		String file = "D:\\gpx\\routes\\" + i + ".gpx";
		FileUtils.copyURLToFile(new URL(url), new File(file));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(file);
		List<GPXPath> paths = GPXParser.parsePaths(gpxFile, false, null);
		paths.remove(0);
		if (i < 10) {
			paths.get(0).setName("route0" + i);
		} else {
			paths.get(0).setName("route" + i);
		}
		GPXFileWriter.writeGpxFile(paths, new File(file));
	}

	private static String pointToString(Point p1) {
		return p1.getLat() + "," + p1.getLon();
	}
}
