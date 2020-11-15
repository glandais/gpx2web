package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.Point;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class CxProviderConstant implements CxProvider {

    private final double cx;

    public CxProviderConstant() {
        this(0.30);
    }

    public double getCx(Point location, double ellapsed, double speed, double grade) {
        return cx;
    }

    public double getCx() {
        return cx;
    }

}
