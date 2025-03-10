package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKey;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.power.PowerProvider;
import io.github.glandais.gpx.virtual.power.PowerProviderId;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class MuscularPowerProvider implements PowerProvider {

    @Override
    public PowerProviderId getId() {
        return PowerProviderId.cyclist;
    }

    @Override
    public double getPowerW(Course course, Point location) {
        double w = course.getCyclistPowerProvider().getPowerW(course, location);
        location.putDebug(ValueKey.p_cyclist_raw, w, Unit.WATTS);
        w = w * course.getBike().getEfficiency();
        return w;
    }

}
