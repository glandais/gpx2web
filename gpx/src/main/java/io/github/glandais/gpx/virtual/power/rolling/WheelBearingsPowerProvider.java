package io.github.glandais.gpx.virtual.power.rolling;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.power.PowerProvider;
import io.github.glandais.gpx.virtual.power.PowerProviderId;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class WheelBearingsPowerProvider implements PowerProvider {

    @Override
    public PowerProviderId getId() {
        return PowerProviderId.bearings;
    }

    @Override
    public double getPowerW(Course course, Point location) {
        return -location.getSpeed() * (91 + 8.7 * location.getSpeed()) / 1000.0;
    }
}
