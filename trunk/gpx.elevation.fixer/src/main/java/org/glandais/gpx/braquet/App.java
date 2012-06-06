package org.glandais.gpx.braquet;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
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

		List<GPXPath> parsedPaths = GPXParser.parsePaths(gpxFile, false);
		List<GPXPath> paths = new ArrayList<GPXPath>();
		for (GPXPath gpxPath : parsedPaths) {
			paths.addAll(gpxPath.splitWithStops());
		}
		braquetComputer.parseGPX(paths, new BufferedWriter(new PrintWriter(System.out)));

		boolean first = true;
		for (Braquet braquetDisp : braquetComputer.getBraquets()) {
			System.out.println(braquetDisp);
			if (first) {
				for (String log : braquetDisp.history) {
					System.out.println(log);
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
