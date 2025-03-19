package io.github.glandais.gpx.virtual.power.aero.aero;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;

public class AeroProviderConstant implements AeroProvider {

    public double getAeroCoef(Course course, Point location) {
        return course.getCyclist().getCd() * course.getCyclist().getA() * course.getRho() / 2;
    }
}
