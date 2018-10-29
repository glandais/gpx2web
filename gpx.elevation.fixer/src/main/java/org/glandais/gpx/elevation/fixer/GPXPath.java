package org.glandais.gpx.elevation.fixer;

import java.util.ArrayList;
import java.util.List;

import org.glandais.gpx.srtm.Point;
import org.glandais.gpx.srtm.SRTMException;
import org.glandais.gpx.srtm.SRTMHelper;

public class GPXPath {

	private double minElevation = 20000;
	private double maxElevation = -10000;
	private Point previousPoint = null;
	private double totalElevation = 0;
	private double minlon = 180;
	private double maxlon = -180;
	private double minlat = 180;
	private double maxlat = -180;

	private List<Point> points = new ArrayList<Point>();
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

	public void processPoint(double lon, double lat, double ele, long date, boolean fixZ) throws SRTMException {
		Point p = new Point(lon, lat, ele, date);
		boolean doAdd = true;
		if (previousPoint == null) {
			doAdd = true;
		} else {
			// en km double
			double dist = previousPoint.distanceTo(p);
			if (Double.toString(dist).contains("NaN") || dist < 0.002) {
				doAdd = false;
			}
		}
		if (doAdd) {
			if (fixZ) {
				if (previousPoint == null) {
					p.setZ(SRTMHelper.getInstance().getElevation(p.getLon(), p.getLat()));
					points.add(p);
				} else {
					List<Point> subPoints = SRTMHelper.getInstance().getPointsBetween(previousPoint, p);
					for (int i = 1; i < subPoints.size(); i++) {
						Point point = subPoints.get(i);
						points.add(point);
					}
				}
				previousPoint = p;
			} else {
				points.add(p);
			}
		}
	}

	public void postProcess(GPXBikeTimeEval bikeTimeEval) throws SRTMException {
		System.out.println("Post process " + name);
		filterPoints();
		computeArrays();
		fixZ();
		computeArrays();

		bikeTimeEval.computeMaxSpeeds(points);

		double previousElevation = -9999;
		time = new long[points.size()];
		previousPoint = null;
		for (Point p : points) {
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
		}

		bikeTimeEval.computeTrack(points);

		for (int j = 0; j < points.size(); j++) {
			time[j] = points.get(j).getTime();
		}

	}

	private void fixZ() {
		double[] newZs = new double[zs.length];
		for (int j = 0; j < newZs.length; j++) {
			newZs[j] = computeNewValue(j, 1, 1, zs);
			Point p = points.get(j);
			p.setZ(newZs[j]);
			p.setDist(dists[j]);
		}
		zs = newZs;
	}

	private void computeArrays() {
		previousPoint = null;
		double d = 0;
		dists = new double[points.size()];
		zs = new double[points.size()];
		time = new long[points.size()];
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
			previousPoint = p;
			i++;
		}
	}

	private void filterPoints() {
		List<Point> newPoints = new ArrayList<Point>();
		Point lastPoint = null;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0 || i == points.size() - 1) {
				newPoints.add(p);
				lastPoint = p;
			} else {
				if (lastPoint.distanceTo(p) > 0.002) {
					newPoints.add(p);
					lastPoint = p;
				}
			}
		}
		points = newPoints;
	}

	private double computeNewValue(int i, double before, double after, double[] data) {
		// double dsample = 1;

		double ac = dists[i];

		int mini = i - 1;
		while (mini >= 0 && (ac - dists[mini]) <= before) {
			mini--;
		}
		mini++;

		int maxi = i + 1;
		while (maxi < data.length && (dists[maxi] - ac) <= after) {
			maxi++;
		}

		double totc = 0;
		double totz = 0;
		for (int j = mini; j < maxi; j++) {
			double c = 1 - (Math.abs(dists[j] - ac) / Math.max(after, before));
			totc = totc + c;
			totz = totz + data[j] * c;
		}

		if (totc == 0) {
			return data[i];
		} else {
			return totz / totc;
		}

	}

}
