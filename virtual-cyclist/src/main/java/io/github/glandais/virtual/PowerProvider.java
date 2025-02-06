package io.github.glandais.virtual;

import io.github.glandais.gpx.data.Point;

public interface PowerProvider {

    double getPowerW(Course course, Point location, CyclistStatus status);

    String getId();
}
