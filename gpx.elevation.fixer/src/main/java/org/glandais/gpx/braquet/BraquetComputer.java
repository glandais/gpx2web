package org.glandais.gpx.braquet;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;
import org.glandais.gpx.elevation.fixer.GPXPath;

public class BraquetComputer {

	private List<Braquet> braquets;

	public BraquetComputer() {
		super();
		braquets = new ArrayList<Braquet>();

		List<Pedalier> pedaliersSansDoublon = new ArrayList<Pedalier>();
		Pedalier[] pedaliers = Pedalier.values();
		for (Pedalier pedalier : pedaliers) {
			boolean add = true;
			for (Pedalier pedalierTest : pedaliersSansDoublon) {
				if (add) {
					if (Arrays.equals(pedalier.plateaux, pedalierTest.plateaux)) {
						add = false;
					}
				}
			}
			if (add) {
				pedaliersSansDoublon.add(pedalier);
			}
		}

		List<Cassette> cassettesSansDoublon = new ArrayList<Cassette>();
		Cassette[] cassettes = Cassette.values();
		for (Cassette cassette : cassettes) {
			boolean add = true;
			for (Cassette cassetteTest : cassettesSansDoublon) {
				if (add) {
					if (Arrays.equals(cassette.pignons, cassetteTest.pignons)) {
						add = false;
					}
				}
			}
			if (add) {
				cassettesSansDoublon.add(cassette);
			}
		}

		for (Pedalier pedalier : pedaliersSansDoublon) {
			for (Cassette cassette : cassettesSansDoublon) {
				Braquet braquet = new Braquet(pedalier, cassette);
				braquets.add(braquet);
			}
		}

	}

	public List<Braquet> getBraquets() {
		return braquets;
	}

	public void parseGPX(List<GPXPath> paths, BufferedWriter writer) throws Exception {
		for (Braquet braquetDisp : braquets) {
			braquetDisp.reset();
		}

		for (GPXPath gpxPath : paths) {
			gpxPath.tryBraquets(braquets, false, writer);
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
				if (maxRatio[i] == minRatio[i]) {
					braquetRatios[b][i] = 1.0;
				} else {
					braquetRatios[b][i] = (braquetRatios[b][i] - minRatio[i]) / (maxRatio[i] - minRatio[i]);
				}
			}
			braquetDisp.setNormRatios(braquetRatios[b]);
			b++;
		}

		Collections.sort(braquets);
	}

}
