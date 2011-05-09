package org.magic.JnxPrepare;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

public class GPXPath {

	private double minlon = 180;
	private double maxlon = -180;
	private double minlat = 180;
	private double maxlat = -180;

	private Polygon2D polygon = null;

	private List<GPXPoint> points = new ArrayList<GPXPoint>();
	private String name;

	public GPXPath(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void processPoint(double lon, double lat) {
		if (lon < minlon) {
			minlon = lon;
		}
		if (lon > maxlon) {
			maxlon = lon;
		}
		if (lat < minlat) {
			minlat = lat;
		}
		if (lat > maxlat) {
			maxlat = lat;
		}

		GPXPoint p = new GPXPoint(lon, lat);

		points.add(p);
	}

	public List<GPXPoint> getPoints() {
		return points;
	}

	public boolean includes(double lon, double lat) {
		if (polygon == null) {
			computePolygon();
		}
		return polygon.contains(lon, lat);
	}

	private void computePolygon() {
		polygon = new Polygon2D();
		for (GPXPoint point : points) {
			polygon.addPoint((float) point.getLon(), (float) point.getLat());
		}
	}
}
