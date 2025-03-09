package io.github.glandais.gpx.virtual.power.grav;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.power.PowerProvider;
import io.github.glandais.gpx.virtual.power.PowerProviderId;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class GravPowerProvider implements PowerProvider {

//    public static final Formul FORMUL = new Formul("-mKg*9.81*(speed)*SIN(ATAN(grade))",
//            Unit.WATTS,
//            new ValueKey("mKg", ValueKind.debug),
//            new ValueKey("speed", ValueKind.staging),
//            new ValueKey("grade", ValueKind.staging)
//    );

    @Override
    public PowerProviderId getId() {
        return PowerProviderId.gravity;
    }

    @Override
    public double getPowerW(Course course, Point location) {
        final double mKg = course.getCyclist().getMKg();
        double grade = location.getGrade();
        double coef = Math.sin(Math.atan(grade));
//        location.putDebug("p_" + getId(), FORMUL, Unit.FORMULA_WATTS);
        return -mKg * Constants.G * location.getSpeed() * coef;
    }

}
