package io.github.glandais.virtual;

import io.github.glandais.gpx.Point;

public interface PowerProvider {

    double getPowerW(Course course, Point location, CyclistStatus status);

}
