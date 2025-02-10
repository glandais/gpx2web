package io.github.glandais.virtual;

public record Bike(
        double crr,
        double inertiaFront,
        double inertiaRear,
        double wheelRadius,
        double efficiency
) {

    public static Bike getDefault() {
        return new Bike(0.004, 0.05, 0.07, 0.7, 0.976);
    }

}
