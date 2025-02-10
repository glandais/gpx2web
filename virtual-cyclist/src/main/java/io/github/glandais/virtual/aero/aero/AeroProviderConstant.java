package io.github.glandais.virtual.aero.aero;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;

public class AeroProviderConstant implements AeroProvider {

    public double getAeroCoef(Course course, Point location, CyclistStatus status) {
        return course.getCyclist().cd() * course.getCyclist().a() * course.getRho() / 2;
    }

}
