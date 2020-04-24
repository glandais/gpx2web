package io.github.glandais.virtual;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.glandais.gpx.Point;
import io.github.glandais.util.Constants;
import io.github.glandais.util.Point2D;

@Service
public class MaxSpeedComputer {

	private static final double CIRC = 40000000.0;

	public void computeMaxSpeeds(Course course) {

		List<Point> points = course.getGpxPath().getPoints();
		Cyclist cyclist = course.getCyclist();
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i == 0 || i == points.size() - 1) {
				p.setMaxSpeed(cyclist.getMaxSpeedMs());
			} else {
				Point pm1 = points.get(i - 1);
				Point pp1 = points.get(i + 1);
				p.setMaxSpeed(getMaxSpeedByIncline(pm1, p, pp1, cyclist));
			}
		}

		for (int i = points.size() - 1; i > 0; i--) {
			double maxSpeedCurrent = points.get(i).getMaxSpeed();
			double maxSpeedPrevious = points.get(i - 1).getMaxSpeed();
			// we have to brake!
			if (maxSpeedCurrent < maxSpeedPrevious) {
				double dist = points.get(i).getDist() - points.get(i - 1).getDist();
				double newMaxSpeedPrevious = getMaxSpeedByBraking(maxSpeedCurrent, dist, cyclist);
				points.get(i - 1).setMaxSpeed(newMaxSpeedPrevious);
			}
		}
	}

	private double getMaxSpeedByIncline(Point pm1, Point p, Point pp1, Cyclist cyclist) {
		double lonRef = p.getLon();
		double latRef = p.getLat();
		double cRef = CIRC * Math.cos(latRef * (Math.PI / 180.0));

		Point2D tpm1 = transform(pm1, cRef, lonRef, latRef);
		Point2D tp = transform(p, cRef, lonRef, latRef);
		Point2D tpp1 = transform(pp1, cRef, lonRef, latRef);

		Point2D circleCenter = getCircleCenter(tpm1, tp, tpp1);
		if (circleCenter == null) {
			return cyclist.getMaxSpeedMs();
		}
		Point2D rad = circleCenter.sub(tp);
		// m
		double radius = Math.sqrt(rad.getX() * rad.getX() + rad.getY() * rad.getY());

		if (radius > 1000) {
			return cyclist.getMaxSpeedMs();
		}
		double vmax = Math.sqrt(Constants.G * radius * cyclist.getTanMaxAngle());
		return Math.min(cyclist.getMaxSpeedMs(), vmax);
	}

	private double getMaxSpeedByBraking(double maxSpeedCurrent, double dist, Cyclist cyclist) {
		// discrete resolution, i'm so lazy...
		double dmax = dist * 1000;
		// m
		double d = 0.0;
		// s
		double t = 0.0;
		// s
		double dt = 0.01;
		double v = maxSpeedCurrent;
		while (true) {
			double dv = cyclist.getMaxBrakeG() * dt;
			v = v + dv;

			double dx = dt * v;
			d = d + dx;
			t = t + dt;
			if (v > cyclist.getMaxSpeedMs()) {
				return cyclist.getMaxSpeedMs();
			}
			if (d > dmax) {
				double ratio = (dmax - (d - dx)) / dx;
				return Math.min(cyclist.getMaxSpeedMs(), v - dv + dv * ratio);
			}
		}
	}

	private static Point2D getCircleCenter(Point2D a, Point2D b, Point2D c) {
		double ax = a.getX();
		double ay = a.getY();
		double bx = b.getX();
		double by = b.getY();
		double cx = c.getX();
		double cy = c.getY();

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
		return new Point2D(px, py);
	}

	private Point2D transform(Point point, double cRef, double lonRef, double latRef) {
		double lon = (point.getLon() - lonRef) * (Math.PI / 180.0);
		double lat = (point.getLat() - latRef) * (Math.PI / 180.0);
		double x = lon * cRef / (2 * Math.PI);
		double y = lat * CIRC / (2 * Math.PI);
		return new Point2D(x, y);
	}

}
