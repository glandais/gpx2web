package org.glandais.gpx.elevation.fixer;

import org.glandais.srtm.loader.SRTMHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GPXProcessor {

	private Document gpxDocument;

	private boolean fixElevation = true;

	private double minElevation = 20000;
	private double maxElevation = -10000;

	public GPXProcessor(Document gpxDocument) {
		super();
		this.gpxDocument = gpxDocument;
	}

	public void showStats() {
		System.out.println("min elevation : " + minElevation);
		System.out.println("max elevation : " + maxElevation);
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
}
