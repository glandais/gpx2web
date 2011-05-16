package org.magic.JnxPrepare;

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

	private List<GPXPoint> wpts = new ArrayList<GPXPoint>();

	private double minLon = 200;

	private double maxLon = -200;

	private double minLat = 200;

	private double maxLat = -200;

	private double extraBuffer;

	public GPXProcessor(Document gpxDocument, double extraBuffer) {
		super();
		this.gpxDocument = gpxDocument;
		this.extraBuffer = extraBuffer;
	}

	public Document getGpxDocument() {
		return gpxDocument;
	}

	public List<GPXPath> getPaths() {
		return paths;
	}

	public List<GPXPoint> getWpts() {
		return wpts;
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
		} else if (tagName.equals("wpt")) {
			double lon = Double.parseDouble(element.getAttribute("lon"));
			double lat = Double.parseDouble(element.getAttribute("lat"));
			GPXPoint p = new GPXPoint(lon, lat);
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

		minLon = Math.min(minLon, lon);
		maxLon = Math.max(maxLon, lon);
		minLat = Math.min(minLat, lat);
		maxLat = Math.max(maxLat, lat);

		currentPath.processPoint(lon, lat);
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

	public float[][][] getPathsAsArray() {
		float[][][] pathsArray = new float[paths.size()][][];
		for (int i = 0; i < pathsArray.length; i++) {
			List<GPXPoint> points = paths.get(i).getPoints();
			pathsArray[i] = toArray(points);
		}
		return pathsArray;
	}

	private float[][] toArray(List<GPXPoint> points) {
		float[][] result = new float[points.size()][2];
		for (int i = 0; i < result.length; i++) {
			GPXPoint point = points.get(i);
			result[i][0] = (float) point.getLat();
			result[i][1] = (float) point.getLon();
		}
		return null;
	}

	public float[][] getWptsAsArray() {
		return toArray(wpts);
	}

	public double getMinLon() {
		return minLon;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public boolean includes(double[] coords) {
		GPXPoint p1 = new GPXPoint(coords[0], coords[1]);
		GPXPoint p2 = new GPXPoint(coords[0], coords[3]);
		GPXPoint p3 = new GPXPoint(coords[2], coords[1]);
		GPXPoint p4 = new GPXPoint(coords[2], coords[3]);
		// one of the coords must be inside a track
		for (GPXPath path : paths) {
			if (path.includes(p1)) {
				return true;
			}
			if (path.includes(p2)) {
				return true;
			}
			if (path.includes(p3)) {
				return true;
			}
			if (path.includes(p4)) {
				return true;
			}
		}

		for (GPXPath path : paths) {
			if (path.inBuffer(p1, extraBuffer)) {
				return true;
			}
			if (path.inBuffer(p2, extraBuffer)) {
				return true;
			}
			if (path.inBuffer(p3, extraBuffer)) {
				return true;
			}
			if (path.inBuffer(p4, extraBuffer)) {
				return true;
			}
		}

		return false;
	}

}
