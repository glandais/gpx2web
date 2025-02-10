package io.github.glandais.virtual.aero.wind;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;

public interface WindProvider {

    Wind getWind(Course course, Point location, CyclistStatus status);

}
