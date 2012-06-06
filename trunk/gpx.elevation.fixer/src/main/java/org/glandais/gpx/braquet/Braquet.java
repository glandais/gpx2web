package org.glandais.gpx.braquet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;

public class Braquet implements Comparable<Braquet> {

	private static final DecimalFormat SPEED_FORMAT = new DecimalFormat("#########0.0");
	private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#########0");
	private static final double ratio_crank = 5.0;
	private static final double ratio_cog = 2.0;
	private static final double ratio_low = 10.0;
	private static final double ratio_high = 5.0;

	public Pedalier pedalier;

	public Cassette cassette;

	public int iPlateau;
	public int iPignon;

	public long timeMissingLow;
	public long timeMissingHigh;
	public int pedalierChanges;
	public int cassetteChanges;

	private double[] normRatios;

	public List<String> history;

	private StringBuilder speeds;
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

		speeds = new StringBuilder();
		timeSinceShift = 0;
		totalDist = 0;
		history = new ArrayList<String>();
	}

	public boolean applySpeed(double curSpeed, long dt, double dist, boolean verbose, BufferedWriter writer)
			throws IOException {
		totalDist = totalDist + dist;
		if (curSpeed > 2.0) {
			int oldPlateau = iPlateau;
			int oldPignon = iPignon;

			speeds.append(" ").append(SPEED_FORMAT.format(curSpeed));
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
			if (changed || history.size() == 0) {
				String logStamp = SPEED_FORMAT.format(totalDist) + " - ";

				if (history.size() != 0) {
					history.add(logStamp + speeds.toString());
					speeds = new StringBuilder();
					String timeS = TIME_FORMAT.format(timeSinceShift / 1000.0);
					timeSinceShift = 0;

					history.add(logStamp + "Lasts for " + timeS + "s, changed due to speed "
							+ SPEED_FORMAT.format(curSpeed) + " (rpm " + SPEED_FORMAT.format(rpm) + ")");
				}
				double newRpm = getRpm(curSpeed, iPignon, iPlateau);
				history.add(logStamp + pedalier.plateaux[iPlateau] + " x " + cassette.pignons[iPignon] + " -> "
						+ SPEED_FORMAT.format(newRpm));
			}
			return changed;
		} else {
			return false;
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

	public Double getScore() {
		return (normRatios[2] * ratio_low + normRatios[0] * ratio_crank + normRatios[1] * ratio_cog + normRatios[3]
				* ratio_high)
				/ (ratio_low + ratio_crank + ratio_cog + ratio_high);
	}

	public int compareTo(Braquet o) {
		return getScore().compareTo(o.getScore());
	}

	@Override
	public String toString() {
		return "Braquet [score=" + getScore() + ", pedalier=" + pedalier + ", cassette=" + cassette
				+ ", timeMissingLow=" + timeMissingLow + ", timeMissingHigh=" + timeMissingHigh + ", pedalierChanges="
				+ pedalierChanges + ", cassetteChanges=" + cassetteChanges + "]";
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
