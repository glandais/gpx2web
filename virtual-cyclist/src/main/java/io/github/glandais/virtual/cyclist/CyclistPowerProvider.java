package io.github.glandais.virtual.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class CyclistPowerProvider implements PowerProvider {

    @Override
    public String getId() {
        return "cyclist";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        double w = course.getCyclistPowerProvider().getPowerW(course, location, status);
        location.putDebug("p_" + getId(), w, Unit.WATTS);
        return w;
    }
}
