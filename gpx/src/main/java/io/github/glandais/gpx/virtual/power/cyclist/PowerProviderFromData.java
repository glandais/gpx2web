package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PowerProviderFromData implements CyclistPowerProvider {

    @Override
    public double getPowerW(Course course, Point location) {

        return location.getPower();
    }
}
