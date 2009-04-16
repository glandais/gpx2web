package org.glandais.gpx.elevation.fixer;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GPXProcessor {

	private Document gpxDocument;

	private List<GPXPath> paths = new ArrayList<GPXPath>();
	private GPXPath currentPath;

	//	private Point previousPoint = null;

	public GPXProcessor(Document gpxDocument) {
		super();
		this.gpxDocument = gpxDocument;
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
		if (tagName.equals("trk") || tagName.equals("rte")) {
			Element nameElement = findElement(element, "name");
			String name = "path-" + paths.size();
			if (nameElement != null) {
				name = nameElement.getTextContent();
			}
			currentPath = new GPXPath(name);
			paths.add(currentPath);
		}

		if (tagName.equals("trkpt")) {
			processPoint(document, element);
		} else if (tagName.equals("rtept")) {
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

		Element ele = findElementEle(document, element);
		currentPath.processPoint(lon, lat, ele);
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

}
