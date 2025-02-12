package io.github.glandais.virtual;

import io.github.glandais.gpx.data.Point;

public record CyclistStatus(
        Point location,
        // m
        double odo,
        // s
        double ellapsed,
        // m.s-2
        double speed,
        boolean end
) {

}
