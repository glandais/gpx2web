package io.github.glandais.gpx.virtual.power.rolling;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.power.PowerProvider;
import io.github.glandais.gpx.virtual.power.PowerProviderId;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class RollingResistancePowerProvider implements PowerProvider {

//    public static final Formul FORMUL = new Formul("-(crr)*mKg*9.81*(speed)*COS(ATAN(grade))",
//            Unit.WATTS,
//            new ValueKey("crr", ValueKind.debug),
//            new ValueKey("mKg", ValueKind.debug),
//            new ValueKey("speed", ValueKind.staging),
//            new ValueKey("grade", ValueKind.staging)
//    );

    @Override
    public PowerProviderId getId() {
        return PowerProviderId.rolling_resistance;
    }

    @Override
    public double getPowerW(Course course, Point location) {

        final double mKg = course.getCyclist().getMKg();
        final double crr = course.getBike().getCrr();
        final double grade = location.getGrade();

        double coef = Math.cos(Math.atan(grade));

//        location.putDebug("p_" + getId(), FORMUL, Unit.FORMULA_WATTS);
        return -coef * mKg * Constants.G * location.getSpeed() * crr;
    }

}
