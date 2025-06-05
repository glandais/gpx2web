package io.github.glandais.gpx.web.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record VirtualizationRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
                Instant startTime,
        CyclistParameters cyclist,
        BikeParameters bike,
        WindParameters wind) {

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