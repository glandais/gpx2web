package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import io.github.glandais.virtual.wind.Wind;
import org.springframework.stereotype.Service;

@Service
public class AeroPowerProvider implements PowerProvider {

    @Override
    public String getId() {
        return "aero";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        double grade = location.getGrade();
        double speed = status.getSpeed();
        final double cx = course.getCxProvider().getCx(location, status.getEllapsed(), speed, grade);
        final Wind wind = course.getWindProvider().getWind(location, status.getEllapsed());
        location.putDebug("4_0_cx", cx, Unit.CX);
        double p_air;
        if (wind.getWindSpeed() == 0) {
            p_air = -cx * speed * speed * speed;
        } else {
            p_air = computePAirWithWind(status, location, cx, wind);
        }
        location.putDebug("4_6_p_air", p_air, Unit.WATTS);
        return p_air;
    }

    private double computePAirWithWind(CyclistStatus status, Point current, double cx, Wind wind) {
        double speed = status.getSpeed();
        double bearing = current.getBearing();
        current.putDebug("4_1_wind_speed", wind.getWindSpeed(), Unit.SPEED_S_M);
        current.putDebug("4_2_wind_direction", wind.getWindDirection(), Unit.RADIANS);
        current.putDebug("4_3_cyclist_bearing", bearing, Unit.RADIANS);
        double windDirectionAsBearing = (Math.PI / 2) - wind.getWindDirection();
        current.putDebug("4_4_wind_bearing", windDirectionAsBearing, Unit.RADIANS);

        double alpha = windDirectionAsBearing - bearing;
        current.putDebug("4_5_wind_alpha", alpha, Unit.RADIANS);

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
