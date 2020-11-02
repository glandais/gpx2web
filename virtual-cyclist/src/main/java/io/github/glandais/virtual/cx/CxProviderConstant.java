package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.Point;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class CxProviderConstant implements CxProvider {

    private final double cx;

    public CxProviderConstant() {
        this(0.30);
    }

    public double getCx(Point from, Point to, ZonedDateTime now, Duration ellapsed, double p_frot, double p_grav, double v, double grad) {
        return cx;
    }

    public double getCx() {
        return cx;
    }

}
