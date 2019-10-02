package io.github.glandais.virtual;

import java.time.ZonedDateTime;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;

public class Course {

	private GPXPath gpxPath;

	private Cyclist cyclist;

	private ZonedDateTime start;

	public Course(GPXPath gpxPath, Cyclist cyclist, ZonedDateTime start) {
		super();
		this.gpxPath = gpxPath;
		this.cyclist = cyclist;
		this.start = start;
	}

	public GPXPath getGpxPath() {
		return gpxPath;
	}

	public Cyclist getCyclist() {
		return cyclist;
	}

	public ZonedDateTime getStart() {
		return start;
	}

	public double getPowerW(Point from, Point to, double p_air, double p_frot, double p_grav, double v, double grad) {
		if (grad < -0.06) {
			return 0;
		} else if (grad < 0) {
			double c = 1 - (grad / -0.06);
			return cyclist.getPowerW() * c;
		} else {
			return cyclist.getPowerW();
		}
	}

	public double getMKg(Point from, double odo) {
		return cyclist.getmKg();
	}

	public double getCx(Point from, double odo) {
		return cyclist.getCx();
	}

	public double getF(Point from, double odo) {
		return cyclist.getF();
	}

}
