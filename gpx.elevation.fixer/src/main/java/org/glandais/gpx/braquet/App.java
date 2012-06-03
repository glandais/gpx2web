package org.glandais.gpx.braquet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;
import org.glandais.gpx.elevation.fixer.GPXParser;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.w3c.dom.Document;

public class App {

	public static void main(String[] args) throws Exception {

		List<Braquet> braquets = new ArrayList<Braquet>();
		Pedalier[] pedaliers = Pedalier.values();
		Cassette[] cassettes = Cassette.values();
		for (Pedalier pedalier : pedaliers) {
			for (Cassette cassette : cassettes) {
				if (pedalier.compat == cassette.compat) {
					Braquet braquet = new Braquet(pedalier, cassette);
					braquets.add(braquet);
				}
			}
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(args[0]);

		List<GPXPath> paths = GPXParser.parsePaths(gpxFile, false);
		for (GPXPath gpxPath : paths) {
			gpxPath.tryBraquets(braquets, false);
		}

		double[][] braquetRatios = new double[braquets.size()][];
		double[] minRatio = new double[braquets.size()];
		double[] maxRatio = new double[braquets.size()];
		int b = 0;
		for (Braquet braquetDisp : braquets) {
			braquetRatios[b] = braquetDisp.getRatios();
			int i = 0;
			for (double d : braquetRatios[b]) {
				if (d < minRatio[i] || b == 0) {
					minRatio[i] = d;
				}
				if (d > maxRatio[i] || b == 0) {
					maxRatio[i] = d;
				}
				i++;
			}
			b++;
		}
		b = 0;
		for (Braquet braquetDisp : braquets) {
			for (int i = 0; i < braquetRatios[b].length; i++) {
				braquetRatios[b][i] = (braquetRatios[b][i] - minRatio[i])
						/ (maxRatio[i] - minRatio[i]);
			}
			braquetDisp.setNormRatios(braquetRatios[b]);
			b++;
		}

		Collections.sort(braquets);

		for (Braquet braquetDisp : braquets) {
			System.out.println(braquetDisp);
		}

		Braquet braquet = new Braquet(braquets.get(0).pedalier,
				braquets.get(0).cassette);
		for (GPXPath gpxPath : paths) {
			gpxPath.tryBraquets(Collections.singletonList(braquet), true);
		}

	}
}
