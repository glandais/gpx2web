package io.github.glandais.virtual;

import io.github.glandais.util.Constants;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Cyclist {

    private final double mKg;

    private final double maxBrakeMS2;

    private final double crr;

    private final double tanMaxAngle;

    private final double maxSpeedMs;

    public Cyclist() {

        this(72, 45, 90, 0.6, 0.005);
    }

    public Cyclist(double mKg, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG, double crr) {

        this.mKg = mKg;
        this.maxBrakeMS2 = maxBrakeG * Constants.G;
        this.crr = crr;
        this.tanMaxAngle = Math.tan(maxAngleDeg * (Math.PI / 180.0));
        this.maxSpeedMs = maxSpeedKmH / 3.6;
    }

}
