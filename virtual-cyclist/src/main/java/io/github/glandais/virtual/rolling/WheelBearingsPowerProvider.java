package io.github.glandais.virtual.rolling;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Formul;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKey;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;

import javax.inject.Singleton;

@Singleton
public class WheelBearingsPowerProvider implements PowerProvider {

    public static final Formul FORMUL = new Formul("-(speed)*(91 + 8.7 * (speed))/1000",
            Unit.WATTS,
            new ValueKey("speed", ValueKind.staging)
    );

    @Override
    public String getId() {
        return "bearings";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {

        location.putDebug("p_" + getId(), FORMUL, Unit.FORMULA_WATTS);
        return -status.getSpeed() * (91 + 8.7 * status.getSpeed()) / 1000.0;
    }

}
