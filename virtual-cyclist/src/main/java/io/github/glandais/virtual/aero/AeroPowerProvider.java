package io.github.glandais.virtual.aero;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Formul;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKey;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import io.github.glandais.virtual.aero.wind.Wind;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class AeroPowerProvider implements PowerProvider {

    public static final Formul FORMUL_SIMPLE = new Formul("-aeroCoef*POWER(speed,3)", Unit.WATTS,
            new ValueKey("aeroCoef", ValueKind.debug),
            new ValueKey("speed", ValueKind.staging)
    );

    @Override
    public String getId() {
        return "aero";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        final double aeroCoef = course.getAeroProvider().getAeroCoef(course, location, status);
        location.putDebug("aeroCoef", aeroCoef, Unit.AERO_COEF);
        final Wind wind = course.getWindProvider().getWind(course, location, status);
        double p_air;
        if (wind.getWindSpeed() == 0) {
            double speed = status.getSpeed();
            location.putDebug("p_" + getId(), FORMUL_SIMPLE, Unit.FORMULA_WATTS);
            p_air = -aeroCoef * speed * speed * speed;
        } else {
            p_air = computePAirWithWind(status, location, aeroCoef, wind);
            // FIXME formulas
            location.putDebug("p_" + getId(), p_air, Unit.WATTS);
        }
        return p_air;
    }

    private double computePAirWithWind(CyclistStatus status, Point current, double aeroCoef, Wind wind) {
        double speed = status.getSpeed();
        double bearing = current.getBearing();
        current.putDebug("wind_speed", wind.getWindSpeed(), Unit.SPEED_S_M);
        current.putDebug("wind_direction", wind.getWindDirection(), Unit.RADIANS);
        current.putDebug("cyclist_bearing", bearing, Unit.RADIANS);
        double windDirectionAsBearing = (Math.PI / 2) - wind.getWindDirection();
        current.putDebug("wind_bearing", windDirectionAsBearing, Unit.RADIANS);

        double alpha = windDirectionAsBearing - bearing;
        current.putDebug("wind_alpha", alpha, Unit.RADIANS);

        double v = wind.getWindSpeed();

        // https://www.sheldonbrown.com/isvan/Power%20Management%20for%20Lightweight%20Vehicles.pdf

        double l1 = speed + v * Math.cos(alpha);
        double l2 = Math.pow(l1, 2);
        double l3 = speed * speed + v * v + 2 * speed * v * Math.cos(alpha);
        double l4 = l2 / l3;

        double mu = 1.2;
        double lambda = l4 + mu * (1 - l4);

        return -aeroCoef * lambda * Math.sqrt(l3) * l1 * speed;
    }

}
