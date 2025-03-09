package io.github.glandais.gpx.virtual.power.aero.aero;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;

public interface AeroProvider {

    double getAeroCoef(Course course, Point location);

}
