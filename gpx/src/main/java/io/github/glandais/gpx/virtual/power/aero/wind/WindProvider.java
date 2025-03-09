package io.github.glandais.gpx.virtual.power.aero.wind;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;

public interface WindProvider {

    Wind getWind(Course course, Point location);

}
