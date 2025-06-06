package io.github.glandais.gpx.virtual.power.rolling;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKeys;
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
        double powerWheelBearings = -location.getSpeed() * (91 + 8.7 * location.getSpeed()) / 1000.0;
        location.putDebug(PropertyKeys.p_wheel_bearings, powerWheelBearings);
        return powerWheelBearings;
    }
}
