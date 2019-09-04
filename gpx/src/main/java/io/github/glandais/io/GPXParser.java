package io.github.glandais.io;

import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GPXParser {

	public List<GPXPath> parsePaths(InputStream is) throws Exception {
		return parsePaths(is, (db, f) -> {
			try {
				return db.parse(f);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public List<GPXPath> parsePaths(File file) throws Exception {
		return parsePaths(file, (db, f) -> {
			try {
				return db.parse(f);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private <T> List<GPXPath> parsePaths(T file, BiFunction<DocumentBuilder, T, Document> parser) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxDocument = parser.apply(db, file);
		List<GPXPath> paths = new ArrayList<GPXPath>();
		processElement(gpxDocument, gpxDocument.getDocumentElement(), paths);
		for (GPXPath gpxPath : paths) {
			gpxPath.computeArrays();
		}
		return paths;
	}

	private void processElement(Document document, Element element, List<GPXPath> paths) throws Exception {
		String tagName = element.getTagName().toLowerCase();

		if (tagName.equals("trk") || tagName.equals("rte")) {
			Element nameElement = findElement(element, "name");
			String name = "path-" + paths.size();
			if (nameElement != null) {
				name = nameElement.getTextContent();
			}
			log.info("Parsing {}", name);
			GPXPath currentPath = new GPXPath(name);
			paths.add(currentPath);
		}

		if (tagName.equals("trkpt")) {
			processPoint(document, element, paths);
		} else if (tagName.equals("rtept")) {
			processPoint(document, element, paths);
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
					processElement(document, (Element) node, paths);
				}
			}
		}
	}

	private void processPoint(Document document, Element element, List<GPXPath> paths) throws Exception {
		double lon = Double.parseDouble(element.getAttribute("lon"));
		double lat = Double.parseDouble(element.getAttribute("lat"));
		Element timeElement = findElement(element, "time");
		Element eleElement = findElement(element, "ele");
		long date = 0;
		double ele = 0;
		if (timeElement != null) {
			String dateString = timeElement.getTextContent();
			TemporalAccessor parse = DateTimeFormatter.ISO_DATE_TIME.parse(dateString);
			date = Instant.from(parse).toEpochMilli();
		}
		if (eleElement != null) {
			ele = Double.valueOf(eleElement.getTextContent());
		}
		Point p = new Point(lon, lat, ele, date);
		paths.get(paths.size() - 1).addPoint(p);
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

}
