package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.Point;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

public interface CxProvider {

    double getCx(Point location, double ellapsed, double speed, double grad);

}
