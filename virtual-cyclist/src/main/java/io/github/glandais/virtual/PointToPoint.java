package io.github.glandais.virtual;

public class PointToPoint {

	private double time;

	private double endSpeed;

	public PointToPoint(double time, double endSpeed) {
		super();
		this.time = time;
		this.endSpeed = endSpeed;
	}

	public double getTime() {
		return time;
	}

	public double getEndSpeed() {
		return endSpeed;
	}

}
