package io.github.glandais.gpx.virtual.power.aero;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKeyKind;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import io.github.glandais.gpx.data.values.unit.Formul;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.power.PowerProvider;
import io.github.glandais.gpx.virtual.power.PowerProviderId;
import io.github.glandais.gpx.virtual.power.aero.wind.Wind;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class AeroPowerProvider implements PowerProvider {

    public static final Formul FORMUL_SIMPLE = new Formul("-aeroCoef*POWER(speed,3)", DoubleUnit.INSTANCE,
            new PropertyKeyKind<>(PropertyKeys.aeroCoef, ValueKind.debug),
            new PropertyKeyKind<>(PropertyKeys.speed, ValueKind.staging)
    );

    @Override
    public PowerProviderId getId() {
        return PowerProviderId.aero;
    }

    @Override
    public double getPowerW(Course course, Point location) {
        final double aeroCoef = course.getAeroProvider().getAeroCoef(course, location);
        location.putDebug(PropertyKeys.aeroCoef, aeroCoef);
        final Wind wind = course.getWindProvider().getWind(course, location);
        double p_air;
        if (wind.windSpeed() == 0) {
            double speed = location.getSpeed();
            location.putDebug(PropertyKeys.p_aero_formula, FORMUL_SIMPLE);
            p_air = -aeroCoef * speed * speed * speed;
        } else {
            p_air = computePAirWithWind(location, aeroCoef, wind);
            // FIXME formulas
        }
        return p_air;
    }

    private double computePAirWithWind(Point current, double aeroCoef, Wind wind) {
        double speed = current.getSpeed();
        double bearing = current.getBearing();
        current.putDebug(PropertyKeys.wind_speed, wind.windSpeed());
        current.putDebug(PropertyKeys.wind_direction, wind.windDirection());
        double windDirectionAsBearing = (Math.PI / 2) - wind.windDirection();
        current.putDebug(PropertyKeys.wind_bearing, windDirectionAsBearing);

        double alpha = windDirectionAsBearing - bearing;
        current.putDebug(PropertyKeys.wind_alpha, alpha);

        double v = wind.windSpeed();

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
