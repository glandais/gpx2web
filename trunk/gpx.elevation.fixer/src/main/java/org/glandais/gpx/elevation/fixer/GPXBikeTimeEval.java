package org.glandais.gpx.elevation.fixer;

public class GPXBikeTimeEval {

	// poids en kg
	private double m;

	// puissance moyenne
	private double puissance;

	// g
	private double g = 9.8;

	private double penteMin = -25;
	private double penteMax = 25;
	private double dpente = 0.1;
	private int nPentes;
	private double[] vitesses;

	public GPXBikeTimeEval(double m, double p) {
		super();
		this.m = m;
		this.puissance = p;
		computeVitesses();
	}

	private void computeVitesses() {
		nPentes = (int) Math.round((penteMax - penteMin) / dpente);
		vitesses = new double[nPentes];

		for (int iPente = 0; iPente < nPentes; iPente++) {
			double p = penteMin + iPente * dpente + dpente / 2;
			Double vitesse = computeVitesse(p);
			vitesses[iPente] = vitesse * 3.6;
		}
	}

	private Double computeVitesse(double pente) {
		return computeVitesse(pente, 0, 11);
	}

	private Double computeVitesse(double pente, double vmin, double vmax) {
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
			return computeVitesse(pente, vmin, vmoy);
		} else {
			return computeVitesse(pente, vmoy, vmax);
		}
	}

	public static void main(String[] args) {
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(105, 150);
		for (int i = -40; i < 40; i++) {
			System.out.println((i / 2.0) + "% -> " + bikeTimeEval.getVitesse(i / 2.0));
		}
	}

	public double getVitesse(double pente) {
		int iPente = (int) Math.round((pente - penteMin) / dpente);
		if (iPente < 0) {
			iPente = 0;
		} else if (iPente >= nPentes) {
			iPente = nPentes - 1;
		}
		return vitesses[iPente];
	}
}
