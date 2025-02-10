package io.github.glandais.virtual.aero.aero;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;

public interface AeroProvider {

    double getAeroCoef(Course course, Point location, CyclistStatus status);

}
