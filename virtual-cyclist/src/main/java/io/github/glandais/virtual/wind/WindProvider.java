package io.github.glandais.virtual.wind;

import io.github.glandais.gpx.Point;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface WindProvider {

    Wind getWind(Point location, ZonedDateTime now, Duration ellapsed);

}
