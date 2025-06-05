package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;

public class PowerProviderConstant extends CyclistPowerProviderBase {

    @Override
    protected double getOptimalPower(Course course, Point location) {
        return course.getCyclist().getPower();
    }
}
