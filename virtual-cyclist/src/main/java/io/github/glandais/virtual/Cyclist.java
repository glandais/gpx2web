package io.github.glandais.virtual;

import io.github.glandais.util.Constants;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Cyclist {

	private double mKg;
	private double powerW;
	private double maxSpeedMs;
	private double tanMaxAngle;
	private double maxBrake;
	private double cx;
	private double f;

	public Cyclist(double mKg, double powerW, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG) {
		this(mKg, powerW, 0.30, 0.01, maxAngleDeg, maxSpeedKmH, maxBrakeG);
	}

	public Cyclist(double mKg, double powerW, double cx, double f, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG) {
		super();

		this.mKg = mKg;
		this.powerW = powerW;
		this.cx = cx;
		this.f = f;
		this.tanMaxAngle = Math.tan(maxAngleDeg * (Math.PI / 180.0));
		this.maxSpeedMs = maxSpeedKmH / 3.6;
		this.maxBrake = maxBrakeG * Constants.G;
	}

	public double getmKg() {
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

	public void setmKg(double mKg) {
		this.mKg = mKg;
	}

	public void setCx(double cx) {
		this.cx = cx;
	}

	public void setF(double f) {
		this.f = f;
	}

}
