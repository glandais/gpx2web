package io.github.glandais.virtual.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;

public class PowerProviderConstantWithTiring extends PowerProviderConstant {

    private final double duration;

    @Override
    public String getId() {
        return "tiring";
    }

    public PowerProviderConstantWithTiring(double duration) {
        this.duration = duration;
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {

        double powerW = super.getPowerW(course, location, status);
        double c = Math.max(0.5, 1 - (0.6 * status.getEllapsed() / duration));
        return powerW * c;
    }
}
