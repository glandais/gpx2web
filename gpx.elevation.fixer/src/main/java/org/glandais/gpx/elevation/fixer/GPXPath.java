package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

	// wpt every 60mins
	private static final long interval = 15 * 60 * 1000;

	private double minElevation = 20000;
	private double maxElevation = -10000;
	private double totalElevation = 0;
	private double previousElevation = -9999;
	private double minlon = 180;
	private double maxlon = -180;
	private double minlat = 180;
	private double maxlat = -180;
	DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	DateTimeFormatter fmt2 = ISODateTimeFormat.hourMinute();
	DecimalFormat speedformat = new DecimalFormat("#0.0");

	private List<GPXPoint> points = new ArrayList<GPXPoint>();
	private List<Point> wptsGlobal;
	private List<CheckPoint> wptsPath;
	private String name;
	private GPXBikeTimeEval bikeTimeEval;

	private double[] dists;
	private double[] zs;
	private long[] timeMin;
	private long[] timeMax;

	public GPXPath(String name, GPXBikeTimeEval bikeTimeEval, List<Point> wpts) {
		super();
		this.name = name;
		this.bikeTimeEval = bikeTimeEval;
		this.wptsGlobal = wpts;
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
			ChartUtilities.saveChartAsPNG(new File(imgPath), chart, 1920, 1080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private double computeNewZ(int i) {
		// double dsample = 0.25;
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

	public List<Point> postProcess() {
		List<Point> wpts = new ArrayList<Point>();

		collectWptsPath();

		dists = new double[points.size()];
		zs = new double[points.size()];
		timeMin = new long[points.size()];
		timeMax = new long[points.size()];
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

		double[] newZs = new double[zs.length];
		for (int j = 0; j < newZs.length; j++) {
			newZs[j] = computeNewZ(j);
		}
		zs = newZs;

		GregorianCalendar today = new GregorianCalendar();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		long currentTimeMin = today.getTimeInMillis();
		long currentTimeMax = today.getTimeInMillis();
		double totdist = 0;

		for (int j = 0; j < zs.length; j++) {
			GPXPoint p = points.get(j);
			if (j == 0) {
				p.setCaption(name + " - 00:00");
				p.setDist(0);
				p.setTime(currentTimeMin);
				wpts.add(p);
			}
			p.setZ(zs[j]);
			p.getEleEle().setTextContent(Double.toString(zs[j]));
			double dist = 0;
			if (j > 0) {
				Point prevPoint = points.get(j - 1);
				// en km double
				dist = prevPoint.distanceTo(p);
				if (dist > 0.005) {
					double dz = p.getZ() - prevPoint.getZ();
					double pente = (dz * 0.1) / dist;
					// vitesse en km/h
					double vitesse = bikeTimeEval.getVitesseMin(pente); // temps!
					long ts = Math.round((dist / vitesse) * 60 * 60 * 1000);

					double previousTimeMin = currentTimeMin;
					currentTimeMin = currentTimeMin + ts;

					vitesse = bikeTimeEval.getVitesseMax(pente); // temps!
					ts = Math.round((dist / vitesse) * 60 * 60 * 1000);
					currentTimeMax = currentTimeMax + ts;

					int nprev = (int) Math.floor(previousTimeMin
							/ (interval * 1.0));
					int n = (int) Math
							.floor(currentTimeMin / (interval * 1.0));
					if (nprev != n) {
						double c = ((n * interval) - previousTimeMin)
								/ (ts * 1.0);
						double lon = prevPoint.getLon() + c
								* (p.getLon() - prevPoint.getLon());
						double lat = prevPoint.getLat() + c
								* (p.getLat() - prevPoint.getLat());
						Point wpt = new Point(lon, lat);
						wpt.setTime(n * interval);
						wpt.setDist(totdist + c * dist);
						wpt.setCaption(name
								+ " - "
								+ fmt2.print(n * interval)
								+ " ("
								+ getSpeedBetween(wpt, wpts
										.get(wpts.size() - 1)) + ")");
						wpts.add(wpt);
					}
				}
				totdist = totdist + dist;
			}
			timeMin[j] = currentTimeMin;
			timeMax[j] = currentTimeMax;
			String time = fmt.print(currentTimeMin);
			p.getTimeEle().setTextContent(time);
			if (j == zs.length - 1) {
				p.setDist(totdist);
				p.setTime(currentTimeMin);

				p.setCaption(name + " - " + fmt2.print(currentTimeMin) + " ("
						+ getSpeedBetween(p, wpts.get(wpts.size() - 1)) + ")");
				wpts.add(p);
			}

			if (j > 0) {
				for (CheckPoint checkPoint : wptsPath) {
					if (checkPoint.getpRefNext() == p) {
						long tmin = (long) (timeMin[j - 1] + checkPoint
								.getCoefRef()
								* (timeMin[j] - timeMin[j - 1]));
						long tmax = (long) (timeMax[j - 1] + checkPoint
								.getCoefRef()
								* (timeMax[j] - timeMax[j - 1]));
						double prevdist = totdist - dist;
						double distcp = prevdist + checkPoint.getCoefRef()
								* dist;

						checkPoint.setDist(distcp);
						checkPoint.setTmax(tmax);
						checkPoint.setTmin(tmin);
					}
				}
			}

		}
		return wpts;
	}

	private void collectWptsPath() {
		wptsPath = new ArrayList<CheckPoint>();
		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		for (Point pointGlobal : wptsGlobal) {
			double dmin = 1e15;
			Point pRef = null;
			Point pRefNext = null;
			double coefRef = 0;

			for (int i = 0; i < points.size() - 1; i++) {
				Point point = points.get(i);
				Point nextPoint = points.get(i + 1);
				d1 = pointGlobal.distanceTo(point);
				d2 = pointGlobal.distanceTo(nextPoint);
				if (d1 + d2 > 0.001) {
					d3 = point.distanceTo(nextPoint);
					if (d1 + d2 < d3 * 2) {
						if (d3 / (d1 + d2) < dmin) {
							dmin = d3 / (d1 + d2);
							pRef = point;
							pRefNext = nextPoint;
							coefRef = d1 / d3;
						}
					}
				}
			}

			if (pRef != null) {
				double lon = pRef.getLon() + coefRef
						* (pRefNext.getLon() - pRef.getLon());
				double lat = pRef.getLat() + coefRef
						* (pRefNext.getLat() - pRef.getLat());
				CheckPoint checkPoint = new CheckPoint(lon, lat);
				checkPoint.setCaption(pointGlobal.getCaption());
				checkPoint.setpRef(pRef);
				checkPoint.setpRefNext(pRefNext);
				checkPoint.setCoefRef(coefRef);
				wptsPath.add(checkPoint);
			}
		}
	}

	private String getSpeedBetween(Point wpt, Point prevwpt) {
		long t = wpt.getTime() - prevwpt.getTime();
		if (t > 500) {
			double dist = wpt.getDist() - prevwpt.getDist();
			double thour = (t / 1000.0) / 3600.0;
			double speed = dist / thour;
			return speedformat.format(speed);
		} else
			return "";
	}

	public List<CheckPoint> getWptsPath() {
		return wptsPath;
	}

}
