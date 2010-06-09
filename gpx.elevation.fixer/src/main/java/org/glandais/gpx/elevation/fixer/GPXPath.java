package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Element;

public class GPXPath {

	private double minElevation = 20000;
	private double maxElevation = -10000;
	private double totalElevation = 0;
	private double previousElevation = -9999;
	private double minlon = 180;
	private double maxlon = -180;
	private double minlat = 180;
	private double maxlat = -180;
	private long currentTime = 0;
	DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

	private List<GPXPoint> points = new ArrayList<GPXPoint>();
	private String name;
	private GPXBikeTimeEval bikeTimeEval;

	public GPXPath(String name, GPXBikeTimeEval bikeTimeEval) {
		super();
		this.name = name;
		this.bikeTimeEval = bikeTimeEval;
	}

	public String getName() {
		return name;
	}

	public void showStats() {
		System.out.println(name);
		System.out.println("min elevation : " + minElevation);
		System.out.println("max elevation : " + maxElevation);
		System.out.println("total elevation : " + totalElevation);
	}

	public void processPoint(double lon, double lat, Element eleEle,
			Element timeEle) throws SRTMException {
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

		GPXPoint p = new GPXPoint(lon, lat, eleEle, timeEle);
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
		// zs = postProcess(dists, zs);
		return new double[][] { dists, zs };
	}

	public void createMap(String outputFile, int maxsize) throws Exception {
		String imgPath = outputFile + name + ".map.png";
		SRTMImageProducer imageProducer = new SRTMImageProducer(minlon, maxlon,
				minlat, maxlat, maxsize, 0.2);
		imageProducer.fillWithZ();
		imageProducer.addPoints(points, minElevation, maxElevation);
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
//		double dsample = 0.25;
		double dsample = 1;

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

	public void postProcess() {
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

		GregorianCalendar today = new GregorianCalendar();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		currentTime = today.getTimeInMillis();
		for (int j = 0; j < zs.length; j++) {
			GPXPoint p = points.get(j);
			p.setZ(zs[j]);
			p.getEleEle().setTextContent(Double.toString(zs[j]));
			if (j > 0) {
				Point prevPoint = points.get(j - 1);
				// en km double
				double dist = prevPoint.distanceTo(p);
				if (dist > 0.005) {
					double dz = p.getZ() - prevPoint.getZ();
					double pente = (dz * 0.1) / dist;
					// vitesse en km/h
					double vitesse = bikeTimeEval.getVitesse(pente); // temps!
					long ts = Math.round((dist / vitesse) * 60 * 60 * 1000);
					currentTime = currentTime + ts;
				}
			}
			String time = fmt.print(currentTime);
			p.getTimeEle().setTextContent(time);
		}
	}
}
