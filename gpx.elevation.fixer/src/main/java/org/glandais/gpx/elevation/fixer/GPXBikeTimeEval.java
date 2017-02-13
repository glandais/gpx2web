package org.glandais.gpx.elevation.fixer;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.glandais.srtm.loader.Point;

public class GPXBikeTimeEval {

	private double latRef = 0;
	private double lonRef = 0;
	private double cRef = 0;
	private static final double CIRC = 40000000.0;

	DecimalFormat speedformat = new DecimalFormat("#0.0");

	private double maxSpeed = 36.0 / 3.6;

	// poids en kg
	private double m;

	// puissance moyenne
	private double max_power;
	private double freewheel_power;

	// g
	private double g = 9.8;

	private Calendar[] starts = { Calendar.getInstance() };
	private int counter = 0;
	private int dday = 0;
	private double tanMaxAngle;
	private double maxBrake;

	/**
	 * @param m
	 *            kg
	 * @param puissance
	 *            W
	 * @param freewheel_power
	 * @param maxAngle
	 * @param maxSpeed
	 * @param maxBrake
	 *            g unit
	 */
	public GPXBikeTimeEval(double m, double puissance, double freewheel_power, double maxAngle, double maxSpeed,
			double maxBrake) {
		super();
		this.m = m;
		this.max_power = puissance;
		this.freewheel_power = freewheel_power;
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

	public void computeTrack(List<Point> points) {
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

					// System.out.println(p.getDist() + " - " + (v * 3.6) + " ("
					// + p.getMaxSpeed() * 3.6 + ")");

					// ms ellapsed time
					long ts = Math.round(time * 1000);
					currentTime = currentTime + ts;
				}
			}
			p.setTime(currentTime);
		}
	}

	public void computeMaxSpeeds(List<Point> points) {
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
			double maxSpeed = points.get(i).getMaxSpeed();
			double maxSpeedPrevious = points.get(i - 1).getMaxSpeed();
			// we have to brake!
			if (maxSpeed < maxSpeedPrevious) {
				double dist = points.get(i).getDist() - points.get(i - 1).getDist();
				double newMaxSpeedPrevious = getMaxSpeedByBraking(maxSpeed, dist);
				points.get(i - 1).setMaxSpeed(newMaxSpeedPrevious);
			}
		}
	}

	private double getMaxSpeedByIncline(Point pm1, Point p, Point pp1) {
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

	private double getMaxSpeedByBraking(double maxSpeed, double dist) {
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
	 * @param speed
	 *            m/h
	 * @param dist
	 *            km
	 * @param grad
	 *            %
	 * @param ms2
	 * @param ms1
	 * @return
	 */
	private Point2D.Double getTimeForDist(double v, double dist, double grad, double ms1, double ms2) {
		// we will split the distance
		// m
		double dmax = dist * 1000.0;
		// m
		double d = 0.0;
		// s
		double t = 0.0;
		// s
		double dt = 0.25;
		while (true) {
			// OK
			// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
			double p_frot = 0.26 * v * v * v + 0.1 * m * v;
			// OK
			// (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
			double p_grav = m * g * v * Math.sin(Math.atan(grad / 100.0));
			// total resistance power
			double p_res = p_frot + p_grav;

			double p_cyclist = max_power;
			if (p_grav < -max_power + freewheel_power) {
				p_cyclist = freewheel_power;
			} else if (p_grav > -max_power + freewheel_power && p_grav < 0) {
				p_cyclist = p_grav + max_power;
			}

			// p_app = cyclist power - resistance
			double p_app = p_cyclist - p_res;

			// m.s-2
			double acc = p_app / m;
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

	private static Point getCircleCenter(Point a, Point b, Point c) {
		double ax = a.getLon();
		double ay = a.getLat();
		double bx = b.getLon();
		double by = b.getLat();
		double cx = c.getLon();
		double cy = c.getLat();

		double A = bx - ax;
		double B = by - ay;
		double C = cx - ax;
		double D = cy - ay;

		double E = A * (ax + bx) + B * (ay + by);
		double F = C * (ax + cx) + D * (ay + cy);

		double G = 2 * (A * (cy - by) - B * (cx - bx));
		if (Math.abs(G) < 0.001)
			return null; // a, b, c must be collinear

		double px = (D * E - B * F) / G;
		double py = (A * F - C * E) / G;
		return new Point(px, py);
	}

	private Point transform(Point point) {
		double lon = (point.getLon() - lonRef) * (Math.PI / 180.0);
		double lat = (point.getLat() - latRef) * (Math.PI / 180.0);
		double x = lon * cRef / (2 * 3.14);
		double y = lat * CIRC / (2 * 3.14);
		return new Point(x, y);
	}

}
