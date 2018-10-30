package org.glandais.gpx.braquet;

import java.io.File;
import java.util.List;

import org.glandais.gpx.elevation.fixer.GPXParser;
import org.glandais.gpx.elevation.fixer.GPXPath;

public class App {

	public static void main(String[] args) throws Exception {

		BraquetComputer braquetComputer = new BraquetComputer();

		List<GPXPath> paths = GPXParser.parsePaths(new File(args[0]));
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
