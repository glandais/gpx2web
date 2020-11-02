package io.github.glandais.virtual;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Cyclist {

    private final double mKg;

    private final double maxAngleDeg;

    private final double maxSpeedKmH;

    private final double maxBrakeG;

    private final double f;

    private final double tanMaxAngle;

    private final double maxSpeedMs;

    public Cyclist() {

        this(72, 15, 90, 0.3, 0.005);
    }

    public Cyclist(double mKg, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG, double f) {

        this.mKg = mKg;
        this.maxAngleDeg = maxAngleDeg;
        this.maxSpeedKmH = maxSpeedKmH;
        this.maxBrakeG = maxBrakeG;
        this.f = f;
        this.tanMaxAngle = Math.tan(maxAngleDeg * (Math.PI / 180.0));
        this.maxSpeedMs = maxSpeedKmH / 3.6;
    }

}
