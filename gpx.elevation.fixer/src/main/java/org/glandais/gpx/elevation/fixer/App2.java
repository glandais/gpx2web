package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

public class App2 {

	public static void main(String[] args) {

		int tmax = 600;
		double[] r_x = new double[tmax + 1];
		double[] r_alti = new double[tmax + 1];
		double[] r_vitesse = new double[tmax + 1];
		double[] r_pres = new double[tmax + 1];
		double[] r_preal = new double[tmax + 1];

		double t = 0;
		double dt = 1;

		double m = 85.0;
		double p = 250.0;
		double p_roueLibre = 50.0;
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(m, 300, 100);
		bikeTimeEval.getTimeForDist(0.0, 10, -3);
		bikeTimeEval.getTimeForDist(0.0, 10, -6);
		bikeTimeEval.getTimeForDist(0.0, 10, 0);
		bikeTimeEval.getTimeForDist(0.0, 10, 6);
		/*
				double z = 0;
				double x = 0;
				double v = 0;

				System.out.println("P cycliste = " + p);
				for (int i = 0; i <= tmax; i++) {
					// double pente = 0;
					double pente = -12.0;// * Math.sin(x * Math.PI / (1.0 * 500));
					System.out.println("t = " + t);
					System.out.println("pente = " + pente + " %");

					double p_res = 0.26 * v * v * v + 0.1 * m * v + m * g * v
							* Math.sin(Math.atan(pente / 100.0));
					System.out.println("P res = " + p_res);

					double p_real = p;
					if (p_res < p_res_min) {
						p_real = 0;
					} else {
						p_real = Math.min(p, p_res - p_res_min);
					}
					System.out.println("real_p = " + p_real + " W");

					double p_app = p_real - p_res;

					// double acc = p_app / (v * m);
					double acc = p_app / m;
					System.out.println("acc = " + acc * 3.6 + " km/h en 1s");
					v = v + acc * dt;

					System.out.println("v = " + (v * 3.6) + " km/h");

					t = t + dt;
					double dx = dt * v;
					x = x + dx;
					z = z + dx * (pente / 100.0);

					r_x[i] = x;
					r_alti[i] = z;
					r_vitesse[i] = v * 3.6;
					r_pres[i] = p_res;
					r_preal[i] = p_real;
				}

				createChart(r_x, r_alti, r_vitesse, r_pres, r_preal);
				*/
	}

	private static void createChart(double[] rX, double[] rAlti,
			double[] rVitesse, double[] rPres, double[] rPreal) {
		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("alti", getSerie(rX, rAlti));
		dataset.addSeries("vitesse", getSerie(rX, rVitesse));
		dataset.addSeries("pres", getSerie(rX, rPres));
		dataset.addSeries("preal", getSerie(rX, rPreal));

		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z",
				dataset, PlotOrientation.VERTICAL, false, false, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYItemRenderer rendu = new XYLineAndShapeRenderer();
		plot.setRenderer(1, rendu);
		try {
			ChartUtilities.saveChartAsPNG(new File("d:\\chart.png"), chart,
					1920, 1080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static double[][] getSerie(double[] rX, double[] rAlti) {
		return new double[][] { rX, rAlti };
	}

}
