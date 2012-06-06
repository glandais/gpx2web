package org.glandais.gpx.elevation.fixer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.glandais.gpx.braquet.Braquet;
import org.glandais.srtm.loader.Point;
import org.glandais.srtm.loader.SRTMException;
import org.glandais.srtm.loader.SRTMHelper;

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

	public void processPoint(double lon, double lat, long date, boolean fixZ) throws SRTMException {
		Point p = new Point(lon, lat, 0, date);
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

	public void fixZ() {
		double[] newZs = new double[zs.length];
		for (int j = 0; j < newZs.length; j++) {
			newZs[j] = computeNewValue(j, 1, 1, zs);
			Point p = points.get(j);
			p.setZ(newZs[j]);
			p.setDist(dists[j]);
		}
		zs = newZs;
	}

	public void fixMaxSpeed(GPXBikeTimeEval bikeTimeEval) {
		for (int i = points.size() - 1; i > 0; i--) {
			double maxSpeed = points.get(i).getMaxSpeed();
			double maxSpeedPrevious = points.get(i - 1).getMaxSpeed();
			// we have to brake!
			if (maxSpeed < maxSpeedPrevious) {
				double dist = points.get(i).getDist() - points.get(i - 1).getDist();
				double newMaxSpeedPrevious = bikeTimeEval.getMaxSpeed(maxSpeed, dist);
				points.get(i - 1).setMaxSpeed(newMaxSpeedPrevious);
			}
		}
		/*
		double[] maxSpeeds = new double[points.size()];
		for (int i = 0; i < maxSpeeds.length; i++) {
			maxSpeeds[i] = points.get(i).getMaxSpeed();
		}

		for (int j = 0; j < maxSpeeds.length; j++) {
			double newMaxSpeed = computeNewValue(j, 0.1, 0.1, maxSpeeds);
			Point p = points.get(j);
			p.setMaxSpeed(newMaxSpeed);
		}
		*/
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

	public void postProcess(GPXBikeTimeEval bikeTimeEval) throws SRTMException {
		System.out.println("Post process " + name);
		filterPoints();
		computeArrays();
		fixZ();
		// addInterPoints(0.01);
		computeArrays();
		computeMaxSpeeds(bikeTimeEval);
		fixMaxSpeed(bikeTimeEval);
		// points = new GPXInterpolator().getInterpolatedPath(points);

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

	private void computeMaxSpeeds(GPXBikeTimeEval bikeTimeEval) {
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0 || i == points.size() - 1) {
				p.setMaxSpeed(bikeTimeEval.getMaxSpeed());
			} else {
				Point pm1 = points.get(i - 1);
				Point pp1 = points.get(i + 1);
				p.setMaxSpeed(bikeTimeEval.getMaxSpeed(pm1, p, pp1));
			}
		}
	}

	private void addInterPoints(double d) {
		List<Point> newPoints = new ArrayList<Point>();
		Point lastPoint = null;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0 || i == points.size() - 1) {
				newPoints.add(p);
				lastPoint = p;
			} else {
				double dist = lastPoint.distanceTo(p);
				if (dist > d) {
					long n = 1 + Math.round(Math.floor((dist * 1.0) / (d * 1.0)));
					for (int j = 1; j < n; j++) {
						float c = (j * 1.0f) / (n * 1.0f);
						Point np = new Point(p).mul(c).add(new Point(lastPoint).mul(1 - c));
						newPoints.add(np);
					}
				}
				newPoints.add(p);
				lastPoint = p;
			}
		}
		points = newPoints;
	}

	private void filterPoints() {
		points = filterPoints(points);
	}

	private List<Point> filterPoints(List<Point> points) {
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
		return newPoints;
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

	private double computeNewValueTime(int i, long timeBefore, long timeAfter, double[] data) {
		// double dsample = 1;

		long ac = time[i];

		int mini = i - 1;
		while (mini >= 0 && (ac - time[mini]) <= timeBefore) {
			mini--;
		}
		mini++;

		int maxi = i + 1;
		while (maxi < data.length && (time[maxi] - ac) <= timeAfter) {
			maxi++;
		}

		double totc = 0;
		double totz = 0;
		for (int j = mini; j < maxi; j++) {
			double c = 1 - (Math.abs(time[j] - ac) / Math.max(timeBefore, timeAfter));
			totc = totc + c;
			totz = totz + data[j] * c;
		}

		if (totc == 0) {
			return data[i];
		} else {
			return totz / totc;
		}

	}

	public void tryBraquets(List<Braquet> braquets, boolean verbose, BufferedWriter writer) throws IOException {
		filterPoints();
		computeArrays();

		Point lastPoint = null;
		double[] speed = new double[points.size() - 1];
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0) {
				lastPoint = p;
			} else {
				if (i > 1) {
					speed[i - 1] = speed[i - 2];
				} else {
					speed[i - 1] = 1.0;
				}
				double dist = lastPoint.distanceTo(p);
				long dt = p.getTime() - lastPoint.getTime();
				if (dt > 0 && dist > 0.002) {
					double invSpeed = (dt / 3600000.0) / dist;
					if (invSpeed > 0.0) {
						speed[i - 1] = invSpeed;
					}
				}
			}
			lastPoint = p;
		}
		lastPoint = points.get(0);
		for (int i = 0; i < points.size() - 1; i++) {
			Point p = points.get(i + 1);
			long dt = p.getTime() - lastPoint.getTime();
			double invSpeed = computeNewValueTime(i, 15000, 15000, speed);
			double curSpeed = 1 / invSpeed;
			if (verbose) {
				writer.append(curSpeed + " ");
			}
			double dist = lastPoint.distanceTo(p);
			for (Braquet braquet : braquets) {
				boolean changed = braquet.applySpeed(curSpeed, dt, dist, verbose, writer);
				if (changed && verbose) {
					// writer.newLine();
					// writer.append("NEW " + braquet.curPlateau + " x " +
					// braquet.curPignon);
				}
			}
			lastPoint = p;
		}

	}

	public List<GPXPath> splitWithStops() {
		List<GPXPath> result = new ArrayList<GPXPath>();
		int ipath = 1;

		List<Point> curPoints = new ArrayList<Point>();
		Point lastPoint = null;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (lastPoint != null) {
				double dist = lastPoint.distanceTo(p);
				long time = p.getTime() - lastPoint.getTime();
				// a stop is not moving for a long time (2m in 10s)
				if (dist < 0.01 && time >= 5000) {
					ipath = addPath(result, ipath, curPoints);
					curPoints = new ArrayList<Point>();
				} else {
					if (curPoints.size() == 0) {
						curPoints.add(lastPoint);
					}
					curPoints.add(p);
				}
			}
			lastPoint = p;
		}
		addPath(result, ipath, curPoints);
		// for (GPXPath path : result) {
		// System.out.println(path.points.get(0).distanceTo(path.points.get(path.points.size()
		// - 1)));
		// List<Point> pathPoints = path.points;
		// for (int i = 1; i < pathPoints.size(); i++) {
		// Point p1 = pathPoints.get(i - 1);
		// Point p2 = pathPoints.get(i);
		// long dt = p2.getTime() - p1.getTime();
		// double speed = p1.distanceTo(p2) / (dt / 3600000.0);
		// System.out.println("- " + p1.distanceTo(p2) + " (" + dt +
		// ") " + speed);
		// }
		// }
		return result;
	}

	private int addPath(List<GPXPath> result, int ipath, List<Point> points) {
		if (points.size() > 0) {
			List<Point> realPoints = filterPoints(points);
			if (realPoints.size() > 0) {
				GPXPath curPath = new GPXPath(getName() + " - " + ipath);
				curPath.points.addAll(realPoints);
				curPath.computeArrays();

				double dist = curPath.points.get(curPath.points.size() - 1).getDist();
				long time = curPath.points.get(curPath.points.size() - 1).getTime() - curPath.points.get(0).getTime();

				double speed = dist / (time / 3600000.0);
				if (dist > 0.1 && speed > 3) {
					result.add(curPath);
					ipath++;
				}
			}
		}
		return ipath;
	}
}
