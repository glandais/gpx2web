package io.github.glandais.gpx.virtual;

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
        return new Bike(0.004, 0.05, 0.07, 0.7, 0.976);
    }

}
