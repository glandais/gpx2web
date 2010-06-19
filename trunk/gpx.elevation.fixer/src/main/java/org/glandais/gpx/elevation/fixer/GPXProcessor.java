package org.glandais.gpx.elevation.fixer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.glandais.srtm.loader.Point;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GPXProcessor {

	private Document gpxDocument;
	private Element gpxElement;
	private Element refElement;

	private List<GPXPath> paths = new ArrayList<GPXPath>();
	private GPXPath currentPath;

	private List<Point> wpts = new ArrayList<Point>();

	DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	DateTimeFormatter fmt2 = ISODateTimeFormat.hourMinute();
	DateTimeFormatter fmt3 = ISODateTimeFormat.hourMinuteSecond();

	private GPXBikeTimeEval bikeTimeEval;

	// private Point previousPoint = null;

	public GPXProcessor(Document gpxDocument, GPXBikeTimeEval bikeTimeEval) {
		super();
		this.gpxDocument = gpxDocument;
		this.bikeTimeEval = bikeTimeEval;
	}

	public Document getGpxDocument() {
		return gpxDocument;
	}

	public void parse() throws Exception {
		processElement(gpxDocument, gpxDocument.getDocumentElement());
	}

	private void processElement(Document document, Element element)
			throws Exception {
		String tagName = element.getTagName().toLowerCase();

		if (tagName.equals("gpx")) {
			gpxElement = element;
			refElement = null;
		}

		if (tagName.equals("trk") || tagName.equals("rte")) {
			if (refElement == null) {
				refElement = element;
			}

			Element nameElement = findElement(element, "name");
			String name = "path-" + paths.size();
			if (nameElement != null) {
				name = nameElement.getTextContent();
			}
			currentPath = new GPXPath(name, bikeTimeEval, wpts);
			paths.add(currentPath);
		}

		if (tagName.equals("trkpt")) {
			processPoint(document, element);
		} else if (tagName.equals("rtept")) {
			processPoint(document, element);
		} else if (tagName.equals("wpt")) {
			double lon = Double.parseDouble(element.getAttribute("lon"));
			double lat = Double.parseDouble(element.getAttribute("lat"));
			Point p = new Point(lon, lat);
			Element eleName = findElement(element, "name");
			if (eleName != null) {
				p.setCaption(eleName.getTextContent());
			}
			wpts.add(p);
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

		Element eleEle = findElementEle(document, element);
		Element eleTime = findElementTime(document, element);
		currentPath.processPoint(lon, lat, eleEle, eleTime);
	}

	private Element findElementEle(Document document, Element element) {
		Element ele = findElement(element, "ele");
		if (ele == null) {
			ele = document.createElement("ele");
			ele.setTextContent("0");
			element.appendChild(ele);
		}
		return ele;
	}

	private Element findElementTime(Document document, Element element) {
		Element ele = findElement(element, "time");
		if (ele == null) {
			ele = document.createElement("time");
			String time = fmt.print(new Date().getTime());
			ele.setTextContent(time);
			element.appendChild(ele);
		}
		return ele;
	}

	private Element findElement(Element element, String string) {
		Element ele = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element potEle = (Element) node;
				if (potEle.getTagName().toLowerCase().equals(string)) {
					ele = potEle;
				}
			}
		}
		return ele;
	}

	public void showStats() {
		for (GPXPath path : paths) {
			path.showStats();
		}
	}

	public void createCharts(String string, int maxsize) throws Exception {
		for (GPXPath path : paths) {
			System.out.println("chart " + path.getName());
			path.createChart(string);
			System.out.println("map " + path.getName());
			path.createMap(string, maxsize);
		}

	}

	public void postProcess(StringBuilder timeSheet) {
		NumberFormat nf = NumberFormat.getNumberInstance();

		List<Point> pointsTime = new ArrayList<Point>();
		for (GPXPath path : paths) {
			timeSheet.append(path.getName());
			timeSheet.append("\r\n");

			System.out.println("postProcess " + path.getName());
			pointsTime.addAll(path.postProcess());

			List<CheckPoint> wptsPath = path.getWptsPath();
			Collections.sort(wptsPath);
			for (CheckPoint checkPoint : wptsPath) {
				timeSheet.append(checkPoint.getCaption());
				timeSheet.append(";");
				timeSheet.append(nf.format(checkPoint.getDist()));
				timeSheet.append(";");
				timeSheet.append(fmt3.print(checkPoint.getTmin()));
				timeSheet.append(";");
				timeSheet.append(fmt3.print(checkPoint.getTmax()));
				timeSheet.append("\r\n");
			}

		}
		for (Point point : pointsTime) {
			Element wpt = gpxDocument.createElement("wpt");
			wpt.setAttribute("lat", Double.toString(point.getLat()));
			wpt.setAttribute("lon", Double.toString(point.getLon()));

			Element name = gpxDocument.createElement("name");
			name.setTextContent(point.getCaption());
			wpt.appendChild(name);

			gpxElement.insertBefore(wpt, refElement);
		}
	}
}
