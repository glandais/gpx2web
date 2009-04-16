package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.glandais.srtm.loader.Point;
import org.glandais.srtm.loader.SRTMException;
import org.glandais.srtm.loader.SRTMHelper;
import org.glandais.srtm.loader.SRTMImageProducer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.w3c.dom.Element;

public class GPXPath {

	private double minElevation = 20000;
	private double maxElevation = -10000;
	private double totalElevation = 0;
	private double previousElevation = -9999;
	private double minlon = Double.MAX_VALUE;
	private double maxlon = Double.MIN_VALUE;
	private double minlat = Double.MAX_VALUE;
	private double maxlat = Double.MIN_VALUE;

	private List<Point> points = new ArrayList<Point>();
	private String name;

	public GPXPath(String name) {
		super();
		this.name = name;
	}

	public void showStats() {
		System.out.println(name);
		System.out.println("min elevation : " + minElevation);
		System.out.println("max elevation : " + maxElevation);
		System.out.println("total elevation : " + totalElevation);
	}

	public void processPoint(double lon, double lat, Element ele)
			throws SRTMException {
		double elevation = -100;
		
		if (lon < minlon) {
			minlon = lon;
		}
		if (lon > maxlon) {
			maxlon = lon;
		}
		if (lat < minlat) {
			minlat = lat;
		}
		if (lat > maxlat) {
			maxlat = lat;
		}

		elevation = SRTMHelper.getInstance().getElevation(lon, lat);
		ele.setTextContent(Double.toString(elevation));

		if (elevation < minElevation) {
			minElevation = elevation;
		}
		if (elevation > maxElevation) {
			maxElevation = elevation;
		}
		if (previousElevation != -9999) {
			double dz = elevation - previousElevation;
			if (dz > 0) {
				totalElevation += dz;
			}
		}
		Point p = new Point(lon, lat);
		p.setZ(elevation);
		points.add(p);

		previousElevation = elevation;
	}

	private double[][] getSerie() {
		double[] dists = new double[points.size()];
		double[] zs = new double[points.size()];
		Point previousPoint = null;
		int i = 0;
		double d = 0;
		for (Point p : points) {
			if (previousPoint != null) {
				double dz = previousPoint.distanceTo(p);
				if (Double.toString(dz).contains("NaN")) {
					dz = 0;
				}
				d += dz;
			}
			dists[i] = d;
			zs[i] = p.getZ();
			previousPoint = p;
			i++;
		}
		zs = postProcess(dists, zs);
		return new double[][] { dists, zs };
	}

	public void createMap(String outputFile) throws Exception {
		String imgPath = outputFile + name + ".map.png";
		SRTMImageProducer imageProducer = new SRTMImageProducer(minlon, maxlon,
				minlat, maxlat, 300, 0.2);
		imageProducer.fillWithZ();
		imageProducer.addPoints(points);
		imageProducer.saveImage(imgPath);
	}

	public void createChart(String outputFile) {
		String imgPath = outputFile + name + ".png";

		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("", getSerie());
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z",
				dataset, PlotOrientation.VERTICAL, false, false, false);

		XYPlot plot = (XYPlot) chart.getPlot();

		XYItemRenderer rendu = new XYLineAndShapeRenderer();
		plot.setRenderer(1, rendu);

		try {
			ChartUtilities.saveChartAsPNG(new File(imgPath), chart, 1680, 1050);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private double[] postProcess(double[] dists, double[] zs) {
		double[] newZs = new double[zs.length];
		for (int i = 0; i < newZs.length; i++) {
			newZs[i] = computeNewZ(dists, zs, i);
		}
		return newZs;
	}

	private double computeNewZ(double[] dists, double[] zs, int i) {
		double dsample = 0.25;

		double ac = dists[i];

		int mini = i - 1;
		while (mini >= 0 && (ac - dists[mini]) <= dsample) {
			mini--;
		}
		mini++;

		int maxi = i + 1;
		while (maxi < zs.length && (dists[maxi] - ac) <= dsample) {
			maxi++;
		}

		double totc = 0;
		double totz = 0;
		for (int j = mini; j < maxi; j++) {
			double c = 1 - (Math.abs(dists[j] - ac) / dsample);
			totc = totc + c;
			totz = totz + zs[j] * c;
		}

		if (totc == 0) {
			return zs[i];
		} else {
			return totz / totc;
		}

	}
}
