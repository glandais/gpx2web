package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cyclist {

    private double mKg;
    private double power;
    private boolean harmonics;
    private double maxBrakeG;
    private double cd;
    private double a;
    private double maxAngleDeg;
    private double maxSpeedKmH;

    public static Cyclist getDefault() {
        return new Cyclist(80, 280, false, 0.6, 0.7, 0.5, 35, 100);
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
