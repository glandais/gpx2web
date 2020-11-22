package io.github.glandais.virtual.aero.cx;

import io.github.glandais.gpx.Point;

public interface CxProvider {

    double getCx(Point location, double ellapsed, double speed, double grade);

}
