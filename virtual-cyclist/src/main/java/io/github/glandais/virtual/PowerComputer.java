package io.github.glandais.virtual;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.wind.Wind;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PowerComputer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerComputer.class);

    // m.s-2, minimal speed = 2km/h
    private static final double MINIMAL_SPEED = 2.0 / 3.6;
    // s
    private static final double DT = 0.25;

    public void computeTrack(Course course) {

        final List<Point> newPoints = new ArrayList<>();

        final CyclistStatus status = new CyclistStatus();
        status.speed = MINIMAL_SPEED;

        int i = 0;
        Point nextPoint = null;
        while (status.odo < course.getGpxPath().getDist()) {
            nextPoint = getNextPoint(course, status);
            if (i % 4 == 0) {
                newPoints.add(nextPoint);
            }
            i++;
        }
        if ((i - 1) % 4 == 0) {
            newPoints.add(nextPoint);
        }

        course.getGpxPath().setPoints(newPoints);
        course.getGpxPath().computeArrays();
    }

    private Point getNextPoint(Course course, CyclistStatus status) {
        final long startMillis = course.getStart().toEpochMilli();
        long now = startMillis + ((long) (1000 * status.ellapsed));
        Point current = getPoint(course, status.odo, now);
        current.setTime(Instant.ofEpochMilli(now));

        current.putDebug("0_0_odo", status.odo, Unit.METERS);
        current.putDebug("0_1_speed", status.speed, Unit.SPEED_S_M);

        double p_grav = computePGrav(course, status, current);
        double p_frot = computePFrot(course, status, current);
        double p_air = computePAir(course, status, current);
        double p_cyclist = getPCyclist(course, status, current, p_grav, p_frot, p_air);

        // p_app = cyclist power - resistance
        double p_app = p_cyclist - p_air - p_frot - p_grav;
        current.putDebug("5_1_p_app", p_app, Unit.WATTS);

        final double mKg = course.getCyclist().getMKg();
        // m.s-2
        double acc = p_app / (Constants.G * mKg);
        current.putDebug("5_2_acc", acc, Unit.DOUBLE_ANY);

        status.speed = status.speed + acc * DT;
        current.putDebug("5_3_new_speed", status.speed, Unit.SPEED_S_M);

        current.putDebug("5_4_max_speed", current.getMaxSpeed(), Unit.SPEED_S_M);

        status.speed = Math.max(MINIMAL_SPEED, Math.min(current.getMaxSpeed(), status.speed));
        current.putDebug("5_5_fixed_speed", status.speed, Unit.SPEED_S_M);

        current.setSpeed(status.speed);

        double dx = DT * status.speed;
        current.putDebug("5_6_dx", dx, Unit.METERS);

        status.odo = status.odo + dx;
        status.ellapsed = status.ellapsed + DT;
        //log.info("{}s {}m", ellapsed, odo);
        return current;
    }

    private double getPCyclist(Course course, CyclistStatus status, Point current, double p_grav, double p_frot, double p_air) {
        double grad = current.getGrade();
        double p_cyclist = course.getPowerProvider().getPowerW(current, status.ellapsed, p_air, p_frot, p_grav, status.speed, grad);
        current.putDebug("5_0_p_cyclist", p_cyclist, Unit.WATTS);
        return p_cyclist;
    }

    private double computePGrav(Course course, CyclistStatus status, Point current) {
        final double mKg = course.getCyclist().getMKg();
        double grad = current.getGrade();
        double p_grav = mKg * Constants.G * status.speed * grad;
        current.putDebug("2_2_p_grav", p_grav, Unit.WATTS);
        return p_grav;
    }

    private double computePFrot(Course course, CyclistStatus status, Point current) {
        final double mKg = course.getCyclist().getMKg();
        final double f = course.getCyclist().getF();
        double p_frot = mKg * Constants.G * status.speed * f;
        current.putDebug("3_0_f", f, Unit.PERCENTAGE);
        current.putDebug("3_1_p_frot", p_frot, Unit.WATTS);
        return p_frot;
    }

    private double computePAir(Course course, CyclistStatus status, Point current) {
        double grad = current.getGrade();
        final double cx = course.getCxProvider().getCx(current, status.ellapsed, status.speed, grad);
        final Wind wind = course.getWindProvider().getWind(current, status.ellapsed);
        current.putDebug("4_0_cx", cx, Unit.CX);
        current.putDebug("4_1_wind_speed", wind.getWindSpeed(), Unit.SPEED_S_M);
        current.putDebug("4_2_wind_direction", wind.getWindDirection(), Unit.RADIANS);
        double p_air;
        if (wind.getWindSpeed() == 0) {
            p_air = cx * status.speed * status.speed * status.speed;
        } else {
            p_air = computePAirWithWind(status, current, cx, wind);
        }
        current.putDebug("4_6_p_air", p_air, Unit.WATTS);
        return p_air;
    }

    private double computePAirWithWind(CyclistStatus status, Point current, double cx, Wind wind) {
        double bearing = current.getBearing();
        current.putDebug("4_3_cyclist_bearing", bearing, Unit.RADIANS);
        double windDirectionAsBearing = (Math.PI / 2) - wind.getWindDirection();
        current.putDebug("4_4_wind_bearing", windDirectionAsBearing, Unit.RADIANS);

        double alpha = windDirectionAsBearing - bearing;
        current.putDebug("4_5_wind_alpha", alpha, Unit.RADIANS);

        double v = wind.getWindSpeed();

        // https://www.sheldonbrown.com/isvan/Power%20Management%20for%20Lightweight%20Vehicles.pdf

        double l1 = status.speed + v * Math.cos(alpha);
        double l2 = Math.pow(l1, 2);
        double l3 = status.speed * status.speed + v * v + 2 * status.speed * v * Math.cos(alpha);
        double l4 = l2 / l3;

        double mu = 1.2;
        double lambda = l4 + mu * (1 - l4);

        return cx * lambda * Math.sqrt(l3) * l1 * status.speed;
    }

    private Point getPoint(Course course, double d, long now) {

        List<Point> points = course.getGpxPath().getPoints();
        double[] dists = course.getGpxPath().getDists();
        for (int i = 0; i < points.size(); i++) {
            if (d <= dists[i]) {
                if (i == 0) {
                    return points.get(0);
                } else {
                    Point p = points.get(i - 1);
                    Point pp1 = points.get(i);
                    if (dists[i] - dists[i - 1] == 0) {
                        continue;
                    }

                    double ratio = (d - dists[i - 1]) / (dists[i] - dists[i - 1]);
                    Point result = Point.interpolate(p, pp1, ratio, now);
                    result.setGrade(p.getGrade());
                    result.setBearing(p.getBearing());
                    return result;
                }
            }
        }
        throw new IllegalStateException();
    }

}
