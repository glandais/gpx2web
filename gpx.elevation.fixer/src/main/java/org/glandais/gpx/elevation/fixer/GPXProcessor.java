package org.glandais.gpx.elevation.fixer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.glandais.srtm.loader.Point;
import org.glandais.srtm.loader.SRTMHelper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GPXProcessor {

	private Document gpxDocument;

	private boolean fixElevation = true;

	private double minElevation = 20000;
	private double maxElevation = -10000;
	private double totalElevation = 0;
	private double previousElevation = -9999;

	private List<Point> points = new ArrayList<Point>();

	//	private Point previousPoint = null;

	public GPXProcessor(Document gpxDocument) {
		super();
		this.gpxDocument = gpxDocument;
	}

	public void showStats() {
		System.out.println("min elevation : " + minElevation);
		System.out.println("max elevation : " + maxElevation);
		System.out.println("total elevation : " + totalElevation);
	}

	public Document getGpxDocument() {
		return gpxDocument;
	}

	public void parse() throws Exception {
		processElement(gpxDocument, gpxDocument.getDocumentElement());
	}

	private void processElement(Document document, Element element)
			throws Exception {
		if (element.getTagName().toLowerCase().equals("trkpt")) {
			processPoint(document, element);
		} else {
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node instanceof Element) {
					processElement(document, (Element) node);
				}
			}
		}
	}

	private void processPoint(Document document, Element element)
			throws Exception {
		double lon = Double.parseDouble(element.getAttribute("lon"));
		double lat = Double.parseDouble(element.getAttribute("lat"));

		Element ele = findElement(document, element, "ele");
		String textElevation = ele.getTextContent();
		double elevation = Double.parseDouble(textElevation);

		if (fixElevation) {
			elevation = SRTMHelper.getInstance().getElevation(lon, lat);
			ele.setTextContent(Double.toString(elevation));
		}

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

	private Element findElement(Document document, Element element,
			String string) {
		Element ele = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element potEle = (Element) node;
				if (potEle.getTagName().toLowerCase().equals("ele")) {
					ele = potEle;
				}
			}
		}
		if (ele == null) {
			ele = document.createElement("ele");
			ele.setTextContent("0");
			element.appendChild(ele);
		}
		return ele;
	}

	public void createChart(String outputFile) {
		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("", getSerie());
		JFreeChart chart = ChartFactory.createXYLineChart("", "d", "z",
				dataset, PlotOrientation.VERTICAL, false, false, false);

		XYPlot plot = (XYPlot) chart.getPlot();

		XYItemRenderer rendu = new XYLineAndShapeRenderer();
		plot.setRenderer(1, rendu);

		try {
			ChartUtilities.saveChartAsPNG(new File(outputFile), chart, 1300,
					600);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		return new double[][] { dists, zs };
	}
}
