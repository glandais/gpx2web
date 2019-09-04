package io.github.glandais.virtual;

import io.github.glandais.util.Constants;

public class Cyclist {

	private double mKg;
	private double powerW;
	private double maxSpeedMs;
	private double tanMaxAngle;
	private double maxBrake;
	private double cx = 0.30;
	private double f = 0.01;

	public Cyclist(double mKg, double powerW, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG) {
		super();

		this.mKg = mKg;
		this.powerW = powerW;
		this.tanMaxAngle = Math.tan(maxAngleDeg * (Math.PI / 180.0));
		this.maxSpeedMs = maxSpeedKmH / 3.6;
		this.maxBrake = maxBrakeG * Constants.G;
	}

	public double getMKg() {
		return mKg;
	}

	public double getPowerW() {
		return powerW;
	}

	public double getMaxSpeedMs() {
		return maxSpeedMs;
	}

	public double getTanMaxAngle() {
		return tanMaxAngle;
	}

	public double getMaxBrake() {
		return maxBrake;
	}

	public double getCx() {
		return cx;
	}
	
	public double getF() {
		return f;
	}

}
