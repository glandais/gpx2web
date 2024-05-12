package io.github.glandais.virtual.rolling;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Formul;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKey;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import org.springframework.stereotype.Service;

import jakarta.inject.Singleton;

@Service
@Singleton
public class RollingResistancePowerProvider implements PowerProvider {

    public static final Formul FORMUL = new Formul("-(crr)*mKg*9.81*(speed)*COS(ATAN(grade))",
            Unit.WATTS,
            new ValueKey("crr", ValueKind.debug),
            new ValueKey("mKg", ValueKind.debug),
            new ValueKey("speed", ValueKind.staging),
            new ValueKey("grade", ValueKind.staging)
    );

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

        location.putDebug("p_" + getId(), FORMUL, Unit.FORMULA_WATTS);
        return p_rr;
    }
}
