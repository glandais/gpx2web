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

	public void processPoint(double lon, double lat, long date, boolean fixZ)
			throws SRTMException {
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
					p.setZ(SRTMHelper.getInstance().getElevation(p.getLon(),
							p.getLat()));
					points.add(p);
				} else {
					List<Point> subPoints = SRTMHelper.getInstance()
							.getPointsBetween(previousPoint, p);
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
				double dist = points.get(i).getDist()
						- points.get(i - 1).getDist();
				double newMaxSpeedPrevious = bikeTimeEval.getMaxSpeed(maxSpeed,
						dist);
				points.get(i - 1).setMaxSpeed(newMaxSpeedPrevious);
			}
		}
		/*
		 * double[] maxSpeeds = new double[points.size()]; for (int i = 0; i <
		 * maxSpeeds.length; i++) { maxSpeeds[i] = points.get(i).getMaxSpeed();
		 * }
		 * 
		 * for (int j = 0; j < maxSpeeds.length; j++) { double newMaxSpeed =
		 * computeNewValue(j, 0.1, 0.1, maxSpeeds); Point p = points.get(j);
		 * p.setMaxSpeed(newMaxSpeed); }
		 */
	}

	public void computeArrays() {
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

		computeMinMax();

		bikeTimeEval.computeTrack(points);

		for (int j = 0; j < points.size(); j++) {
			time[j] = points.get(j).getTime();
		}

	}

	private void computeMinMax() {
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
					long n = 1 + Math.round(Math
							.floor((dist * 1.0) / (d * 1.0)));
					for (int j = 1; j < n; j++) {
						float c = (j * 1.0f) / (n * 1.0f);
						Point np = new Point(p).mul(c).add(
								new Point(lastPoint).mul(1 - c));
						newPoints.add(np);
					}
				}
				newPoints.add(p);
				lastPoint = p;
			}
		}
		points = newPoints;
	}

	public void filterPoints() {
		points = filterPoints(points);
	}

	public static List<Point> filterPoints(List<Point> points) {
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

	public double computeNewValue(int i, double before, double after,
			double[] data) {
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

	public double computeNewValueTime(int i, long timeBefore, long timeAfter,
			double[] data) {
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
			double c = 1 - (Math.abs(time[j] - ac) / Math.max(timeBefore,
					timeAfter));
			totc = totc + c;
			totz = totz + data[j] * c;
		}

		if (totc == 0) {
			return data[i];
		} else {
			return totz / totc;
		}

	}

	public void amplify() {
		double[] speeds = getSpeeds();

		double totDist = 0;
		double totTime = 0;

		Point lastPoint = getPoints().get(0);
		for (int i = 0; i < getPoints().size() - 1; i++) {
			Point p = getPoints().get(i + 1);
			long dt = p.getTime() - lastPoint.getTime();
			double invSpeed = computeNewValueTime(i, 15000, 15000, speeds);
			double curSpeed = 1 / invSpeed;
			double dist = lastPoint.distanceTo(p);
			if (curSpeed > 2.0) {
				totDist = totDist + dist;
				totTime = totTime + dt;
			}
			// braquet.applySpeed(p, curSpeed, dt, dist);
			lastPoint = p;
		}

		double average = totDist / (totTime / 3600000);

		long newTime = adjustSpeedAmplify(average, speeds);
		double ratio = (totTime * 1.0) / (newTime * 1.0);
		adjustSpeedSameLength(ratio);

		System.out.println(average);
	}

	private double[] getSpeeds() {
		Point lastPoint = null;
		double[] speed = new double[getPoints().size() - 1];
		for (int i = 0; i < getPoints().size(); i++) {
			Point p = getPoints().get(i);
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
		return speed;
	}

	private long adjustSpeedAmplify(double average, double[] speeds) {
		long time = getPoints().get(0).getTime();
		long startTime = time;
		Point lastPoint = getPoints().get(0);

		for (int i = 0; i < getPoints().size() - 1; i++) {
			Point p = getPoints().get(i + 1);
			long dt = p.getTime() - lastPoint.getTime();
			double invSpeed = computeNewValueTime(i, 15000, 15000, speeds);

			double curSpeed = 1 / invSpeed;
			double dist = lastPoint.distanceTo(p);
			if (curSpeed > 2.0) {

				double relSpeed = curSpeed / average;

				relSpeed = relSpeed * relSpeed * relSpeed * relSpeed;

				relSpeed = relSpeed * average;

				double dtDouble = dist / relSpeed;
				dtDouble = dtDouble * 3600 * 1000;
				dt = Math.round(dtDouble);

			}

			time = time + dt;

			p.setTime(time);

			lastPoint = p;
		}

		return time - startTime;
	}

	private void adjustSpeedSameLength(double ratio) {
		long time = getPoints().get(0).getTime();

		long[] times = new long[points.size()];
		for (int i = 0; i < getPoints().size(); i++) {
			times[i] = getPoints().get(i).getTime();
		}

		for (int i = 0; i < getPoints().size() - 1; i++) {

			double dtDouble = times[i + 1] - times[i];
			dtDouble = dtDouble * ratio;
			long dt = Math.round(dtDouble);
			Point p = getPoints().get(i + 1);
			time = time + dt;
			p.setTime(time);

		}

	}

}
