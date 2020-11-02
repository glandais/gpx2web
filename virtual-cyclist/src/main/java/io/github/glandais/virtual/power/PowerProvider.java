package io.github.glandais.virtual.power;

import io.github.glandais.gpx.Point;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface PowerProvider {

    double getPowerW(Point from, Point to, ZonedDateTime now, Duration ellapsed, double p_air, double p_frot, double p_grav, double v, double grad);

}
