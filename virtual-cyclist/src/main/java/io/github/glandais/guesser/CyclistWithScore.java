package io.github.glandais.guesser;

import io.github.glandais.virtual.Cyclist;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CyclistWithScore extends Cyclist {

	@Getter
	@Setter
	private double score;

	public CyclistWithScore(double mKg, double powerW, double maxAngleDeg, double maxSpeedKmH,
			double maxBrakeG, double cx, double f) {
		super(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG, cx, f);
	}

}
