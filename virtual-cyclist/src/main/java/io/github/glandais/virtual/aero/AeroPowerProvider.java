package io.github.glandais.virtual.aero;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Formul;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKey;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import io.github.glandais.virtual.aero.wind.Wind;
import org.springframework.stereotype.Service;

@Service
public class AeroPowerProvider implements PowerProvider {

    public static final Formul FORMUL_SIMPLE = new Formul("-cx*POWER(speed,3)", Unit.WATTS,
            new ValueKey("cx", ValueKind.debug),
            new ValueKey("speed", ValueKind.staging)
    );

    @Override
    public String getId() {
        return "aero";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        double grade = location.getGrade();
        double speed = status.getSpeed();
        final double cx = course.getCxProvider().getCx(location, status.getEllapsed(), speed, grade);
        location.putDebug("cx", cx, Unit.CX);
        final Wind wind = course.getWindProvider().getWind(location, status.getEllapsed());
        double p_air;
        if (wind.getWindSpeed() == 0) {
            location.putDebug("p_" + getId(), FORMUL_SIMPLE, Unit.FORMULA_WATTS);
            p_air = -cx * speed * speed * speed;
        } else {
            p_air = computePAirWithWind(status, location, cx, wind);
            // FIXME formulas
            location.putDebug("p_" + getId(), p_air, Unit.WATTS);
        }
        return p_air;
    }

    private double computePAirWithWind(CyclistStatus status, Point current, double cx, Wind wind) {
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

        return -cx * lambda * Math.sqrt(l3) * l1 * speed;
    }

}
