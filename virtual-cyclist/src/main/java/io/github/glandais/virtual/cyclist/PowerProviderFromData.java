package io.github.glandais.virtual.cyclist;

import io.github.glandais.gpx.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PowerProviderFromData implements PowerProvider {

    @Override
    public String getId() {
        return "provided";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {

        Double p = location.getPower();
        if (p == null) {
            return 0;
        }
        return p;
    }
}
