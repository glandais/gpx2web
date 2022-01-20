package io.github.glandais.virtual.grav;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Formul;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKey;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;

import javax.inject.Singleton;

@Singleton
public class GravPowerProvider implements PowerProvider {

    public static final Formul FORMUL = new Formul("-mKg*9.81*(speed)*SIN(ATAN(grade))",
            Unit.WATTS,
            new ValueKey("mKg", ValueKind.debug),
            new ValueKey("speed", ValueKind.staging),
            new ValueKey("grade", ValueKind.staging)
    );

    @Override
    public String getId() {
        return "gravity";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        final double mKg = course.getCyclist().getMKg();
        double grade = location.getGrade();
        double coef = Math.sin(Math.atan(grade));
        location.putDebug("p_" + getId(), FORMUL, Unit.FORMULA_WATTS);
        return -mKg * Constants.G * status.getSpeed() * coef;
    }
}
