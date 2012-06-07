package org.glandais.gpx.braquet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;
import org.glandais.srtm.loader.Point;

public class Braquet {

	public static final int INDEX_CRANKSET_CHANGES = 0;

	public static final int INDEX_COGSET_CHANGES = 1;

	public static final int INDEX_LOW_RPM = 2;

	public static final int INDEX_HIGH_RPM = 3;

	// private static final double ratio_crank = 5.0;
	// private static final double ratio_cog = 2.0;
	// private static final double ratio_low = 10.0;
	// private static final double ratio_high = 5.0;

	public Pedalier pedalier;

	public Cassette cassette;

	public int iPlateau;
	public int iPignon;

	public long timeMissingLow;
	public long timeMissingHigh;
	public int pedalierChanges;
	public int cassetteChanges;

	private double[] normRatios;

	public List<PointBraquet> history;

	private long timeSinceShift;
	private double totalDist;

	public Braquet(Pedalier pedalier, Cassette cassette) {
		super();
		this.pedalier = pedalier;
		this.cassette = cassette;
		reset();
	}

	public void reset() {
		iPlateau = 0;
		iPignon = cassette.pignons.length - 1;

		timeMissingLow = 0;
		timeMissingHigh = 0;
		pedalierChanges = 0;
		cassetteChanges = 0;

		timeSinceShift = 0;
		totalDist = 0;
		history = new ArrayList<PointBraquet>();
	}

	public void applySpeed(Point point, double curSpeed, long dt, double dist) throws IOException {
		totalDist = totalDist + dist;
		if (curSpeed > 2.0) {
			int oldPlateau = iPlateau;
			int oldPignon = iPignon;

			timeSinceShift = timeSinceShift + dt;

			double rpm = getRpm(curSpeed, iPignon, iPlateau);
			if (rpm < 80) {
				if (!changeCassette(+1, curSpeed)) {
					if (rpm < 70) {
						timeMissingLow += dt / 1000;
					}
				}
			} else if (rpm > 90) {
				if (!changeCassette(-1, curSpeed)) {
					if (rpm > 100) {
						timeMissingHigh += dt / 1000;
					}
				}
			}

			boolean changed = oldPignon != iPignon || oldPlateau != iPlateau;

			if (changed) {
				timeSinceShift = 0;
				rpm = getRpm(curSpeed, iPignon, iPlateau);
			}
			PointBraquet pointBraquet = new PointBraquet(this, point, totalDist, iPlateau, iPignon, rpm, curSpeed,
					timeSinceShift);
			history.add(pointBraquet);
		}
	}

	private double getRpm(double curSpeed, int iPignon, int iPlateau) {
		// km
		double developpement = ((1.0 * pedalier.plateaux[iPlateau]) / (1.0 * cassette.pignons[iPignon])) * 0.0007
				* Math.PI;
		// RPM (tours/min)
		// tours/h
		double rpm = (curSpeed / developpement) / 60.0;
		return rpm;
	}

	private boolean changeCassette(int dec, double curSpeed) {
		if (dec < 0 && isMinPignon(iPlateau)) {
			if (changePedalier(+1)) {
				changeCassette(0, curSpeed);
			} else {
				return false;
			}
		}
		if (dec > 0 && isMaxPignon(iPlateau)) {
			if (changePedalier(-1)) {
				changeCassette(0, curSpeed);
			} else {
				return false;
			}
		}
		trouvePignon(curSpeed);
		return true;
	}

	private boolean isMaxPignon(int plateau) {
		int maxPignon = getMaxPignon(plateau);
		return iPignon >= maxPignon;
	}

	private boolean isMinPignon(int plateau) {
		int pignonsImpossibles = getMinPignon(plateau);
		// pignonsImpossibles = 0;
		return iPignon <= pignonsImpossibles;
	}

	private int getMinPignon(int plateau) {
		int pignonsImpossibles = 0;
		if (pedalier.plateaux.length == 2) {
			// double plateau

			// petit plateau
			if (plateau == 0) {
				pignonsImpossibles = 1;
			}
		} else {
			// triple plateau

			if (plateau == 0) {
				// petit plateau
				pignonsImpossibles = 2;
			} else if (plateau == 1) {
				// plateau intermediaire
				pignonsImpossibles = 1;
			}
		}
		if (cassette.pignons.length >= 10 && pignonsImpossibles > 0) {
			pignonsImpossibles = pignonsImpossibles + 1;
		}
		return pignonsImpossibles;
	}

	private int getMaxPignon(int plateau) {
		int pignonsImpossibles = 0;
		// grand plateau
		if (plateau == pedalier.plateaux.length - 1) {
			pignonsImpossibles = 2;
		}
		if (cassette.pignons.length >= 10 && pignonsImpossibles > 0) {
			pignonsImpossibles = pignonsImpossibles + 1;
		}
		// pignonsImpossibles = 0;
		int maxPignon = cassette.pignons.length - 1 - pignonsImpossibles;
		return maxPignon;
	}

	private void trouvePignon(double curSpeed) {
		int bestI = findBestCog(curSpeed);
		cassetteChanges = cassetteChanges + Math.abs(iPignon - bestI);
		iPignon = bestI;
	}

	private int findBestCog(double curSpeed) {
		double bestrpm = Double.MAX_VALUE;
		int bestI = -1;
		int minPignon = getMinPignon(iPlateau);
		int maxPignon = getMaxPignon(iPlateau);
		for (int i = minPignon; i < maxPignon + 1; i++) {
			double rpm = getRpm(curSpeed, i, iPlateau);
			rpm = Math.abs(80.0 - rpm);
			if (rpm < bestrpm) {
				bestI = i;
				bestrpm = rpm;
			}
		}
		return bestI;
	}

	private boolean changePedalier(int dec) {
		if (dec < 0 && iPlateau == 0) {
			return false;
		}
		if (dec > 0 && iPlateau == pedalier.plateaux.length - 1) {
			return false;
		}
		iPlateau = iPlateau + dec;
		pedalierChanges += 1;
		return true;
	}

	public Double getScore(double[] ratios) {
		return (normRatios[INDEX_CRANKSET_CHANGES] * ratios[INDEX_CRANKSET_CHANGES] + normRatios[INDEX_COGSET_CHANGES]
				* ratios[INDEX_COGSET_CHANGES] + normRatios[INDEX_LOW_RPM] * ratios[INDEX_LOW_RPM] + normRatios[INDEX_HIGH_RPM]
				* ratios[INDEX_HIGH_RPM])
				/ (ratios[INDEX_CRANKSET_CHANGES] + ratios[INDEX_COGSET_CHANGES] + ratios[INDEX_LOW_RPM] + ratios[INDEX_HIGH_RPM]);
	}

	// public int compareTo(Braquet o) {
	// return getScore().compareTo(o.getScore());
	// }

	@Override
	public String toString() {
		return "Braquet [pedalier=" + pedalier + ", cassette=" + cassette + ", timeMissingLow=" + timeMissingLow
				+ ", timeMissingHigh=" + timeMissingHigh + ", pedalierChanges=" + pedalierChanges
				+ ", cassetteChanges=" + cassetteChanges + "]";
	}

	public double[] getRatios() {
		double[] result = new double[4];
		result[INDEX_CRANKSET_CHANGES] = pedalierChanges;
		result[INDEX_COGSET_CHANGES] = cassetteChanges;
		result[INDEX_LOW_RPM] = timeMissingLow;
		result[INDEX_HIGH_RPM] = timeMissingHigh;
		return result;
	}

	public void setNormRatios(double[] ds) {
		this.normRatios = ds;
	}

}
