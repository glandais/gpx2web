package io.github.glandais.virtual.power;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import org.springframework.stereotype.Service;

@Service
public class GravPowerProvider implements PowerProvider {
    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        final double mKg = course.getCyclist().getMKg();
        double grad = location.getGrade();
        double p_grav = -mKg * Constants.G * status.getSpeed() * grad;
        location.putDebug("2_p_grav", p_grav, Unit.WATTS);
        return p_grav;
    }
}
