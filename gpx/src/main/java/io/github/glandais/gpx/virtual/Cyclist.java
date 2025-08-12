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
        return new Cyclist(
            Constants.DEFAULT_CYCLIST_MASS_KG,
            Constants.DEFAULT_CYCLIST_POWER_W,
            false, // harmonics - not a physical parameter
            Constants.DEFAULT_MAX_BRAKE_G,
            Constants.DEFAULT_DRAG_COEFFICIENT,
            Constants.DEFAULT_FRONTAL_AREA,
            Constants.DEFAULT_MAX_LEAN_ANGLE_DEG,
            Constants.DEFAULT_MAX_SPEED_KMH
        );
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
