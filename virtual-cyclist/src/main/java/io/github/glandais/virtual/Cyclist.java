package io.github.glandais.virtual;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Cyclist {

    private final double mKg;

    private final double maxBrakeG;

    private final double crr;

    private final double tanMaxAngle;

    private final double maxSpeedMs;

    public Cyclist() {

        this(72, 15, 90, 0.3, 0.005);
    }

    public Cyclist(double mKg, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG, double crr) {

        this.mKg = mKg;
        this.maxBrakeG = maxBrakeG;
        this.crr = crr;
        this.tanMaxAngle = Math.tan(maxAngleDeg * (Math.PI / 180.0));
        this.maxSpeedMs = maxSpeedKmH / 3.6;
    }

}
