package org.glandais.gpx.braquet;

import org.glandais.srtm.loader.Point;

public class PointBraquet {

	private Braquet braquet;

	private Point point;

	private int iPlateau;

	private int iPignon;

	private double rpm;

	private double speed;

	private double dist;

	private long timeSinceShift;

	public PointBraquet(Braquet braquet, Point point, double dist, int iPlateau, int iPignon, double rpm, double speed,
			long timeSinceShift) {
		super();
		this.braquet = braquet;
		this.point = point;
		this.iPlateau = iPlateau;
		this.iPignon = iPignon;
		this.rpm = rpm;
		this.speed = speed;
		this.dist = dist;
		this.timeSinceShift = timeSinceShift;
	}

	public Braquet getBraquet() {
		return braquet;
	}

	public void setBraquet(Braquet braquet) {
		this.braquet = braquet;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public int getiPlateau() {
		return iPlateau;
	}

	public void setiPlateau(int iPlateau) {
		this.iPlateau = iPlateau;
	}

	public int getiPignon() {
		return iPignon;
	}

	public void setiPignon(int iPignon) {
		this.iPignon = iPignon;
	}

	public double getRpm() {
		return rpm;
	}

	public void setRpm(double rpm) {
		this.rpm = rpm;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	public long getTimeSinceShift() {
		return timeSinceShift;
	}

	public void setTimeSinceShift(long timeSinceShift) {
		this.timeSinceShift = timeSinceShift;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PointBraquet [point=");
		builder.append(point);
		builder.append(", dist=");
		builder.append(dist);
		builder.append(", iPlateau=");
		builder.append(braquet.pedalier.plateaux[iPlateau]);
		builder.append(", iPignon=");
		builder.append(braquet.cassette.pignons[iPignon]);
		builder.append(", rpm=");
		builder.append(rpm);
		builder.append(", speed=");
		builder.append(speed);
		builder.append("]");
		return builder.toString();
	}

}
