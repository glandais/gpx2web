package org.glandais.gpx.braquet;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glandais.gpx.elevation.fixer.GPXParser;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.w3c.dom.Document;

public class App {

	public static void main(String[] args) throws Exception {

		BraquetComputer braquetComputer = new BraquetComputer();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(args[0]);

		List<GPXPath> paths = GPXParser.parsePaths(gpxFile, false);
		braquetComputer.parseGPX(paths, new BraquetProgress() {

			public void progress(int i, int size) {
				System.out.println(i + " / " + size);
			}
		});

		boolean first = true;
		for (Braquet braquetDisp : braquetComputer.getBraquets()) {
			System.out.println(braquetDisp);
			if (first) {
				for (PointBraquet log : braquetDisp.history) {
					System.out.println(log.toString());
				}
				first = false;
			}
		}

		// Braquet braquet = new Braquet(braquets.get(0).pedalier,
		// braquets.get(0).cassette);
		// for (GPXPath gpxPath : paths) {
		// gpxPath.tryBraquets(Collections.singletonList(braquet), true, new
		// BufferedWriter(
		// new PrintWriter(System.out)));
		// }

	}
}
