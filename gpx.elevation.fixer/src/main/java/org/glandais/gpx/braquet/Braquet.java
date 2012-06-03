package org.glandais.gpx.braquet;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;

public class Braquet implements Comparable<Braquet> {

	Pedalier pedalier;

	Cassette cassette;

	public int curPlateau;
	public int curPignon;

	private long timeMissingLow;
	private long timeMissingHigh;
	private int pedalierChanges;
	private int cassetteChanges;

	private double[] normRatios;

	public Braquet(Pedalier pedalier, Cassette cassette) {
		super();
		this.pedalier = pedalier;
		curPlateau = pedalier.plateaux[0];
		this.cassette = cassette;
		curPignon = cassette.pignons[cassette.pignons.length - 1];

		timeMissingLow = 0;
		timeMissingHigh = 0;
		pedalierChanges = 0;
		cassetteChanges = 0;
	}

	public boolean applySpeed(double curSpeed, long dt, boolean verbose) {
		int oldPlateau = curPlateau;
		int oldPignon = curPignon;

		if (curSpeed > 2.0) {
			double rpm = getRpm(curSpeed, curPignon, curPlateau);
			if (rpm < 70) {
				if (!changeCassette(+1, curSpeed)) {
					if (verbose) {
						System.out.println("");
						System.out.println("********** " + curSpeed + " - "
								+ dt + " - " + rpm);
					}
					timeMissingLow += dt;
				}
			} else if (rpm > 90) {
				if (!changeCassette(-1, curSpeed)) {
					timeMissingHigh += dt;
				}
			}
		}

		return (oldPignon != curPignon || oldPlateau != curPlateau);
	}

	private double getRpm(double curSpeed, int pignon, int plateau) {
		// km
		double developpement = ((1.0 * plateau) / (1.0 * pignon)) * 0.0007
				* Math.PI;
		// RPM (tours/min)
		// tours/h
		double rpm = (curSpeed / developpement) / 60.0;
		return rpm;
	}

	private boolean changeCassette(int dec, double curSpeed) {
		if (dec < 0 && curPignon == cassette.pignons[0]) {
			if (changePedalier(+1)) {
				changeCassette(0, curSpeed);
			} else {
				return false;
			}
		}
		if (dec > 0
				&& curPignon == cassette.pignons[cassette.pignons.length - 1]) {
			if (changePedalier(-1)) {
				changeCassette(0, curSpeed);
			} else {
				return false;
			}
		}
		trouvePignon(curSpeed);
		return true;
	}

	private void trouvePignon(double curSpeed) {
		double bestrpm = 500;
		int bestI = -1;
		int curI = -1;
		for (int i = 0; i < cassette.pignons.length; i++) {
			int pignon = cassette.pignons[i];
			if (pignon == curPignon) {
				curI = i;
			}
			double rpm = getRpm(curSpeed, pignon, curPlateau);
			rpm = Math.abs(80.0 - rpm);
			if (rpm < bestrpm) {
				bestI = i;
				bestrpm = rpm;
			}
		}
		curPignon = cassette.pignons[bestI];
		cassetteChanges = cassetteChanges + Math.abs(curI - bestI);
	}

	private boolean changePedalier(int dec) {
		if (dec < 0 && curPlateau == pedalier.plateaux[0]) {
			return false;
		}
		if (dec > 0
				&& curPlateau == pedalier.plateaux[pedalier.plateaux.length - 1]) {
			return false;
		}
		int iPedalier = 0;
		for (int i = 0; i < pedalier.plateaux.length; i++) {
			if (curPlateau == pedalier.plateaux[i]) {
				iPedalier = i;
			}
		}
		curPlateau = pedalier.plateaux[iPedalier + dec];
		pedalierChanges += 1;
		return true;
	}

	public Double getScore() {
		return normRatios[2] * 10 + normRatios[0] * 5 + normRatios[1] * 2
				+ normRatios[3];
	}

	public int compareTo(Braquet o) {
		return getScore().compareTo(o.getScore());
	}

	@Override
	public String toString() {
		return "Braquet [pedalier=" + pedalier + ", cassette=" + cassette
				+ ", timeMissingLow=" + timeMissingLow + ", timeMissingHigh="
				+ timeMissingHigh + ", pedalierChanges=" + pedalierChanges
				+ ", cassetteChanges=" + cassetteChanges + "]";
	}

	public double[] getRatios() {
		double[] result = new double[4];
		result[0] = pedalierChanges;
		result[1] = cassetteChanges;
		result[2] = timeMissingLow;
		result[3] = timeMissingHigh;
		return result;
	}

	public void setNormRatios(double[] ds) {
		this.normRatios = ds;
	}

}
