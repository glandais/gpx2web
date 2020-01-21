package io.github.glandais.io;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
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
import org.springframework.stereotype.Service;

import io.github.glandais.gpx.GPXPath;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GPXCharter {

	private static final DecimalFormat speedformat = new DecimalFormat("#0.0");

	public void createChartTime(GPXPath gpxPath, File file) {
		log.info("start createChartTime");
		DateAxis dateAxis = new DateAxis("");

		DateTickUnit unit = null;
		unit = new DateTickUnit(DateTickUnitType.MINUTE, 30);

		DateFormat chartFormatter = new SimpleDateFormat("HH:mm");
		dateAxis.setDateFormatOverride(chartFormatter);

		dateAxis.setTickUnit(unit);

		NumberAxis valueAxis = new NumberAxis("");

		XYSeries dataSeries = new XYSeries("");
		long[] time = gpxPath.getTime();
		for (int i = 0; i < time.length; i++) {
			dataSeries.add(time[i], gpxPath.getZs()[i]);
		}
		XYSeriesCollection xyDataset = new XYSeriesCollection(dataSeries);

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		XYPlot plot = new XYPlot(xyDataset, dateAxis, valueAxis, null);
		plot.setRenderer(renderer);

		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		try {
			ChartUtils.saveChartAsPNG(file, chart, 1920, 1080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("end createChartTime");

	}

	public void createChartWeb(GPXPath gpxPath, File file) {
		log.info("start createChartWeb");
		JFreeChart chart = ChartFactory.createXYAreaChart("", "", "", getDataSet(gpxPath), PlotOrientation.VERTICAL,
				false, false, false);
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

		try {
			ChartUtils.saveChartAsPNG(file, chart, 640, 240);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("end createChartWeb");
	}

	public void createChartSmall(GPXPath gpxPath, File file) {
		log.info("start createChartSmall");
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z", getDataSet(gpxPath), PlotOrientation.VERTICAL,
				false, false, false);

		chart.getXYPlot().getDomainAxis().setVisible(false);
		chart.getXYPlot().getRangeAxis().setVisible(false);
		chart.setBackgroundPaint(Color.white);
		chart.getXYPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.blue);
		chart.getXYPlot().setRangeGridlinePaint(Color.blue);

		try {
			ChartUtils.saveChartAsPNG(file, chart, 512, 64);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("end createChartSmall");
	}

	public void createChartBig(GPXPath gpxPath, File file) {
		log.info("start createChartBig");
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z", getDataSet(gpxPath), PlotOrientation.VERTICAL,
				false, false, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYItemRenderer rendu = new XYLineAndShapeRenderer();
		plot.setRenderer(1, rendu);
		try {
			ChartUtils.saveChartAsPNG(file, chart, 1920, 1080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("end createChartBig");
	}

	private DefaultXYDataset getDataSet(GPXPath gpxPath) {
		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("", getSerie(gpxPath));
		return dataset;
	}

	public double[][] getSerie(GPXPath gpxPath) {
		return new double[][] { gpxPath.getDists(), gpxPath.getZs() };
	}

}
