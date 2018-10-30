package org.glandais.gpx.elevation.fixer;

import java.util.ArrayList;
import java.util.List;

import org.glandais.gpx.srtm.Point;

public class GPXPath {

	private double minElevation = 20000;
	private double maxElevation = -10000;
	private double totalElevation = 0;
	private double minlon = 180;
	private double maxlon = -180;
	private double minlat = 180;
	private double maxlat = -180;

	private List<Point> points = new ArrayList<>();
	private String name;

	private double[] dists;
	private double[] zs;
	private long[] time;

	public GPXPath(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getMinlon() {
		return minlon;
	}

	public double getMaxlon() {
		return maxlon;
	}

	public double getMinlat() {
		return minlat;
	}

	public double getMaxlat() {
		return maxlat;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
		updateArrays();
	}

	public double[] getDists() {
		return dists;
	}

	public double[] getZs() {
		return zs;
	}

	public long[] getTime() {
		return time;
	}

	public double getTotalElevation() {
		return totalElevation;
	}

	public double getMinElevation() {
		return minElevation;
	}

	public double getMaxElevation() {
		return maxElevation;
	}

	public void addPoint(double lon, double lat, double ele, long date) {
		points.add(new Point(lon, lat, ele, date));
	}

	public void updateArrays() {
		dists = new double[points.size()];
		zs = new double[points.size()];
		time = new long[points.size()];
		time = new long[points.size()];
		minElevation = 20000;
		maxElevation = -10000;
		totalElevation = 0;
		minlon = 180;
		maxlon = -180;
		minlat = 180;
		maxlat = -180;

		double d = 0;
		Point previousPoint = null;
		double previousElevation = -9999;

		int i = 0;
		for (Point p : points) {
			zs[i] = p.getZ();
			if (previousPoint != null) {
				double dist = previousPoint.distanceTo(p);
				d += dist;
			}
			dists[i] = d;
			p.setDist(dists[i]);
			zs[i] = p.getZ();
			time[i] = p.getTime();

			if (p.getLon() < minlon) {
				minlon = p.getLon();
			}
			if (p.getLon() > maxlon) {
				maxlon = p.getLon();
			}
			if (p.getLat() < minlat) {
				minlat = p.getLat();
			}
			if (p.getLat() > maxlat) {
				maxlat = p.getLat();
			}

			double elevation = p.getZ();
			if (elevation < minElevation) {
				minElevation = elevation;
			}
			if (elevation > maxElevation) {
				maxElevation = elevation;
			}
			if (previousElevation != -9999) {
				double dz = elevation - previousElevation;
				if (dz > 0) {
					totalElevation += dz;
				}
			}
			previousElevation = elevation;
			previousPoint = p;
			i++;
		}
	}

}
