package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;

public interface CyclistPowerProvider {
    double getPowerW(Course course, Point location);
}
