package io.github.glandais.virtual.frot;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import org.springframework.stereotype.Service;

@Service
public class FrotPowerProvider implements PowerProvider {

    @Override
    public String getId() {
        return "frot";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {

        final double mKg = course.getCyclist().getMKg();
        final double f = course.getCyclist().getF();
        double p_frot = -mKg * Constants.G * status.getSpeed() * f;
        location.putDebug("3_0_f", f, Unit.PERCENTAGE);
        location.putDebug("3_1_p_frot", p_frot, Unit.WATTS);
        return p_frot;
    }
}
