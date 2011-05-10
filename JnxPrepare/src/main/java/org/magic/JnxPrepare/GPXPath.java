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

	public boolean includes(GPXPoint p) {
		if (polygon == null) {
			computePolygon();
		}
		return polygon.contains(p.getLon(), p.getLat());
	}

	private void computePolygon() {
		polygon = new Polygon2D();
		for (GPXPoint point : points) {
			polygon.addPoint((float) point.getLon(), (float) point.getLat());
		}
	}

	public boolean inBuffer(GPXPoint p, double dmax) {
		GPXPoint s1;
		GPXPoint s2;
		double dmin = 1000;

		for (int i = 1; i < points.size(); i++) {
			s1 = points.get(i - 1);
			s2 = points.get(i);

			try {
				double d = s1.distanceTo(s2);
				double d1 = s1.distanceTo(p);
				double d2 = s2.distanceTo(p);

				// Al-Kashi
				// d2² = d² + d1² + 2*d*d1*cos a
				double cosa1 = (d2 * d2 - d * d - d1 * d1) / (2.0 * d * d1);
				double a1 = Math.acos(cosa1);

				double cosa2 = (d1 * d1 - d * d - d2 * d2) / (2.0 * d * d2);
				double a2 = Math.acos(cosa2);

				if (a1 < Math.PI / 1.9 && a2 < Math.PI / 1.9) {
					// sin a = d / d1
					double dist = d1 * Math.sin(a1);
					dmin = Math.min(dmin, dist);
					if (dist < dmax) {
						return true;
					}
				}
			} catch (Throwable e) {
			}

		}
		return false;
	}
}
