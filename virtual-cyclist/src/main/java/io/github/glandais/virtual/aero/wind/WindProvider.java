package io.github.glandais.virtual.aero.wind;

import io.github.glandais.gpx.Point;

public interface WindProvider {

    Wind getWind(Point location, double ellapsed);

}
