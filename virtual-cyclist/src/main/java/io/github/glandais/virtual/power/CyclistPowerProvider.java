package io.github.glandais.virtual.power;

import io.github.glandais.gpx.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import org.springframework.stereotype.Service;

@Service
public class CyclistPowerProvider implements PowerProvider {

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        return course.getCyclistPowerProvider().getPowerW(course, location, status);
    }
}
