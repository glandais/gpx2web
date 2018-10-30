package org.glandais.gpx.elevation.fixer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
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

	private GPXCharter() {
		super();
	}

	public static void createCharts(GPXPath gpxPath, String outputFolder) throws IOException {
		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("", getSerie(gpxPath));

		createChartBig(gpxPath, outputFolder, dataset);
		createChartTime(gpxPath, outputFolder);
		createChartSmall(gpxPath, outputFolder, dataset);
		createChartWeb(gpxPath, outputFolder, dataset);
	}

	private static void createChartTime(GPXPath gpxPath, String outputFolder) throws IOException {
		long[] time = gpxPath.getTime();
		String imgPathTime = outputFolder + gpxPath.getName() + "-time.png";
		DateAxis dateAxis = new DateAxis("");

		DateTickUnit unit = new DateTickUnit(DateTickUnitType.MINUTE, 30);

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

		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		ChartUtilities.saveChartAsPNG(new File(imgPathTime), chart, 1920, 1080);

	}

	private static void createChartWeb(GPXPath gpxPath, String outputFolder, DefaultXYDataset dataset)
			throws IOException {
		String imgPathWeb = outputFolder + gpxPath.getName() + "-web.png";
		JFreeChart chart = ChartFactory.createXYAreaChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false,
				false);
		chart.getXYPlot().getDomainAxis().setVisible(true);
		double distance = gpxPath.getDists()[gpxPath.getDists().length - 1];
		chart.getXYPlot().getDomainAxis().setRange(0, distance);
		chart.getXYPlot().getRangeAxis().setVisible(true);
		chart.getXYPlot().getRangeAxis().setRange(gpxPath.getMinElevation(), gpxPath.getMaxElevation());
		chart.setBackgroundPaint(Color.white);
		chart.getXYPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.blue);
		chart.getXYPlot().setRangeGridlinePaint(Color.blue);

		chart.addSubtitle(new TextTitle(
				speedformat.format(distance) + " km     +" + speedformat.format(gpxPath.getTotalElevation()) + " m"));

		ChartUtilities.saveChartAsPNG(new File(imgPathWeb), chart, 640, 480);
	}

	private static void createChartSmall(GPXPath gpxPath, String outputFolder, DefaultXYDataset dataset)
			throws IOException {
		String imgPathSmall = outputFolder + gpxPath.getName() + "-small.png";
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z", dataset, PlotOrientation.VERTICAL, false, false,
				false);

		chart.getXYPlot().getDomainAxis().setVisible(false);
		chart.getXYPlot().getRangeAxis().setVisible(false);
		chart.setBackgroundPaint(Color.white);
		chart.getXYPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.blue);
		chart.getXYPlot().setRangeGridlinePaint(Color.blue);

		ChartUtilities.saveChartAsPNG(new File(imgPathSmall), chart, 512, 64);
	}

	private static void createChartBig(GPXPath gpxPath, String outputFolder, DefaultXYDataset dataset)
			throws IOException {
		String imgPath = outputFolder + gpxPath.getName() + ".png";
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z", dataset, PlotOrientation.VERTICAL, false, false,
				false);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYItemRenderer rendu = new XYLineAndShapeRenderer();
		plot.setRenderer(1, rendu);
		ChartUtilities.saveChartAsPNG(new File(imgPath), chart, 1920, 1080);
	}

	public static double[][] getSerie(GPXPath gpxPath) {
		return new double[][] { gpxPath.getDists(), gpxPath.getZs() };
	}

}
