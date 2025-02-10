package io.github.glandais.virtual;

import io.github.glandais.util.Constants;

public record Cyclist(
        double mKg,
        double power,
        double maxBrakeG,
        double cd,
        double a,
        double maxAngleDeg,
        double maxSpeedKmH
) {

    public static Cyclist getDefault() {
        return new Cyclist(80, 280, 0.6, 0.7, 0.5, 45, 90);
    }

    public double getTanMaxAngle() {
        return Math.tan(maxAngleDeg * (Math.PI / 180.0));
    }

    public double getMaxBrakeMS2() {
        return maxBrakeG * Constants.G;
    }

    public double getMaxSpeedMs() {
        return maxSpeedKmH / 3.6;
    }

}
