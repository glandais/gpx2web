package io.github.glandais.guesser;

import io.github.glandais.virtual.Cyclist;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CyclistWithScore extends Cyclist {

	private double score;

	public CyclistWithScore(double mKg, double powerW, double cx, double f, double maxAngleDeg, double maxSpeedKmH,
			double maxBrakeG) {
		super(mKg, powerW, cx, f, maxAngleDeg, maxSpeedKmH, maxBrakeG);
	}

}
