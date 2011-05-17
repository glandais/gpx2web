package org.glandais.gpx.elevation.fixer;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class GPXBikeTimeEval {

	// poids en kg
	private double m;

	// puissance moyenne
	private double puissanceMin;
	private double puissanceMax;

	// g
	private double g = 9.8;

	private double penteMin = -25;
	private double penteMax = 25;
	private double dpente = 0.1;
	private int nPentes;
	private double[] vitessesMin;
	private double[] vitessesMax;

	private Calendar[] starts = { Calendar.getInstance() };
	private int counter = 0;
	private int dday = 0;

	public GPXBikeTimeEval(double m, double puissanceMin, double puissanceMax) {
		super();
		this.m = m;
		this.puissanceMin = puissanceMin;
		this.puissanceMax = puissanceMax;
		computeVitesses();
	}

	private void computeVitesses() {
		nPentes = (int) Math.round((penteMax - penteMin) / dpente);
		vitessesMin = new double[nPentes];
		vitessesMax = new double[nPentes];

		for (int iPente = 0; iPente < nPentes; iPente++) {
			double p = penteMin + iPente * dpente + dpente / 2;
			Double vitesse = computeVitesse(puissanceMin, p);
			vitessesMin[iPente] = vitesse * 3.6;
			vitesse = computeVitesse(puissanceMax, p);
			vitessesMax[iPente] = vitesse * 3.6;
		}
	}

	private Double computeVitesse(double puissance, double pente) {
		return computeVitesse(puissance, pente, 0, (30 / 3.6));
	}

	private Double computeVitesse(double puissance, double pente, double vmin,
			double vmax) {
		double vmoy = (vmax + vmin) / 2.0;
		if (vmax - vmin < 0.01) {
			return vmoy;
		}
		double puissancemoy = 0.26 * vmoy * vmoy * vmoy + 0.1 * m * vmoy + m
				* g * vmoy * Math.sin(Math.atan(pente / 100.0));
		if (puissancemoy < 0) {
			puissancemoy = 0;
		}
		if (puissancemoy > puissance) {
			return computeVitesse(puissance, pente, vmin, vmoy);
		} else {
			return computeVitesse(puissance, pente, vmoy, vmax);
		}
	}

	public static void main(String[] args) {
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(105, 130, 160);
		for (int i = -40; i < 40; i++) {
			System.out.println((i / 2.0) + "% -> "
					+ bikeTimeEval.getVitesseMin(i / 2.0) + " / "
					+ bikeTimeEval.getVitesseMax(i / 2.0));
		}
	}

	public double getVitesseMin(double pente) {
		int iPente = iPente(pente);
		return vitessesMin[iPente];
	}

	public double getVitesseMax(double pente) {
		int iPente = iPente(pente);
		return vitessesMax[iPente];
	}

	private int iPente(double pente) {
		int iPente = (int) Math.round((pente - penteMin) / dpente);
		if (iPente < 0) {
			iPente = 0;
		} else if (iPente >= nPentes) {
			iPente = nPentes - 1;
		}
		return iPente;
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
}
