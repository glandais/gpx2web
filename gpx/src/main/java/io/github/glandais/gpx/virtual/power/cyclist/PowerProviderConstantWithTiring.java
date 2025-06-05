package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;

public class PowerProviderConstantWithTiring extends PowerProviderConstant {

    private final double duration;

    public PowerProviderConstantWithTiring(double duration) {
        super();
        this.duration = duration;
    }

    @Override
    public double getOptimalPower(Course course, Point location) {

        double powerW = super.getOptimalPower(course, location);
        double c = Math.max(0.5, 1 - (0.6 * location.getElapsedSeconds() / duration));
        return powerW * c;
    }
}
