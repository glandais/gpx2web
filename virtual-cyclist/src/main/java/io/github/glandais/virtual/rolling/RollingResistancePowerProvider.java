package io.github.glandais.virtual.rolling;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import org.springframework.stereotype.Service;

@Service
public class RollingResistancePowerProvider implements PowerProvider {

    @Override
    public String getId() {
        return "rr";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {

        final double mKg = course.getCyclist().getMKg();
        final double crr = course.getCyclist().getCrr();
        final double grade = location.getGrade();

        double coef = Math.cos(Math.atan(grade));
        double p_rr = -coef * mKg * Constants.G * status.getSpeed() * crr;

        location.putDebug("3_0_crr", crr, Unit.PERCENTAGE);
        location.putDebug("3_1_p_rr", p_rr, Unit.WATTS);
        return p_rr;
    }
}
