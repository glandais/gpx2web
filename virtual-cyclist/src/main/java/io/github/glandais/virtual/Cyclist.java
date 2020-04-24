package io.github.glandais.virtual;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Cyclist {

    private final double mKg;

    private final double powerW;

    private final double maxAngleDeg;

    private final double maxSpeedKmH;

    private final double maxBrakeG;

    private final double cx;

    private final double f;

    private final double tanMaxAngle;

    private final double maxSpeedMs;

    public Cyclist(double mKg, double powerW, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG) {

        this(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG, 0.30, 0.01);
    }

    public Cyclist(double mKg, double powerW, double maxAngleDeg, double maxSpeedKmH, double maxBrakeG, double cx, double f) {

        this(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG, cx, f, Math.tan(maxAngleDeg * (Math.PI / 180.0)), maxSpeedKmH / 3.6);
    }

    public Cyclist(final double mKg,
            final double powerW,
            final double maxAngleDeg,
            final double maxSpeedKmH,
            final double maxBrakeG,
            final double cx,
            final double f,
            final double tanMaxAngle,
            final double maxSpeedMs) {

        this.mKg = mKg;
        this.powerW = powerW;
        this.maxAngleDeg = maxAngleDeg;
        this.maxSpeedKmH = maxSpeedKmH;
        this.maxBrakeG = maxBrakeG;
        this.cx = cx;
        this.f = f;
        this.tanMaxAngle = tanMaxAngle;
        this.maxSpeedMs = maxSpeedMs;
    }
}
