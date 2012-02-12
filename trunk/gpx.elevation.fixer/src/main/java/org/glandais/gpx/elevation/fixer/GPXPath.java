package org.glandais.gpx.elevation.fixer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.glandais.srtm.loader.Point;
import org.glandais.srtm.loader.SRTMException;
import org.glandais.srtm.loader.SRTMHelper;
import org.glandais.srtm.loader.SRTMImageProducer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
	private int counter = 0;

	private double[] dists;
	private double[] zs;
	private long[] timeMin;

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

	private double[][] getSerieDeriv() {
		double[] dzs = new double[zs.length];
		double lastdd = 0;
		for (int i = 0; i < dzs.length; i++) {
			int im1, ip1;
			if (i == 0) {
				im1 = 0;
			} else {
				im1 = i - 1;
			}
			if (i == dzs.length - 1) {
				ip1 = dzs.length - 1;
			} else {
				ip1 = i + 1;
			}

			double dz = zs[ip1] - zs[im1];
			double dd = dists[ip1] - dists[im1];
			if (dd < 1e-3) {
				dd = lastdd;
			} else {
				dd = dz / dd;
			}
			dzs[i] = dd;
			lastdd = dd;
		}
		return new double[][] { dists, dzs };
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
		String imgPathTimeMin = outputFile + name + "-timeMin.png";
		String imgPathTimeMax = outputFile + name + "-timeMax.png";
		String imgPathSmall = outputFile + name + "-small.png";
		String imgPathWeb = outputFile + name + "-web.png";

		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("", getSerie());

		createChartBig(imgPath, dataset);
		createChartTime(imgPathTimeMin, timeMin);
		// createChartTime(imgPathTimeMax, timeMax);
		createChartSmall(imgPathSmall, dataset);
		createChartWeb(imgPathWeb, dataset);
	}

	private void createChartTime(String imgPathTime, long[] time) {
		DateAxis dateAxis = new DateAxis("");

		DateTickUnit unit = null;
		unit = new DateTickUnit(DateTickUnit.MINUTE, 30);

		DateFormat chartFormatter = new SimpleDateFormat("HH:mm");
		dateAxis.setDateFormatOverride(chartFormatter);

		dateAxis.setTickUnit(unit);

		NumberAxis valueAxis = new NumberAxis("");

		XYSeries dataSeries = new XYSeries("");
		for (int i = 0; i < time.length; i++) {
			dataSeries.add(time[i], zs[i]);
		}
		XYSeriesCollection xyDataset = new XYSeriesCollection(dataSeries);

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		XYPlot plot = new XYPlot(xyDataset, dateAxis, valueAxis, null);
		plot.setRenderer(renderer);

		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT,
				plot, false);
		try {
			ChartUtilities.saveChartAsPNG(new File(imgPathTime), chart, 1920,
					1080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void createChartWeb(String imgPathWeb, DefaultXYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYAreaChart("", "", "", dataset,
				PlotOrientation.VERTICAL, false, false, false);
		chart.getXYPlot().getDomainAxis().setVisible(true);
		double distance = dists[dists.length - 1];
		chart.getXYPlot().getDomainAxis().setRange(0, distance);
		chart.getXYPlot().getRangeAxis().setVisible(true);
		chart.getXYPlot().getRangeAxis().setRange(minElevation, maxElevation);
		chart.setBackgroundPaint(Color.white);
		chart.getXYPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.blue);
		chart.getXYPlot().setRangeGridlinePaint(Color.blue);

		chart
				.addSubtitle(new TextTitle(speedformat.format(distance)
						+ " km     +"
						+ speedformat.format(totalElevation * 0.5) + " m"));

		try {
			ChartUtilities
					.saveChartAsPNG(new File(imgPathWeb), chart, 640, 480);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void createChartSmall(String imgPathSmall, DefaultXYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z",
				dataset, PlotOrientation.VERTICAL, false, false, false);

		chart.getXYPlot().getDomainAxis().setVisible(false);
		chart.getXYPlot().getRangeAxis().setVisible(false);
		chart.setBackgroundPaint(Color.white);
		chart.getXYPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.blue);
		chart.getXYPlot().setRangeGridlinePaint(Color.blue);

		try {
			ChartUtilities.saveChartAsPNG(new File(imgPathSmall), chart, 512,
					64);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void createChartBig(String imgPath, DefaultXYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z",
				dataset, PlotOrientation.VERTICAL, false, false, false);

		XYPlot plot = (XYPlot) chart.getPlot();

		for (CheckPoint checkPoint : wptsPath) {
			XYAnnotation annotation = new XYPointerAnnotation(checkPoint
					.getCaption(), checkPoint.getDist(), checkPoint.getZ(),
					-Math.PI / 2);
			plot.addAnnotation(annotation);
		}

		XYItemRenderer rendu = new XYLineAndShapeRenderer();
		plot.setRenderer(1, rendu);

		try {
			ChartUtilities.saveChartAsPNG(new File(imgPath), chart, 1920, 1080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void createChartDeriv(String outputFile) {
		String imgPath = outputFile + name + ".deriv.png";

		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("", getSerieDeriv());
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

		Calendar today = bikeTimeEval.getNextStart();

		long currentTimeMin = today.getTimeInMillis();
		double totdist = 0;
		double toteleplus = 0;
		double vitesse = 5.0;

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
			double diffele = 0;
			if (j > 0) {
				Point prevPoint = points.get(j - 1);
				// en km double
				dist = prevPoint.distanceTo(p);
				if (dist > 0.005) {
					double dz = p.getZ() - prevPoint.getZ();
					double pente = (dz * 0.1) / dist;
					
					// temps en secondes
					double time = bikeTimeEval.getTimeForDist(vitesse, dist, pente); // temps!
					// vitesse en km/h
					vitesse = dist / (time / (60 * 60));
					long ts = Math.round(time * 1000);

					double previousTimeMin = currentTimeMin;
					currentTimeMin = currentTimeMin + ts;

					int nprev = (int) Math.floor(previousTimeMin
							/ (interval * 1.0));
					int n = (int) Math.floor(currentTimeMin / (interval * 1.0));
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
						wpt.setCaption(fmt2.print(n * interval));
						// wpt.setCaption(name
						// + " - "
						// + fmt2.print(n * interval)
						// + " ("
						// + getSpeedBetween(wpt, wpts
						// .get(wpts.size() - 1)) + ")");
						wpts.add(wpt);
					}
				}
				totdist = totdist + dist;
				diffele = zs[j] - zs[j - 1];
				if (diffele > 0) {
					toteleplus = toteleplus + diffele;
				}
			}
			timeMin[j] = currentTimeMin;
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
						double prevdist = totdist - dist;
						double distcp = prevdist + checkPoint.getCoefRef()
								* dist;

						double prevele = toteleplus - diffele;
						double elecp = prevele + checkPoint.getCoefRef()
								* diffele;

						checkPoint.setDist(distcp);
						checkPoint.setDeniv(elecp);
						checkPoint.setTmin(tmin);
					}
				}
			}

		}

		// collectWptsMaxs(wpts);

		return wpts;
	}

	private void collectWptsMaxs(List<Point> wpts) {
		for (int i = 0; i < dists.length; i++) {
			int starti = -1;
			int endi = -1;
			for (int j = 0; j < dists.length; j++) {
				if (starti == -1 && j < i) {
					if (dists[i] - dists[j] < 0.5) {
						starti = j;
					}
				}
				if (endi == -1 && j > i) {
					if (dists[j] - dists[i] >= 0.5) {
						endi = j;
					}
				}
			}
			if (starti != -1 && endi != -1) {
				int imax = -1;
				double maxz = -1;
				for (int j = starti; j <= endi; j++) {
					if (zs[j] > maxz) {
						maxz = zs[j];
						imax = j;
					}
				}
				if (imax != starti && imax != endi) {
					Point wpt = new Point(points.get(imax).getLon(), points
							.get(imax).getLat());
					wpt.setCaption("MAX " + speedformat.format(maxz) + " m");
					wpts.add(wpt);
				}
			}
		}

	}

	private void collectWptsPath() {
		wptsPath = new ArrayList<CheckPoint>();

		if (points.size() > 1) {
			GPXPoint point = points.get(0);
			GPXPoint pointN = points.get(1);
			String caption = "Start";
			wptsPath.add(buildCheckPoint(point, pointN, caption, 0.0));
			point = points.get(points.size() - 2);
			pointN = points.get(points.size() - 1);
			caption = "End";
			wptsPath.add(buildCheckPoint(point, pointN, caption, 1.0));
		}

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

		for (CheckPoint checkPoint : wptsPath) {
			double z;
			try {
				z = SRTMHelper.getInstance().getElevation(checkPoint.getLon(),
						checkPoint.getLat());
			} catch (SRTMException e) {
				z = 0;
			}
			checkPoint.setZ(z);
		}
	}

	private CheckPoint buildCheckPoint(GPXPoint point, GPXPoint pointN,
			String caption, double coef) {
		CheckPoint checkPoint = new CheckPoint(point.getLon(), point.getLat());
		checkPoint.setCaption(caption);
		checkPoint.setpRef(point);
		checkPoint.setpRefNext(pointN);
		checkPoint.setCoefRef(coef);
		return checkPoint;
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

	public double getTotalElevation() {
		return totalElevation;
	}

	public double getMinElevation() {
		return minElevation;
	}

	public double getMaxElevation() {
		return maxElevation;
	}

}
