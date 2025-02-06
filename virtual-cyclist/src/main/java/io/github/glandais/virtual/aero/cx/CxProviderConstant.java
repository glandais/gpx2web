package io.github.glandais.virtual.aero.cx;

import io.github.glandais.gpx.data.Point;
import lombok.AllArgsConstructor;

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
