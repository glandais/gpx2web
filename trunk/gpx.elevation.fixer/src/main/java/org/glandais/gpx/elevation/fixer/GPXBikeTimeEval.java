package org.glandais.gpx.elevation.fixer;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class GPXBikeTimeEval {

	DecimalFormat speedformat = new DecimalFormat("#0.0");
	// poids en kg
	private double m;

	// puissance moyenne
	private double max_power;
	private double freewheel_power;

	// g
	private double g = 9.8;

	private Calendar[] starts = { Calendar.getInstance() };
	private int counter = 0;
	private int dday = 0;

	/**
	 * @param m
	 *            kg
	 * @param puissance
	 *            W
	 */
	public GPXBikeTimeEval(double m, double puissance, double freewheel_power) {
		super();
		this.m = m;
		this.max_power = puissance;
		this.freewheel_power = freewheel_power;
	}

	public void setStarts(Calendar[] starts) {
		this.starts = starts;
	}

	public Calendar getNextStart() {
		if (counter < starts.length) {
			Calendar start = starts[counter];
			counter++;
			return start;
		} else {
			dday++;
			GregorianCalendar start = (GregorianCalendar) starts[starts.length - 1]
					.clone();
			start.add(Calendar.DAY_OF_YEAR, dday);
			return start;
		}
	}

	/**
	 * @param speed
	 *            km/h
	 * @param dist
	 *            km
	 * @param grad
	 *            %
	 * @return
	 */
	public double getTimeForDist(double speed, double dist, double grad) {
		// we will split the distance
		// m/h
		double v = speed / 3.6;
		// m
		double dmax = dist * 1000.0;
		// m
		double d = 0.0;
		// s
		double t = 0.0;
		// s
		double dt = 0.25;
		while (true) {
			// OK
			// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
			double p_frot = 0.26 * v * v * v + 0.1 * m * v;
			// OK
			// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
			double p_grav = m * g * v * Math.sin(Math.atan(grad / 100.0));
			// total resistance power
			double p_res = p_frot + p_grav;

			double p_cyclist = max_power;
			if (p_grav < -max_power + freewheel_power) {
				p_cyclist = freewheel_power;
			} else if (p_grav > -max_power + freewheel_power && p_grav < 0) {
				p_cyclist = p_grav + max_power;
			}

			// p_app = cyclist power - resistance
			double p_app = p_cyclist - p_res;

			System.out.println(speedformat.format(p_app) + " (="
					+ speedformat.format(p_cyclist) + " - "
					+ speedformat.format(p_res) + " (= "
					+ speedformat.format(p_frot) + " + "
					+ speedformat.format(p_grav) + ") (v = "
					+ speedformat.format(v * 3.6) + " , pente = "
					+ speedformat.format(grad) + ")");

			// m.s-2
			double acc = p_app / m;
			v = v + acc * dt;
			double dx = dt * v;
			d = d + dx;
			t = t + dt;
			if (d > dmax) {
				double ratio = (dmax - (d - dx)) / dx;
				return t - dt + dt * ratio;
			}
		}
	}
}
