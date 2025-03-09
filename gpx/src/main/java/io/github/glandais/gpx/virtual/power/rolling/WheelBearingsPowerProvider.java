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

//    public static final Formul FORMUL = new Formul("-(speed)*(91 + 8.7 * (speed))/1000",
//            Unit.WATTS,
//            new ValueKey("speed", ValueKind.staging)
//    );

    @Override
    public PowerProviderId getId() {
        return PowerProviderId.bearings;
    }

    @Override
    public double getPowerW(Course course, Point location) {

//        location.putDebug("p_" + getId(), FORMUL, Unit.FORMULA_WATTS);
        return -location.getSpeed() * (91 + 8.7 * location.getSpeed()) / 1000.0;
    }

}
