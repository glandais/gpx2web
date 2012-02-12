package org.glandais.gpx.elevation.fixer;

import org.glandais.srtm.loader.Point;

public class CheckPoint extends Point implements Comparable<CheckPoint> {

	private Point pRef = null;
	private Point pRefNext = null;
	private double coefRef = 0;
	private long tmin = 0;
	private double deniv = 0;

	public Point getpRef() {
		return pRef;
	}

	public void setpRef(Point pRef) {
		this.pRef = pRef;
	}

	public Point getpRefNext() {
		return pRefNext;
	}

	public void setpRefNext(Point pRefNext) {
		this.pRefNext = pRefNext;
	}

	public double getCoefRef() {
		return coefRef;
	}

	public void setCoefRef(double coefRef) {
		this.coefRef = coefRef;
	}

	public long getTmin() {
		return tmin;
	}

	public void setTmin(long tmin) {
		this.tmin = tmin;
	}

	public CheckPoint(double lon, double lat) {
		super(lon, lat);
	}

	public int compareTo(CheckPoint o) {
		return Double.compare(getDist(), o.getDist());
	}

	public double getDeniv() {
		return deniv;
	}

	public void setDeniv(double deniv) {
		this.deniv = deniv;
	}

}
