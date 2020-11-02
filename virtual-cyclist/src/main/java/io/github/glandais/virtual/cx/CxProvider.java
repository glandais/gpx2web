package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.Point;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface CxProvider {

    double getCx(Point from, Point to, ZonedDateTime now, Duration ellapsed, double p_frot, double p_grav, double v, double grad);

}
