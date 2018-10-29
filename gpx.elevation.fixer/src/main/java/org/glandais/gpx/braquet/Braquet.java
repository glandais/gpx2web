package org.glandais.gpx.braquet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;
import org.glandais.gpx.srtm.Point;

public class Braquet {

	public static final int INDEX_CRANKSET_CHANGES = 0;

	public static final int INDEX_COGSET_CHANGES = 1;

	public static final int INDEX_LOW_RPM = 2;

	public static final int INDEX_HIGH_RPM = 3;

	public static final int INDEX_STD_DEV_RPM = 4;

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
	public double rpmStandardDeviation;

	private double[] normRatios;

	public List<PointBraquet> history;

	private long timeSinceShift;
	private double totalDist;

	private boolean first;

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

		first = true;
	}

	public void applySpeed(Point point, double curSpeed, long dt, double dist) throws IOException {
		totalDist = totalDist + dist;
		if (curSpeed > 2.0) {
			if (first) {
				double bestRpm = 1000;
				for (int i = 0; i < pedalier.plateaux.length; i++) {
					int pignon = findBestCog(curSpeed);
					double rpm = Math.abs(getRpm(curSpeed, pignon, i) - 80);
					if (rpm < bestRpm) {
						iPlateau = i;
						iPignon = pignon;
						bestRpm = rpm;
					}
				}
				first = false;
			} else {
				int oldPlateau = iPlateau;
				int oldPignon = iPignon;

				timeSinceShift = timeSinceShift + dt;

				boolean changed = fixShifting(curSpeed, dt);
				while (changed) {
					changed = fixShifting(curSpeed, dt);
				}

				changed = oldPignon != iPignon || oldPlateau != iPlateau;

				if (changed) {
					timeSinceShift = 0;
				}
			}
			PointBraquet pointBraquet = new PointBraquet(this, point, totalDist, iPlateau, iPignon,
					getRpm(curSpeed, iPignon, iPlateau), curSpeed, timeSinceShift);
			history.add(pointBraquet);
		}
	}

	private boolean fixShifting(double curSpeed, long dt) {
		double rpm = getRpm(curSpeed, iPignon, iPlateau);
		boolean changed = false;
		if (rpm < 80) {
			changed = changeCassette(+1, curSpeed);
			if (!changed) {
				if (rpm < 70) {
					timeMissingLow += dt / 1000;
				}
			}
		} else if (rpm > 90) {
			changed = changeCassette(-1, curSpeed);
			if (!changed) {
				if (rpm > 100) {
					timeMissingHigh += dt / 1000;
				}
			}
		}
		return changed;
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
		return trouvePignon(curSpeed);
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

	private boolean trouvePignon(double curSpeed) {
		int bestI = findBestCog(curSpeed);
		cassetteChanges = cassetteChanges + Math.abs(iPignon - bestI);
		boolean changed = (iPignon != bestI);
		iPignon = bestI;
		return changed;
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
		return (normRatios[INDEX_CRANKSET_CHANGES] * ratios[INDEX_CRANKSET_CHANGES]
				+ normRatios[INDEX_COGSET_CHANGES] * ratios[INDEX_COGSET_CHANGES]
				+ normRatios[INDEX_LOW_RPM] * ratios[INDEX_LOW_RPM]
				+ normRatios[INDEX_HIGH_RPM] * ratios[INDEX_HIGH_RPM]
				+ normRatios[INDEX_STD_DEV_RPM] * ratios[INDEX_STD_DEV_RPM])
				/ (ratios[INDEX_CRANKSET_CHANGES] + ratios[INDEX_COGSET_CHANGES] + ratios[INDEX_LOW_RPM]
						+ ratios[INDEX_HIGH_RPM] + ratios[INDEX_STD_DEV_RPM]);
	}

	// public int compareTo(Braquet o) {
	// return getScore().compareTo(o.getScore());
	// }

	@Override
	public String toString() {
		return "Braquet [pedalier=" + pedalier + ", cassette=" + cassette + ", timeMissingLow=" + timeMissingLow
				+ ", timeMissingHigh=" + timeMissingHigh + ", pedalierChanges=" + pedalierChanges + ", cassetteChanges="
				+ cassetteChanges + "]";
	}

	public double[] getRatios() {
		double[] result = new double[5];
		result[INDEX_CRANKSET_CHANGES] = pedalierChanges;
		result[INDEX_COGSET_CHANGES] = cassetteChanges;
		result[INDEX_LOW_RPM] = timeMissingLow;
		result[INDEX_HIGH_RPM] = timeMissingHigh;
		result[INDEX_STD_DEV_RPM] = rpmStandardDeviation;
		return result;
	}

	public void setNormRatios(double[] ds) {
		this.normRatios = ds;
	}

	public void computeRpmStandardDeviation() {
		if (history.size() > 2) {

			PointBraquet firstPoint = history.get(0);
			PointBraquet lastPoint = history.get(history.size() - 1);

			long totalTime = lastPoint.getPoint().getTime() - firstPoint.getPoint().getTime();
			int n = (int) (totalTime / 1000);

			List<Double> valueList = new ArrayList<Double>(n);

			long t = firstPoint.getPoint().getTime();
			for (int i = 1; i < history.size(); i++) {
				PointBraquet p1 = history.get(i - 1);
				PointBraquet p2 = history.get(i);
				long dt = (p2.getPoint().getTime() - p1.getPoint().getTime()) / 1000;
				if (dt > 0) {
					for (int j = 0; j < dt; j++) {
						valueList.add(p1.getRpm());
					}
				}
			}

			//			StandardDeviation sdComputer = new StandardDeviation();

			double[] values = new double[valueList.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = valueList.get(i);
			}

			//			rpmStandardDeviation = sdComputer.evaluate(values);
		}
	}
}
