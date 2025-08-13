package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bike {

    double crr;
    double inertiaFront;
    double inertiaRear;
    double wheelRadius;
    double efficiency;

    public static Bike getDefault() {
        return new Bike(
                Constants.DEFAULT_CRR,
                Constants.DEFAULT_INERTIA_FRONT,
                Constants.DEFAULT_INERTIA_REAR,
                Constants.DEFAULT_WHEEL_RADIUS,
                Constants.DEFAULT_DRIVETRAIN_EFFICIENCY);
    }
}
