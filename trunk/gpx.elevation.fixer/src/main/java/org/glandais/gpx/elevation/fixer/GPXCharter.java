package org.glandais.gpx.elevation.fixer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.glandais.srtm.loader.SRTMImageProducer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
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

public class GPXCharter {

	private static final DecimalFormat speedformat = new DecimalFormat("#0.0");

	public static void createChartAndMap(GPXPath gpxPath, String string,
			int maxsize) throws Exception {
		System.out.println("chart " + gpxPath.getName());
		createChart(gpxPath, string);
		if (maxsize > 0) {
			System.out.println("map " + gpxPath.getName());
			createMap(gpxPath, string, maxsize);
		}
	}

	public static void createMap(GPXPath gpxPath, String outputFile, int maxsize)
			throws Exception {
		String imgPath = outputFile + gpxPath.getName() + ".map.png";
		SRTMImageProducer imageProducer = new SRTMImageProducer(
				gpxPath.getMinlon(), gpxPath.getMaxlon(), gpxPath.getMinlat(),
				gpxPath.getMaxlat(), maxsize, 0.2);
		imageProducer.fillWithZ();
		imageProducer.addPoints(gpxPath.getPoints(), gpxPath.getMinElevation(),
				gpxPath.getMaxElevation());
		imageProducer.saveImage(imgPath);
	}

	public static void createChart(GPXPath gpxPath, String outputFile) {
		String imgPath = outputFile + gpxPath.getName() + ".png";
		String imgPathTimeMin = outputFile + gpxPath.getName() + "-timeMin.png";
		String imgPathSmall = outputFile + gpxPath.getName() + "-small.png";
		String imgPathWeb = outputFile + gpxPath.getName() + "-web.png";

		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("", getSerie(gpxPath));

		createChartBig(gpxPath, imgPath, dataset);
		createChartTime(gpxPath, imgPathTimeMin, gpxPath.getTime());
		createChartSmall(gpxPath, imgPathSmall, dataset);
		createChartWeb(gpxPath, imgPathWeb, dataset);
	}

	private static void createChartTime(GPXPath gpxPath, String imgPathTime,
			long[] time) {
		DateAxis dateAxis = new DateAxis("");

		DateTickUnit unit = null;
		unit = new DateTickUnit(DateTickUnit.MINUTE, 30);

		DateFormat chartFormatter = new SimpleDateFormat("HH:mm");
		dateAxis.setDateFormatOverride(chartFormatter);

		dateAxis.setTickUnit(unit);

		NumberAxis valueAxis = new NumberAxis("");

		XYSeries dataSeries = new XYSeries("");
		for (int i = 0; i < time.length; i++) {
			dataSeries.add(time[i], gpxPath.getZs()[i]);
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

	private static void createChartWeb(GPXPath gpxPath, String imgPathWeb,
			DefaultXYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYAreaChart("", "", "", dataset,
				PlotOrientation.VERTICAL, false, false, false);
		chart.getXYPlot().getDomainAxis().setVisible(true);
		double distance = gpxPath.getDists()[gpxPath.getDists().length - 1];
		chart.getXYPlot().getDomainAxis().setRange(0, distance);
		chart.getXYPlot().getRangeAxis().setVisible(true);
		chart.getXYPlot().getRangeAxis()
				.setRange(gpxPath.getMinElevation(), gpxPath.getMaxElevation());
		chart.setBackgroundPaint(Color.white);
		chart.getXYPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.blue);
		chart.getXYPlot().setRangeGridlinePaint(Color.blue);

		chart.addSubtitle(new TextTitle(speedformat.format(distance)
				+ " km     +"
				+ speedformat.format(gpxPath.getTotalElevation() * 0.5) + " m"));

		try {
			ChartUtilities
					.saveChartAsPNG(new File(imgPathWeb), chart, 640, 480);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void createChartSmall(GPXPath gpxPath, String imgPathSmall,
			DefaultXYDataset dataset) {
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

	private static void createChartBig(GPXPath gpxPath, String imgPath,
			DefaultXYDataset dataset) {
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

	public static double[][] getSerie(GPXPath gpxPath) {
		return new double[][] { gpxPath.getDists(), gpxPath.getZs() };
	}

}
