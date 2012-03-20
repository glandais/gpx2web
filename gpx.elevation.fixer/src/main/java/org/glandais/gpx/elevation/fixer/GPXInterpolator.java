package org.glandais.gpx.elevation.fixer;

import java.util.ArrayList;
import java.util.List;

import org.glandais.srtm.loader.Point;

public class GPXInterpolator {

	private double latRef = 0;
	private double lonRef = 0;
	private double cRef = 0;
	private static final double CIRC = 40000000.0;

	/**
	 * Returns a path, between every two control points numPoints are generated
	 * and the control points themselves are added too. The first and the last
	 * controlpoint are omitted. if there's less than 4 controlpoints an empty
	 * path is returned.
	 * 
	 * @param numPoints
	 *            number of points returned for a segment
	 * @return the path
	 */
	private List<Point> getInterpolatedPathImpl(List<Point> points) {
		ArrayList<Point> newPoints = new ArrayList<Point>();

		if (points.size() < 4)
			return points;

		Point T1 = new Point();
		Point T2 = new Point();

		for (int i = 1; i <= points.size() - 3; i++) {
			Point pm1 = points.get(i - 1);
			Point p = points.get(i);
			Point pp1 = points.get(i + 1);
			Point pp2 = points.get(i + 2);
			double dist = p.getDist();
			int numPoints = (int) (dist / 0.005);

			newPoints.add(p);
			float increment = 1.0f / (numPoints + 1);
			float t = increment;

			T1.set(pp1).sub(pm1).mul(0.5f);
			T2.set(pp2).sub(p).mul(0.5f);

			for (int j = 0; j < numPoints; j++) {
				float h1 = 2 * t * t * t - 3 * t * t + 1; // calculate basis
				// function 1
				float h2 = -2 * t * t * t + 3 * t * t; // calculate basis
				// function 2
				float h3 = t * t * t - 2 * t * t + t; // calculate basis
				// function 3
				float h4 = t * t * t - t * t; // calculate basis function 4

				Point point = new Point(p).mul(h1);
				point.add(pp1.tmp().mul(h2));
				point.add(T1.tmp().mul(h3));
				point.add(T2.tmp().mul(h4));
				newPoints.add(point);
				t += increment;
			}
		}

		if (points.size() >= 4)
			newPoints.add(points.get(points.size() - 2));

		return newPoints;
	}

	public List<Point> getInterpolatedPath(List<Point> points) {
		List<Point> geoPoints = projectPoints(points);

		Point p1 = geoPoints.get(0).tmp();
		for (Point point : geoPoints) {
			point.sub(p1);
		}

		geoPoints = getInterpolatedPathImpl(geoPoints);

		for (Point point : geoPoints) {
			point.add(p1);
		}

		List<Point> result = inverseProjectPoints(geoPoints);

		return result;
	}

	private List<Point> inverseProjectPoints(List<Point> geoPoints) {
		List<Point> result = new ArrayList<Point>(geoPoints.size());
		for (Point point : geoPoints) {
			Point inverse = inverseTransform(point);
			inverse.setZ(point.getZ());
			result.add(inverse);
		}
		return result;
	}

	private List<Point> projectPoints(List<Point> points) {
		latRef = points.get(0).getLat();
		cRef = CIRC * Math.cos(latRef);
		lonRef = points.get(0).getLon();
		List<Point> result = new ArrayList<Point>(points.size());
		Point prevPoint = null;
		for (Point point : points) {
			Point projected = transform(point);
			projected.setZ(point.getZ());
			result.add(projected);
			if (prevPoint == null) {
				projected.setDist(0);
			} else {
				projected.setDist(point.distanceTo(prevPoint));
			}
			prevPoint = point;
		}

		return result;
	}

	private Point inverseTransform(Point point) {
		double lon = (point.getLon() * 2 * 3.14) / CIRC;
		double lat = (point.getLat() * 2 * 3.14) / cRef;
		return new Point(lon + lonRef, lat + latRef);
	}

	private Point transform(Point point) {
		double lon = point.getLon() - lonRef;
		double lat = point.getLat() - latRef;
		double x = lon * CIRC / (2 * 3.14);
		double y = lat * cRef / (2 * 3.14);
		return new Point(x, y);
	}
}
