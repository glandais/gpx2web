package io.github.glandais.gpx.web.model;

import java.time.Instant;

public record VirtualizationRequest(
        Instant startTime, CyclistParameters cyclist, BikeParameters bike, WindParameters wind) {

    public record CyclistParameters(
            double weightKg,
            double powerWatts,
            boolean harmonics,
            double maxBrakeG,
            double dragCoefficient,
            double frontalAreaM2,
            double maxAngleDeg,
            double maxSpeedKmH) {}

    public record BikeParameters(
            double rollingResistance,
            double frontWheelInertia,
            double rearWheelInertia,
            double wheelRadiusM,
            double efficiency) {}

    public record WindParameters(double speedMs, double directionDeg) {}
}
