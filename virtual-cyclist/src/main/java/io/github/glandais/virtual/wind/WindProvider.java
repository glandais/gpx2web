package io.github.glandais.virtual.wind;

import io.github.glandais.gpx.Point;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

public interface WindProvider {

    Wind getWind(Point location, double ellapsed);

}
