package io.github.glandais.virtual.aero.wind;

import io.github.glandais.gpx.data.Point;

public interface WindProvider {

    Wind getWind(Point location, double ellapsed);

}
