package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.gpx.Point;
import io.github.glandais.util.Constants;
import io.github.glandais.util.MagicPower2MapSpace;
import io.github.glandais.util.Vector;
import io.github.glandais.virtual.wind.Wind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PowerComputerOld {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerComputerOld.class);

    // m.s-2, minimal speed = 2km/h
    private static final double MINIMAL_SPEED = 2.0 / 3.6;
/*
    public void computeTrack(Course course) {

        List<Point> points = course.getGpxPath().getPoints();
        List<Point> newPoints = new ArrayList<>(points.size());

        ZonedDateTime start = course.getStart();

        Point prev = null;

        for (int i = 0; i < points.size(); i++) {
            Point current = points.get(i);
            if (prev != null) {
                // meters
                double dist = prev.distanceTo(current);
                if (dist > 0) {
                    // point to point result
                    computePointToPoint(prev, current, start, course, newPoints);
                    prev = newPoints.get(newPoints.size() - 1);
                }
            } else {
                current.setSpeed(0.0);
                current.setTime(start);
                newPoints.add(current);
                prev = current;
            }
        }
        course.getGpxPath().setPoints(newPoints);
        course.getGpxPath().computeArrays();
    }

    private void computePointToPoint(Point from, Point to, ZonedDateTime start, Course course, List<Point> newPoints) {

        final ZonedDateTime fromTime = from.getTime();
        long fromMillis = fromTime.toInstant().toEpochMilli();
        final Duration ellapsed = Duration.between(start, fromTime);

        final double mKg = course.getCyclist().getMKg();
        final double f = course.getCyclist().getF();

        // max speeds, m.s-2
        final double ms1 = from.getMaxSpeed();
        final double ms2 = to.getMaxSpeed();

        final double dist = from.distanceTo(to);

        final double dz = to.getZ() - from.getZ();
        final double grad = dz / dist;

        final double cx = course.getCxProvider().getCx(from, to, fromTime, ellapsed, from.getSpeed(), grad);
        final Wind wind = course.getWindProvider().getWind(from, fromTime, ellapsed);

        // we will split the distance
        // current speed m.s-2
        double speed = from.getSpeed();
        // m
        double d = 0.0;
        // s
        double t = 0.0;
        // s
        final double dt = 0.25;

        double p_cyclist_tot = 0.0;
        double p_air_tot = 0.0;
        double p_frot_tot = 0.0;
        double p_grav_tot = 0.0;

        // (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
        while (true) {
            double prev_speed = speed;
            double prev_d = d;
            double prev_t = t;

            // Compute max speed
            double ms;
            if (d > dist) {
                ms = ms2;
            } else {
                double c = d / dist;
                ms = ms1 + c * (ms2 - ms1);
            }

            double p_grav = mKg * Constants.G * speed * grad;
            double p_frot = mKg * Constants.G * speed * f;

            double p_air;
            if (wind.getWindSpeed() == 0) {
                p_air = cx * speed * speed * speed;
            } else {

                Vector v_from = project(from);
                Vector v_to = project(to);
                double dy2 = v_to.getY() - v_from.getY();
                double dx2 = v_to.getX() - v_from.getX();
                double bearing = Math.atan2(-dy2, dx2);
                double windDirectionAsBearing = (Math.PI / 2) - wind.getWindDirection();

                double alpha = windDirectionAsBearing - bearing;
                double v = wind.getWindSpeed();

                // https://www.sheldonbrown.com/isvan/Power%20Management%20for%20Lightweight%20Vehicles.pdf

                double l1 = speed + v * Math.cos(alpha);
                double l2 = Math.pow(l1, 2);
                double l3 = speed * speed + v * v + 2 * speed * v * Math.cos(alpha);
                double l4 = l2 / l3;

                double mu = 1.2;
                double lambda = l4 + mu * (1 - l4);

                p_air = cx * lambda * Math.sqrt(l3) * l1 * speed;
            }

            double p_cyclist = course.getPowerProvider().getPowerW(from, to, fromTime, ellapsed, p_air, p_frot, p_grav, speed, grad);

            // p_app = cyclist power - resistance
            double p_app = p_cyclist - p_air - p_frot - p_grav;

            // m.s-2
            double acc = p_app / mKg;
            speed = speed + acc * dt;

            if (speed > ms) {
                speed = ms;
            }
            if (speed < MINIMAL_SPEED) {
                speed = MINIMAL_SPEED;
            }

            double dx = dt * speed;
            d = d + dx;
            t = t + dt;
            if (d > dist) {
                double ratio = (dist - prev_d) / dx;
                t = prev_t + dt * ratio;
                speed = prev_speed + acc * dt * ratio;
                if (speed < MINIMAL_SPEED) {
                    speed = MINIMAL_SPEED;
                }

                p_grav_tot = p_grav_tot + p_grav * dt * ratio;
                p_frot_tot = p_frot_tot + p_frot * dt * ratio;
                p_cyclist_tot = p_cyclist_tot + p_cyclist * dt * ratio;
                p_air_tot = p_air_tot + p_air * dt * ratio;

                ZonedDateTime end = from.getTime().plus(Duration.ofNanos((long) (t * 1000 * 1000 * 1000)));
                to.setTime(end);
                to.setSpeed(speed);
                to.getData().put("ratio", 1.0);
                to.getData().put("p_cyclist", p_cyclist);
                to.getData().put("p_air", p_air);
                to.getData().put("p_frot", p_frot);
                to.getData().put("p_grav", p_grav);
                if (t > 0) {
//                    from.getData().put("p_cyclist", p_cyclist_tot / t);
//                    from.getData().put("p_air", p_air_tot / t);
//                    from.getData().put("p_frot", p_frot_tot / t);
//                    from.getData().put("p_grav", p_grav_tot / t);
                }
                newPoints.add(to);
                return;
            } else {

                p_grav_tot = p_grav_tot + p_grav * dt;
                p_frot_tot = p_frot_tot + p_frot * dt;
                p_cyclist_tot = p_cyclist_tot + p_cyclist * dt;
                p_air_tot = p_air_tot + p_air * dt;

                double ratio = d / dist;
                long epochMillis = (long) (fromMillis + t * 1000);
                Point point = GPXPerSecond.getPoint(from, to, ratio, epochMillis);
                point.getData().put("p_cyclist", p_cyclist);
                point.getData().put("p_air", p_air);
                point.getData().put("p_frot", p_frot);
                point.getData().put("p_grav", p_grav);
                point.getData().put("ratio", ratio);
                newPoints.add(point);
            }
        }
    }

    private Vector project(final Point point) {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(point.getLonDeg(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(point.getLatDeg(), 12));
    }
*/
}
