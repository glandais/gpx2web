package org.glandais.gpx.elevation.fixer;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.glandais.gpx.srtm.Point;

public class GPXBikeTimeEval {

	private double latRef = 0;
	private double lonRef = 0;
	private double cRef = 0;
	private static final double CIRC = 40000000.0;

	private double maxSpeed = 36.0 / 3.6;

	// poids en kg
	private double m;

	// puissance moyenne
	private double maxPower;
	private double freewheelPower;

	// g
	private double g = 9.8;

	private Calendar[] starts = { Calendar.getInstance() };
	private int counter = 0;
	private int dday = 0;
	private double tanMaxAngle;
	private double maxBrake;

	/**
	 * @param m              kg
	 * @param puissance      W
	 * @param freewheelPower
	 * @param maxAngle
	 * @param maxSpeed
	 * @param maxBrake       g unit
	 */
	public GPXBikeTimeEval(double m, double puissance, double freewheelPower, double maxAngle, double maxSpeed,
			double maxBrake) {
		super();
		this.m = m;
		this.maxPower = puissance;
		this.freewheelPower = freewheelPower;
		this.tanMaxAngle = Math.tan(maxAngle * (Math.PI / 180.0));
		this.maxSpeed = maxSpeed / 3.6;
		this.maxBrake = maxBrake * g;
	}

	public void setStarts(Calendar[] starts) {
		this.starts = starts;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public Calendar getNextStart() {
		if (counter < starts.length) {
			Calendar start = starts[counter];
			counter++;
			return start;
		} else {
			dday++;
			GregorianCalendar start = (GregorianCalendar) starts[starts.length - 1].clone();
			start.add(Calendar.DAY_OF_YEAR, dday);
			return start;
		}
	}

	public void computeVirtualTime(GPXPath gpxPath) {
		List<Point> points = gpxPath.getPoints();
		computeMaxSpeeds(points);
		computeTrack(points);
		gpxPath.setPoints(points);
	}

	protected void computeTrack(List<Point> points) {
		Calendar start = getNextStart();
		long currentTime = start.getTimeInMillis();

		// m/h
		double v = 5.0 / 3.6;
		double time = 0;
		for (int j = 0; j < points.size(); j++) {
			Point p = points.get(j);
			double dist = 0;
			if (j > 0) {
				Point prevPoint = points.get(j - 1);
				// en km double
				dist = prevPoint.distanceTo(p);
				if (dist > 0) {
					double dz = p.getZ() - prevPoint.getZ();
					double pente = (dz * 0.1) / dist;

					// max speeds
					double pms = prevPoint.getMaxSpeed();
					double ms = p.getMaxSpeed();

					// temps en secondes
					Double timeSpeed = getTimeForDist(v, dist, pente, pms, ms);

					time = timeSpeed.x;
					v = timeSpeed.y;

					// ms ellapsed time
					long ts = Math.round(time * 1000);
					currentTime = currentTime + ts;
				}
			}
			p.setTime(currentTime);
		}
	}

	protected void computeMaxSpeeds(List<Point> points) {
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0 || i == points.size() - 1) {
				p.setMaxSpeed(getMaxSpeed());
			} else {
				Point pm1 = points.get(i - 1);
				Point pp1 = points.get(i + 1);
				p.setMaxSpeed(getMaxSpeedByIncline(pm1, p, pp1));
			}
		}

		for (int i = points.size() - 1; i > 0; i--) {
			double maxSpeedCurrent = points.get(i).getMaxSpeed();
			double maxSpeedPrevious = points.get(i - 1).getMaxSpeed();
			// we have to brake!
			if (maxSpeedCurrent < maxSpeedPrevious) {
				double dist = points.get(i).getDist() - points.get(i - 1).getDist();
				double newMaxSpeedPrevious = getMaxSpeedByBraking(maxSpeedCurrent, dist);
				points.get(i - 1).setMaxSpeed(newMaxSpeedPrevious);
			}
		}
	}

	protected double getMaxSpeedByIncline(Point pm1, Point p, Point pp1) {
		lonRef = p.getLon();
		latRef = p.getLat();
		cRef = CIRC * Math.cos(latRef * (Math.PI / 180.0));

		Point tpm1 = transform(pm1);
		Point tp = transform(p);
		Point tpp1 = transform(pp1);

		Point circleCenter = getCircleCenter(tpm1, tp, tpp1);
		if (circleCenter == null) {
			return maxSpeed;
		}
		Point rad = circleCenter.copy().sub(tp);
		// m
		double radius = Math.sqrt(rad.getLon() * rad.getLon() + rad.getLat() * rad.getLat());

		if (radius > 1000) {
			return maxSpeed;
		}
		double vmax = Math.sqrt(g * radius * tanMaxAngle);
		return Math.min(maxSpeed, vmax);
	}

	protected double getMaxSpeedByBraking(double maxSpeed, double dist) {
		// discrete resolution, i'm so lazy...
		double dmax = dist * 1000;
		// m
		double d = 0.0;
		// s
		double t = 0.0;
		// s
		double dt = 0.01;
		double v = maxSpeed;
		while (true) {
			double dv = maxBrake * dt;
			v = v + dv;

			double dx = dt * v;
			d = d + dx;
			t = t + dt;
			if (v > this.maxSpeed) {
				return this.maxSpeed;
			}
			if (d > dmax) {
				double ratio = (dmax - (d - dx)) / dx;
				return Math.min(this.maxSpeed, v - dv + dv * ratio);
			}
		}
	}

	/**
	 * @param speed m/h
	 * @param dist  km
	 * @param grad  %
	 * @param ms2
	 * @param ms1
	 * @return
	 */
	protected Point2D.Double getTimeForDist(double v, double dist, double grad, double ms1, double ms2) {
		// we will split the distance
		// m
		double dmax = dist * 1000.0;
		// m
		double d = 0.0;
		// s
		double t = 0.0;
		// s
		double dt = 0.2;
		while (true) {
			// OK
			// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
			double pFrot = 0.26 * v * v * v + 0.1 * m * v;
			// OK
			// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
			double pGrav = m * g * v * Math.sin(Math.atan(grad / 100.0));
			// total resistance power
			double pRes = pFrot + pGrav;

			double pCyclist = maxPower;
			if (pGrav < -maxPower + freewheelPower) {
				pCyclist = freewheelPower;
			} else if (pGrav > -maxPower + freewheelPower && pGrav < 0) {
				pCyclist = pGrav + maxPower;
			}

			// p_app = cyclist power - resistance
			double pApp = pCyclist - pRes;

			// m.s-2
			double acc = pApp / m;
			v = v + acc * dt;

			// Compute max speed
			double c = d / dmax;
			double ms = Math.min(maxSpeed, ms1 + c * (ms2 - ms1));
			if (v > ms) {
				v = ms;
			}

			double dx = dt * v;
			d = d + dx;
			t = t + dt;
			if (d > dmax) {
				double ratio = (dmax - (d - dx)) / dx;
				v = Math.min(v, ms2);
				return new Point2D.Double(t - dt + dt * ratio, v);
			}
		}
	}

	protected static Point getCircleCenter(Point p1, Point p2, Point p3) {
		double ax = p1.getLon();
		double ay = p1.getLat();
		double bx = p2.getLon();
		double by = p2.getLat();
		double cx = p3.getLon();
		double cy = p3.getLat();

		double a = bx - ax;
		double b = by - ay;
		double c = cx - ax;
		double d = cy - ay;

		double e = a * (ax + bx) + b * (ay + by);
		double f = c * (ax + cx) + d * (ay + cy);

		double G = 2 * (a * (cy - by) - b * (cx - bx));
		if (Math.abs(G) < 0.001)
			return null; // p1, p2, p3 must be collinear

		double px = (d * e - b * f) / G;
		double py = (a * f - c * e) / G;
		return new Point(px, py);
	}

	protected Point transform(Point point) {
		double lon = (point.getLon() - lonRef) * (Math.PI / 180.0);
		double lat = (point.getLat() - latRef) * (Math.PI / 180.0);
		double x = lon * cRef / (2 * 3.14);
		double y = lat * CIRC / (2 * 3.14);
		return new Point(x, y);
	}

}
