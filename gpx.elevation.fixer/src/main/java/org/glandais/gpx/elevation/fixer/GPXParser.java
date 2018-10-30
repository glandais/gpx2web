package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GPXParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(GPXParser.class);

	private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeParser();

	protected GPXParser() {
		super();
	}

	public static List<GPXPath> parsePaths(File inputFile)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxDocument = db.parse(inputFile);

		List<GPXPath> paths = new ArrayList<>();
		processElement(gpxDocument, gpxDocument.getDocumentElement(), paths);
		for (GPXPath gpxPath : paths) {
			GPXPostProcessor.filterPoints(gpxPath);
		}
		return paths;
	}

	protected static void processElement(Document document, Element element, List<GPXPath> paths) {
		String tagName = element.getTagName().toLowerCase();

		if (tagName.equals("trk") || tagName.equals("rte")) {
			Element nameElement = findElement(element, "name");
			String name = "path-" + paths.size();
			if (nameElement != null) {
				name = nameElement.getTextContent();
			}
			LOGGER.info("Parsing {}", name);
			GPXPath currentPath = new GPXPath(name);
			paths.add(currentPath);
		}

		if (tagName.equals("trkpt")) {
			processPoint(element, paths);
		} else if (tagName.equals("rtept")) {
			processPoint(element, paths);
		} else {
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node instanceof Element) {
					processElement(document, (Element) node, paths);
				}
			}
		}
	}

	protected static void processPoint(Element element, List<GPXPath> paths) {
		double lon = Double.parseDouble(element.getAttribute("lon"));
		double lat = Double.parseDouble(element.getAttribute("lat"));
		Element eleElement = findElement(element, "ele");
		double ele = 0;
		if (eleElement != null) {
			String eleString = eleElement.getTextContent();
			ele = Double.parseDouble(eleString);
		}
		Element timeElement = findElement(element, "time");
		long date = 0;
		if (timeElement != null) {
			String dateString = timeElement.getTextContent();
			date = DATE_TIME_FORMAT.parseMillis(dateString);
		}
		paths.get(paths.size() - 1).addPoint(lon, lat, ele, date);
	}

	protected static Element findElement(Element element, String string) {
		Element ele = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element potEle = (Element) node;
				if (potEle.getTagName().equalsIgnoreCase(string)) {
					ele = potEle;
				}
			}
		}
		return ele;
	}

}
