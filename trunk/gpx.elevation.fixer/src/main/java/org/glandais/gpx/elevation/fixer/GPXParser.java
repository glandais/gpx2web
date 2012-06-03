package org.glandais.gpx.elevation.fixer;

import java.util.ArrayList;
import java.util.List;

import org.glandais.srtm.loader.Point;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GPXParser {

	private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat
			.dateTimeParser();

	public static List<GPXPath> parsePaths(Document gpxDocument, boolean fixZ)
			throws Exception {
		List<GPXPath> paths = new ArrayList<GPXPath>();
		processElement(gpxDocument, gpxDocument.getDocumentElement(), paths,
				fixZ);
		return paths;
	}

	private static void processElement(Document document, Element element,
			List<GPXPath> paths, boolean fixZ) throws Exception {
		String tagName = element.getTagName().toLowerCase();

		if (tagName.equals("trk") || tagName.equals("rte")) {
			Element nameElement = findElement(element, "name");
			String name = "path-" + paths.size();
			if (nameElement != null) {
				name = nameElement.getTextContent();
			}
			System.out.println("Parsing " + name);
			GPXPath currentPath = new GPXPath(name);
			paths.add(currentPath);
		}

		if (tagName.equals("trkpt")) {
			processPoint(document, element, paths, fixZ);
		} else if (tagName.equals("rtept")) {
			processPoint(document, element, paths, fixZ);
		} else if (tagName.equals("wpt")) {
			double lon = Double.parseDouble(element.getAttribute("lon"));
			double lat = Double.parseDouble(element.getAttribute("lat"));
			Point p = new Point(lon, lat);
			Element eleName = findElement(element, "name");
			if (eleName != null) {
				p.setCaption(eleName.getTextContent());
			}
			// wpts.add(p);
		} else {
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node instanceof Element) {
					processElement(document, (Element) node, paths, fixZ);
				}
			}
		}
	}

	private static void processPoint(Document document, Element element,
			List<GPXPath> paths, boolean fixZ) throws Exception {
		double lon = Double.parseDouble(element.getAttribute("lon"));
		double lat = Double.parseDouble(element.getAttribute("lat"));
		Element timeElement = findElement(element, "time");
		long date = 0;
		if (timeElement != null) {
			String dateString = timeElement.getTextContent();
			date = DATE_TIME_FORMAT.parseMillis(dateString);
		}
		paths.get(paths.size() - 1).processPoint(lon, lat, date, fixZ);
	}

	private static Element findElement(Element element, String string) {
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

}
